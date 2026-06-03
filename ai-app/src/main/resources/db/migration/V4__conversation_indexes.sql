-- V4: Conversation indexes and schema enhancements

-- Add updated_at column for conversation sorting
ALTER TABLE conversation ADD COLUMN IF NOT EXISTS updated_at TIMESTAMP;

-- Initialize updated_at from created_at for existing rows
UPDATE conversation SET updated_at = created_at WHERE updated_at IS NULL;

-- Indexes for common query patterns
CREATE INDEX IF NOT EXISTS idx_conversation_app_id ON conversation(app_id);
CREATE INDEX IF NOT EXISTS idx_conversation_app_user ON conversation(app_id, user_id);
CREATE INDEX IF NOT EXISTS idx_conversation_updated_at ON conversation(updated_at DESC);
CREATE INDEX IF NOT EXISTS idx_conv_msg_conversation_id ON conversation_message(conversation_id);
