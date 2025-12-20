#!/bin/bash
# ═══════════════════════════════════════════════════════════════════
# Sport SaaS - Database Restore Script
# ═══════════════════════════════════════════════════════════════════
# Usage: ./scripts/restore-db.sh <backup_file>

set -e

# ─────────────────────────────────────────────────────────────────
# Configuration
# ─────────────────────────────────────────────────────────────────
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(dirname "$SCRIPT_DIR")"
BACKUP_FILE="$1"

# Load environment variables
if [ -f "$PROJECT_ROOT/.env" ]; then
    export $(grep -v '^#' "$PROJECT_ROOT/.env" | xargs)
fi

# Default values
POSTGRES_USER="${POSTGRES_USER:-sportsaas}"
POSTGRES_DB="${POSTGRES_DB:-sportsaas}"
CONTAINER_NAME="sportsaas-postgres"

# Colors
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m'

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
if [ -z "$BACKUP_FILE" ]; then
    log_error "Usage: $0 <backup_file>"
    echo ""
    echo "Available backups:"
    ls -la "$PROJECT_ROOT/backups/"*.sql.gz 2>/dev/null || echo "  No backups found"
    exit 1
fi

if [ ! -f "$BACKUP_FILE" ]; then
    # Try in backups directory
    if [ -f "$PROJECT_ROOT/backups/$BACKUP_FILE" ]; then
        BACKUP_FILE="$PROJECT_ROOT/backups/$BACKUP_FILE"
    else
        log_error "Backup file not found: $BACKUP_FILE"
        exit 1
    fi
fi

# ─────────────────────────────────────────────────────────────────
# Main
# ─────────────────────────────────────────────────────────────────
main() {
    log_warning "This will REPLACE ALL DATA in the database!"
    read -p "Are you sure you want to restore from $BACKUP_FILE? (y/N) " -n 1 -r
    echo

    if [[ ! $REPLY =~ ^[Yy]$ ]]; then
        log_info "Restore cancelled"
        exit 0
    fi

    # Check if container is running
    if ! docker ps --format '{{.Names}}' | grep -q "^${CONTAINER_NAME}$"; then
        log_error "PostgreSQL container is not running"
        exit 1
    fi

    log_info "Restoring database from: $BACKUP_FILE"

    # Drop and recreate database
    log_info "Dropping existing database..."
    docker exec "$CONTAINER_NAME" psql -U "$POSTGRES_USER" -c "DROP DATABASE IF EXISTS $POSTGRES_DB;"
    docker exec "$CONTAINER_NAME" psql -U "$POSTGRES_USER" -c "CREATE DATABASE $POSTGRES_DB OWNER $POSTGRES_USER;"

    # Restore backup
    log_info "Restoring data..."
    gunzip -c "$BACKUP_FILE" | docker exec -i "$CONTAINER_NAME" psql -U "$POSTGRES_USER" "$POSTGRES_DB"

    log_success "Database restored successfully!"
    log_warning "You may need to restart the backend service: docker-compose restart backend"
}

main
