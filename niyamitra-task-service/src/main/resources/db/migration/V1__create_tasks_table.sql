-- Niyamitra Task Service: Compliance Tasks Table
CREATE TABLE IF NOT EXISTS niyamitra_tasks.compliance_tasks (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL,
    rule_id UUID,
    assigned_to UUID,
    title VARCHAR(500) NOT NULL,
    description TEXT,
    category VARCHAR(30) NOT NULL,
    status VARCHAR(40) NOT NULL DEFAULT 'PENDING',
    due_date DATE,
    completed_date DATE,
    acknowledged BOOLEAN DEFAULT FALSE,
    escalation_level INTEGER DEFAULT 0,
    created_at TIMESTAMP DEFAULT NOW(),
    updated_at TIMESTAMP DEFAULT NOW()
);

CREATE INDEX idx_tasks_tenant ON niyamitra_tasks.compliance_tasks(tenant_id);
CREATE INDEX idx_tasks_status ON niyamitra_tasks.compliance_tasks(status);
CREATE INDEX idx_tasks_due_date ON niyamitra_tasks.compliance_tasks(due_date);
CREATE INDEX idx_tasks_assigned ON niyamitra_tasks.compliance_tasks(assigned_to);
