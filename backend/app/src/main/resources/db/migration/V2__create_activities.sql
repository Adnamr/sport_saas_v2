-- ============================================================================
-- V2__create_activities.sql
-- Sport SaaS - Activities table for dashboard activity log
-- ============================================================================

-- ============================================================================
-- ACTIVITIES
-- ============================================================================
CREATE TABLE activities (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    tenant_id UUID NOT NULL REFERENCES tenants(id) ON DELETE CASCADE,
    type VARCHAR(50) NOT NULL,
    description VARCHAR(500) NOT NULL,
    user_id UUID,
    user_name VARCHAR(255),
    entity_type VARCHAR(100),
    entity_id UUID,
    metadata TEXT,
    ip_address VARCHAR(45),
    user_agent VARCHAR(500),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Indexes
CREATE INDEX idx_activities_tenant ON activities(tenant_id);
CREATE INDEX idx_activities_type ON activities(type);
CREATE INDEX idx_activities_user ON activities(user_id);
CREATE INDEX idx_activities_entity ON activities(entity_type, entity_id);
CREATE INDEX idx_activities_created ON activities(created_at DESC);
