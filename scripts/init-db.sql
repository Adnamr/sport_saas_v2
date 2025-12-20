-- ═══════════════════════════════════════════════════════════════════
-- Sport SaaS - Database Initialization Script
-- ═══════════════════════════════════════════════════════════════════
-- This script runs when the PostgreSQL container is first created

-- Enable UUID extension
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- Grant privileges
GRANT ALL PRIVILEGES ON DATABASE sportsaas TO sportsaas;

-- Create schemas if needed (Flyway will handle migrations)
-- The actual tables are created by Flyway migrations

-- Log completion
DO $$
BEGIN
    RAISE NOTICE 'Database initialization completed successfully';
END $$;
