-- V3: Knowledge ingestion schema updates

-- Add error_message to knowledge_document
ALTER TABLE knowledge_document ADD COLUMN IF NOT EXISTS error_message TEXT;

-- Add app_id to knowledge_chunk for direct tenant-scoped queries
ALTER TABLE knowledge_chunk ADD COLUMN IF NOT EXISTS app_id VARCHAR(64);

-- Add status constraint
ALTER TABLE knowledge_document DROP CONSTRAINT IF EXISTS chk_knowledge_doc_status;
ALTER TABLE knowledge_document ADD CONSTRAINT chk_knowledge_doc_status
    CHECK (status IN ('PROCESSING', 'COMPLETED', 'FAILED'));

-- Update existing 'processing' to uppercase for consistency
UPDATE knowledge_document SET status = 'PROCESSING' WHERE status = 'processing';

-- Backfill app_id on knowledge_chunk from knowledge_document
UPDATE knowledge_chunk kc SET app_id = kd.app_id
FROM knowledge_document kd WHERE kc.document_id = kd.id AND kc.app_id IS NULL;

-- Indexes for common queries
CREATE INDEX IF NOT EXISTS idx_knowledge_document_app_id ON knowledge_document(app_id);
CREATE INDEX IF NOT EXISTS idx_knowledge_chunk_app_id ON knowledge_chunk(app_id);
CREATE INDEX IF NOT EXISTS idx_knowledge_chunk_document_id ON knowledge_chunk(document_id);
