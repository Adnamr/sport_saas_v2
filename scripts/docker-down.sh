#!/bin/bash
# ═══════════════════════════════════════════════════════════════════
# Sport SaaS - Stop Docker Services
# ═══════════════════════════════════════════════════════════════════
# Usage: ./scripts/docker-down.sh [--volumes]

set -e

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(dirname "$SCRIPT_DIR")"

# Colors
YELLOW='\033[1;33m'
GREEN='\033[0;32m'
BLUE='\033[0;34m'
NC='\033[0m'

cd "$PROJECT_ROOT"

if [ "$1" = "--volumes" ]; then
    echo -e "${YELLOW}[WARNING]${NC} This will remove all data volumes!"
    read -p "Are you sure? (y/N) " -n 1 -r
    echo
    if [[ $REPLY =~ ^[Yy]$ ]]; then
        echo -e "${BLUE}[INFO]${NC} Stopping services and removing volumes..."
        docker-compose down -v
    else
        echo -e "${BLUE}[INFO]${NC} Cancelled"
        exit 0
    fi
else
    echo -e "${BLUE}[INFO]${NC} Stopping services..."
    docker-compose down
fi

echo -e "${GREEN}[SUCCESS]${NC} Services stopped!"
