#!/bin/bash
# ═══════════════════════════════════════════════════════════════════
# Sport SaaS - Deployment Script
# ═══════════════════════════════════════════════════════════════════
# Usage: ./scripts/deploy.sh [environment]
# Environment: dev (default), staging, prod

set -e

# ─────────────────────────────────────────────────────────────────
# Configuration
# ─────────────────────────────────────────────────────────────────
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(dirname "$SCRIPT_DIR")"
ENV="${1:-dev}"

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# ─────────────────────────────────────────────────────────────────
# Helper Functions
# ─────────────────────────────────────────────────────────────────
log_info() {
    echo -e "${BLUE}[INFO]${NC} $1"
}

log_success() {
    echo -e "${GREEN}[SUCCESS]${NC} $1"
}

log_warning() {
    echo -e "${YELLOW}[WARNING]${NC} $1"
}

log_error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

# ─────────────────────────────────────────────────────────────────
# Validation
# ─────────────────────────────────────────────────────────────────
validate_environment() {
    case "$ENV" in
        dev|staging|prod)
            log_info "Deploying to: $ENV"
            ;;
        *)
            log_error "Invalid environment: $ENV"
            log_error "Valid options: dev, staging, prod"
            exit 1
            ;;
    esac
}

check_prerequisites() {
    log_info "Checking prerequisites..."

    # Check Docker
    if ! command -v docker &> /dev/null; then
        log_error "Docker is not installed"
        exit 1
    fi

    # Check Docker Compose
    if ! command -v docker-compose &> /dev/null && ! docker compose version &> /dev/null; then
        log_error "Docker Compose is not installed"
        exit 1
    fi

    # Check .env file
    if [ ! -f "$PROJECT_ROOT/.env" ]; then
        log_warning ".env file not found. Copying from .env.example..."
        cp "$PROJECT_ROOT/.env.example" "$PROJECT_ROOT/.env"
        log_warning "Please review and update .env file before proceeding"
    fi

    log_success "Prerequisites check passed"
}

# ─────────────────────────────────────────────────────────────────
# Deployment Functions
# ─────────────────────────────────────────────────────────────────
pull_latest() {
    log_info "Pulling latest changes..."
    cd "$PROJECT_ROOT"
    git fetch origin
    git pull origin "$(git branch --show-current)"
    log_success "Code updated"
}

build_images() {
    log_info "Building Docker images..."
    cd "$PROJECT_ROOT"

    if [ "$ENV" = "prod" ]; then
        docker-compose -f docker-compose.yml -f docker-compose.prod.yml build --no-cache
    else
        docker-compose build
    fi

    log_success "Images built successfully"
}

backup_database() {
    log_info "Creating database backup..."
    "$SCRIPT_DIR/backup-db.sh"
    log_success "Database backup completed"
}

deploy_services() {
    log_info "Deploying services..."
    cd "$PROJECT_ROOT"

    if [ "$ENV" = "prod" ]; then
        docker-compose -f docker-compose.yml -f docker-compose.prod.yml up -d
    else
        docker-compose up -d
    fi

    log_success "Services deployed"
}

run_migrations() {
    log_info "Waiting for services to be healthy..."
    sleep 10

    log_info "Database migrations will run automatically via Flyway on backend startup"
    log_success "Migrations handled by application"
}

health_check() {
    log_info "Running health checks..."

    local max_attempts=30
    local attempt=1

    while [ $attempt -le $max_attempts ]; do
        if curl -s http://localhost:8080/actuator/health | grep -q "UP"; then
            log_success "Backend is healthy"
            break
        fi

        if [ $attempt -eq $max_attempts ]; then
            log_error "Backend health check failed after $max_attempts attempts"
            exit 1
        fi

        log_info "Waiting for backend to be healthy... (attempt $attempt/$max_attempts)"
        sleep 5
        ((attempt++))
    done

    if curl -s -o /dev/null -w "%{http_code}" http://localhost:80 | grep -q "200\|304"; then
        log_success "Frontend is healthy"
    else
        log_warning "Frontend may not be responding correctly"
    fi
}

show_status() {
    log_info "Current service status:"
    cd "$PROJECT_ROOT"
    docker-compose ps
}

# ─────────────────────────────────────────────────────────────────
# Main
# ─────────────────────────────────────────────────────────────────
main() {
    echo "═══════════════════════════════════════════════════════════════"
    echo "  Sport SaaS - Deployment Script"
    echo "═══════════════════════════════════════════════════════════════"
    echo ""

    validate_environment
    check_prerequisites

    if [ "$ENV" = "prod" ]; then
        backup_database
    fi

    pull_latest
    build_images
    deploy_services
    run_migrations
    health_check
    show_status

    echo ""
    echo "═══════════════════════════════════════════════════════════════"
    log_success "Deployment to $ENV completed successfully!"
    echo "═══════════════════════════════════════════════════════════════"
}

main
