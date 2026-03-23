-- Kavach Notification Service: Notifications Table
CREATE TABLE IF NOT EXISTS kavach_notifications.kavach_notifications (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL,
    user_id UUID,
    channel VARCHAR(20) NOT NULL,
    template_id VARCHAR(50),
    recipient VARCHAR(100) NOT NULL,
    message_content TEXT,
    delivery_status VARCHAR(20) DEFAULT 'PENDING',
    whatsapp_message_id VARCHAR(100),
    retry_count INTEGER DEFAULT 0,
    created_at TIMESTAMP DEFAULT NOW(),
    sent_at TIMESTAMP
);

CREATE INDEX idx_notifications_tenant ON kavach_notifications.kavach_notifications(tenant_id);
CREATE INDEX idx_notifications_status ON kavach_notifications.kavach_notifications(delivery_status);

-- Gazette notifications table (for Kavach Gazette Watcher)
CREATE TABLE IF NOT EXISTS kavach_notifications.anupalan_gazette_notifications (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    source_url VARCHAR(500),
    source_hash VARCHAR(64) UNIQUE,
    published_date DATE,
    effective_date DATE,
    affected_nic_codes TEXT,
    affected_states TEXT,
    summary_en TEXT,
    summary_hi TEXT,
    action_required TEXT,
    created_at TIMESTAMP DEFAULT NOW()
);

CREATE INDEX idx_gazette_hash ON kavach_notifications.anupalan_gazette_notifications(source_hash);
