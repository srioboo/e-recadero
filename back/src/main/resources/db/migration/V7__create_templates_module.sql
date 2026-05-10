-- Flyway Migration V7: Create Templates Module Tables
-- This migration creates all tables for the templates module

-- Template table
CREATE TABLE IF NOT EXISTS templates.template (
    id BIGSERIAL PRIMARY KEY,
    template_name VARCHAR(255) NOT NULL,
    template_type VARCHAR(100) NOT NULL,
    description TEXT,
    slug VARCHAR(255) UNIQUE,
    is_active BOOLEAN DEFAULT true,
    is_default BOOLEAN DEFAULT false,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(255),
    updated_by VARCHAR(255)
);
CREATE INDEX idx_template_type ON templates.template(template_type);
CREATE INDEX idx_template_active ON templates.template(is_active);
COMMENT ON TABLE templates.template IS 'Email, SMS, and content templates';

-- Template Version table
CREATE TABLE IF NOT EXISTS templates.template_version (
    id BIGSERIAL PRIMARY KEY,
    template_id BIGINT NOT NULL REFERENCES templates.template(id) ON DELETE CASCADE,
    version_number INTEGER NOT NULL,
    subject VARCHAR(255),
    content_html TEXT,
    content_text TEXT,
    content_json JSONB,
    variables JSONB,
    is_published BOOLEAN DEFAULT false,
    published_at TIMESTAMP WITH TIME ZONE,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(255),
    UNIQUE(template_id, version_number)
);
CREATE INDEX idx_version_template ON templates.template_version(template_id);
CREATE INDEX idx_version_published ON templates.template_version(template_id, is_published);
COMMENT ON TABLE templates.template_version IS 'Versioned template content with HTML, text, and JSON formats';

-- Template Block table (for modular template construction)
CREATE TABLE IF NOT EXISTS templates.template_block (
    id BIGSERIAL PRIMARY KEY,
    version_id BIGINT NOT NULL REFERENCES templates.template_version(id) ON DELETE CASCADE,
    block_name VARCHAR(255) NOT NULL,
    block_type VARCHAR(100) NOT NULL,
    content_html TEXT,
    content_text TEXT,
    sort_order INTEGER DEFAULT 0,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);
CREATE INDEX idx_block_version ON templates.template_block(version_id);
CREATE INDEX idx_block_type ON templates.template_block(block_type);
COMMENT ON TABLE templates.template_block IS 'Reusable template blocks (header, footer, content sections)';

-- Template Metadata table
CREATE TABLE IF NOT EXISTS templates.template_meta (
    id BIGSERIAL PRIMARY KEY,
    template_id BIGINT NOT NULL REFERENCES templates.template(id) ON DELETE CASCADE,
    meta_key VARCHAR(255) NOT NULL,
    meta_value VARCHAR(1000),
    UNIQUE(template_id, meta_key)
);
CREATE INDEX idx_meta_template ON templates.template_meta(template_id);
COMMENT ON TABLE templates.template_meta IS 'Key-value metadata for templates (tags, categories, etc.)';

-- Template Attachment table
CREATE TABLE IF NOT EXISTS templates.template_attachment (
    id BIGSERIAL PRIMARY KEY,
    version_id BIGINT NOT NULL REFERENCES templates.template_version(id) ON DELETE CASCADE,
    file_name VARCHAR(255) NOT NULL,
    file_url VARCHAR(1000) NOT NULL,
    file_type VARCHAR(100),
    file_size BIGINT,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);
CREATE INDEX idx_attach_version ON templates.template_attachment(version_id);
COMMENT ON TABLE templates.template_attachment IS 'Attachments for template versions';

-- Template Preview table
CREATE TABLE IF NOT EXISTS templates.template_preview (
    id BIGSERIAL PRIMARY KEY,
    version_id BIGINT NOT NULL REFERENCES templates.template_version(id) ON DELETE CASCADE,
    preview_url VARCHAR(1000),
    thumbnail_url VARCHAR(1000),
    preview_generated_at TIMESTAMP WITH TIME ZONE,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);
CREATE INDEX idx_preview_version ON templates.template_preview(version_id);
COMMENT ON TABLE templates.template_preview IS 'Preview images and URLs for templates';

-- Template Usage Tracking table
CREATE TABLE IF NOT EXISTS templates.template_usage (
    id BIGSERIAL PRIMARY KEY,
    version_id BIGINT NOT NULL REFERENCES templates.template_version(id) ON DELETE CASCADE,
    usage_type VARCHAR(100) NOT NULL,
    usage_count INTEGER DEFAULT 0,
    last_used_at TIMESTAMP WITH TIME ZONE,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);
CREATE INDEX idx_usage_version ON templates.template_usage(version_id);
COMMENT ON TABLE templates.template_usage IS 'Tracking template usage across the system';
