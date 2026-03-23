-- Niyamitra Profile Service: Tenant and User Tables
CREATE TABLE IF NOT EXISTS niyamitra_profiles.niyamitra_tenants (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    gstin VARCHAR(15) UNIQUE,
    udyam VARCHAR(20),
    company_name VARCHAR(255) NOT NULL,
    nic_code VARCHAR(10),
    state VARCHAR(50),
    district VARCHAR(100),
    industry_category VARCHAR(20),
    preferred_language VARCHAR(5) DEFAULT 'hi',
    onboarded_at TIMESTAMP DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS niyamitra_profiles.niyamitra_users (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL REFERENCES niyamitra_profiles.niyamitra_tenants(id),
    name VARCHAR(255) NOT NULL,
    phone VARCHAR(15) NOT NULL,
    role VARCHAR(20) NOT NULL
);

CREATE TABLE IF NOT EXISTS niyamitra_profiles.kavach_portal_creds (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL REFERENCES niyamitra_profiles.niyamitra_tenants(id),
    portal_name VARCHAR(100) NOT NULL,
    encrypted_username TEXT NOT NULL,
    encrypted_password TEXT NOT NULL
);

CREATE INDEX idx_users_tenant_id ON niyamitra_profiles.niyamitra_users(tenant_id);
CREATE INDEX idx_users_phone ON niyamitra_profiles.niyamitra_users(phone);
CREATE INDEX idx_portal_creds_tenant ON niyamitra_profiles.kavach_portal_creds(tenant_id);
