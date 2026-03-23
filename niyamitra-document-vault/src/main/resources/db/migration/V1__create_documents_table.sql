-- Niyamitra Document Vault: Documents Table
CREATE TABLE IF NOT EXISTS niyamitra_documents.niyamitra_documents (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL,
    task_id UUID,
    original_filename VARCHAR(255),
    s3_key VARCHAR(500),
    file_type VARCHAR(10),
    file_size_bytes BIGINT,
    upload_source VARCHAR(30),
    processing_status VARCHAR(30) DEFAULT 'UPLOADED',
    extraction_result JSONB,
    uploaded_at TIMESTAMP DEFAULT NOW(),
    uploaded_by UUID
);

CREATE INDEX idx_documents_tenant ON niyamitra_documents.niyamitra_documents(tenant_id);
CREATE INDEX idx_documents_task ON niyamitra_documents.niyamitra_documents(task_id);
CREATE INDEX idx_documents_status ON niyamitra_documents.niyamitra_documents(processing_status);
