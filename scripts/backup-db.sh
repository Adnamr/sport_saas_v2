#!/bin/bash
# ═══════════════════════════════════════════════════════════════════
# Sport SaaS - Database Backup Script
# ═══════════════════════════════════════════════════════════════════
# Usage: ./scripts/backup-db.sh [backup_name]

set -e

# ─────────────────────────────────────────────────────────────────
# Configuration
# ─────────────────────────────────────────────────────────────────
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(dirname "$SCRIPT_DIR")"
BACKUP_DIR="$PROJECT_ROOT/backups"
TIMESTAMP=$(date +"%Y%m%d_%H%M%S")
BACKUP_NAME="${1:-backup_$TIMESTAMP}"

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

log_error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

# ─────────────────────────────────────────────────────────────────
# Main
# ─────────────────────────────────────────────────────────────────
main() {
    log_info "Starting database backup..."

    # Create backup directory
    mkdir -p "$BACKUP_DIR"

    # Check if container is running
    if ! docker ps --format '{{.Names}}' | grep -q "^${CONTAINER_NAME}$"; then
        log_error "PostgreSQL container is not running"
        exit 1
    fi

    # Create backup
    BACKUP_FILE="$BACKUP_DIR/${BACKUP_NAME}.sql.gz"

    log_info "Creating backup: $BACKUP_FILE"

    docker exec "$CONTAINER_NAME" pg_dump -U "$POSTGRES_USER" "$POSTGRES_DB" | gzip > "$BACKUP_FILE"

    # Verify backup
    if [ -s "$BACKUP_FILE" ]; then
        BACKUP_SIZE=$(du -h "$BACKUP_FILE" | cut -f1)
        log_success "Backup created successfully: $BACKUP_FILE ($BACKUP_SIZE)"
    else
        log_error "Backup file is empty or was not created"
        rm -f "$BACKUP_FILE"
        exit 1
    fi

    # Cleanup old backups (keep last 10)
    log_info "Cleaning up old backups (keeping last 10)..."
    cd "$BACKUP_DIR"
    ls -t *.sql.gz 2>/dev/null | tail -n +11 | xargs -r rm -f

    log_success "Backup completed!"
}

main
