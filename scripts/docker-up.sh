#!/bin/bash
# ═══════════════════════════════════════════════════════════════════
# Sport SaaS - Start Docker Services
# ═══════════════════════════════════════════════════════════════════
# Usage: ./scripts/docker-up.sh [prod]

set -e

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(dirname "$SCRIPT_DIR")"
ENV="${1:-dev}"

# Colors
GREEN='\033[0;32m'
BLUE='\033[0;34m'
NC='\033[0m'

echo -e "${BLUE}[INFO]${NC} Starting Sport SaaS services ($ENV)..."

cd "$PROJECT_ROOT"

# Check .env file
if [ ! -f ".env" ]; then
    echo -e "${BLUE}[INFO]${NC} Creating .env from .env.example..."
    cp .env.example .env
fi

# Start services
if [ "$ENV" = "prod" ]; then
    docker-compose -f docker-compose.yml -f docker-compose.prod.yml up -d
else
    docker-compose up -d
fi

echo ""
echo -e "${GREEN}[SUCCESS]${NC} Services started!"
echo ""
echo "Available services:"
echo "  - Frontend:  http://localhost:80"
echo "  - Backend:   http://localhost:8080"
echo "  - API Docs:  http://localhost:8080/swagger-ui.html"
if [ "$ENV" != "prod" ]; then
    echo "  - MailHog:   http://localhost:8025"
fi
echo ""
echo "Run 'docker-compose ps' to see service status"
echo "Run 'docker-compose logs -f' to follow logs"
