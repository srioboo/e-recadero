-- V7 created a schema for a different, unrelated domain (email/SMS
-- messaging templates: subject/content_html/content_text/attachments).
-- The actual product requirement (specs/002-backend-ecommerce/contracts/
-- templates-contract.md, already consumed by admin/front — see
-- specs/003-templates-frontend-admin) is a CMS-style page builder:
-- pages composed of ordered content blocks, SEO metadata, and immutable
-- version snapshots. No entity or code ever referenced V7's tables, so
-- it is safe to drop and recreate them with the correct shape rather than
-- attempt an incompatible ALTER migration.

DROP TABLE IF EXISTS templates.template_usage;
DROP TABLE IF EXISTS templates.template_preview;
DROP TABLE IF EXISTS templates.template_attachment;
DROP TABLE IF EXISTS templates.template_meta;
DROP TABLE IF EXISTS templates.template_block;
DROP TABLE IF EXISTS templates.template_version;
DROP TABLE IF EXISTS templates.template;

CREATE TABLE templates.template (
    id UUID PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    type VARCHAR(30) NOT NULL,
    slug VARCHAR(255) NOT NULL UNIQUE,
    status VARCHAR(20) NOT NULL DEFAULT 'DRAFT',
    version INTEGER NOT NULL DEFAULT 1,
    published_version INTEGER,
    created_by VARCHAR(255),
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    published_at TIMESTAMP WITH TIME ZONE
);
CREATE INDEX idx_template_type ON templates.template(type);
CREATE INDEX idx_template_status ON templates.template(status);
COMMENT ON TABLE templates.template IS 'Page templates (landing/category/product/custom) composed of ordered blocks';

CREATE TABLE templates.template_block (
    id UUID PRIMARY KEY,
    template_id UUID NOT NULL REFERENCES templates.template(id) ON DELETE CASCADE,
    block_type VARCHAR(30) NOT NULL,
    block_name VARCHAR(255),
    block_order INTEGER NOT NULL,
    is_visible BOOLEAN NOT NULL DEFAULT true,
    content_json JSONB NOT NULL DEFAULT '{}'::jsonb,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);
CREATE INDEX idx_block_template ON templates.template_block(template_id, block_order);
COMMENT ON TABLE templates.template_block IS 'Ordered content blocks composing a template';

CREATE TABLE templates.template_meta (
    id UUID PRIMARY KEY,
    template_id UUID NOT NULL UNIQUE REFERENCES templates.template(id) ON DELETE CASCADE,
    page_title VARCHAR(255),
    page_description VARCHAR(1000),
    og_title VARCHAR(255),
    og_description VARCHAR(1000),
    og_image_url VARCHAR(1000),
    keywords VARCHAR(500),
    canonical_url VARCHAR(1000),
    robots_directive VARCHAR(100),
    structured_data_json JSONB,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);
COMMENT ON TABLE templates.template_meta IS 'SEO/social metadata for a template, one row per template';

CREATE TABLE templates.template_version (
    id UUID PRIMARY KEY,
    template_id UUID NOT NULL REFERENCES templates.template(id) ON DELETE CASCADE,
    version_number INTEGER NOT NULL,
    content_snapshot_json JSONB NOT NULL,
    published_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(255),
    change_note VARCHAR(1000),
    UNIQUE(template_id, version_number)
);
CREATE INDEX idx_version_template ON templates.template_version(template_id, version_number);
COMMENT ON TABLE templates.template_version IS 'Immutable snapshot of blocks+meta captured on each publish';

CREATE TABLE templates.page_content_mapping (
    id UUID PRIMARY KEY,
    template_id UUID NOT NULL REFERENCES templates.template(id) ON DELETE CASCADE,
    entity_id VARCHAR(255) NOT NULL,
    entity_type VARCHAR(30) NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'PUBLISHED',
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    published_at TIMESTAMP WITH TIME ZONE,
    UNIQUE(entity_id, entity_type)
);
CREATE INDEX idx_mapping_template ON templates.page_content_mapping(template_id);
COMMENT ON TABLE templates.page_content_mapping IS 'Which template renders a given product/category/landing page/user entity';
