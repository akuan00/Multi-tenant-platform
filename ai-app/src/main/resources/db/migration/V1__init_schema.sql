-- Enable pgvector extension
CREATE EXTENSION IF NOT EXISTS vector;

-- Application/Tenant table
CREATE TABLE app_info (
    id          BIGSERIAL PRIMARY KEY,
    app_id      VARCHAR(64) NOT NULL,
    app_name    VARCHAR(128) NOT NULL,
    status      SMALLINT DEFAULT 1,
    created_at  TIMESTAMP DEFAULT NOW(),
    UNIQUE(app_id)
);

-- Tenant configuration table
CREATE TABLE tenant_config (
    id           BIGSERIAL PRIMARY KEY,
    app_id       VARCHAR(64) NOT NULL,
    config_key   VARCHAR(128) NOT NULL,
    config_value TEXT,
    config_type  VARCHAR(32) NOT NULL,
    created_at   TIMESTAMP DEFAULT NOW(),
    updated_at   TIMESTAMP DEFAULT NOW(),
    UNIQUE(app_id, config_key)
);

-- Prompt template table
CREATE TABLE prompt_template (
    id         BIGSERIAL PRIMARY KEY,
    app_id     VARCHAR(64) NOT NULL,
    scene      VARCHAR(128) NOT NULL,
    content    TEXT NOT NULL,
    variables  JSONB,
    created_at TIMESTAMP DEFAULT NOW(),
    updated_at TIMESTAMP DEFAULT NOW(),
    UNIQUE(app_id, scene)
);

-- Conversation table
CREATE TABLE conversation (
    id         BIGSERIAL PRIMARY KEY,
    app_id     VARCHAR(64) NOT NULL,
    user_id    VARCHAR(128),
    title      VARCHAR(256),
    created_at TIMESTAMP DEFAULT NOW()
);

-- Conversation message table
CREATE TABLE conversation_message (
    id              BIGSERIAL PRIMARY KEY,
    conversation_id BIGINT NOT NULL REFERENCES conversation(id) ON DELETE CASCADE,
    role            VARCHAR(32) NOT NULL,
    content         TEXT NOT NULL,
    tokens          INT,
    created_at      TIMESTAMP DEFAULT NOW()
);

-- Knowledge document table
CREATE TABLE knowledge_document (
    id          BIGSERIAL PRIMARY KEY,
    app_id      VARCHAR(64) NOT NULL,
    title       VARCHAR(256),
    file_url    VARCHAR(512),
    file_type   VARCHAR(32),
    chunk_count INT DEFAULT 0,
    status      VARCHAR(32) DEFAULT 'processing',
    created_at  TIMESTAMP DEFAULT NOW()
);

-- Knowledge chunk table
CREATE TABLE knowledge_chunk (
    id           BIGSERIAL PRIMARY KEY,
    document_id  BIGINT NOT NULL REFERENCES knowledge_document(id) ON DELETE CASCADE,
    content      TEXT NOT NULL,
    chunk_index  INT NOT NULL
);

-- Workflow definition table
CREATE TABLE workflow_definition (
    id            BIGSERIAL PRIMARY KEY,
    app_id        VARCHAR(64) NOT NULL,
    workflow_name VARCHAR(128) NOT NULL,
    graph_config  JSONB NOT NULL,
    created_at    TIMESTAMP DEFAULT NOW()
);

-- Agent definition table
CREATE TABLE agent_definition (
    id            BIGSERIAL PRIMARY KEY,
    app_id        VARCHAR(64) NOT NULL,
    agent_name    VARCHAR(128) NOT NULL,
    system_prompt TEXT,
    tool_ids      JSONB,
    created_at    TIMESTAMP DEFAULT NOW()
);

-- Insert default tenants
INSERT INTO app_info (app_id, app_name, status) VALUES ('decorate', 'AI装修', 1);
INSERT INTO app_info (app_id, app_name, status) VALUES ('archaeology', 'AI考古', 1);
INSERT INTO app_info (app_id, app_name, status) VALUES ('chat', 'AI聊天', 1);
INSERT INTO app_info (app_id, app_name, status) VALUES ('draw', 'AI绘图', 1);

-- Insert default tenant configs
INSERT INTO tenant_config (app_id, config_key, config_value, config_type) VALUES ('chat', 'llm.model', 'gpt-4o', 'MODEL');
INSERT INTO tenant_config (app_id, config_key, config_value, config_type) VALUES ('decorate', 'llm.model', 'gpt-4o', 'MODEL');
INSERT INTO tenant_config (app_id, config_key, config_value, config_type) VALUES ('archaeology', 'llm.model', 'gpt-4o', 'MODEL');
INSERT INTO tenant_config (app_id, config_key, config_value, config_type) VALUES ('chat', 'workflow.default', 'chat_workflow', 'WORKFLOW');
INSERT INTO tenant_config (app_id, config_key, config_value, config_type) VALUES ('decorate', 'workflow.default', 'decorate_workflow', 'WORKFLOW');

-- Insert default prompt templates
INSERT INTO prompt_template (app_id, scene, content, variables) VALUES ('chat', 'system', '你是一个智能助手，请用中文回答用户问题。', NULL);
INSERT INTO prompt_template (app_id, scene, content, variables) VALUES ('decorate', 'system', '你是一个专业的AI室内设计师，请根据用户需求提供装修方案。', NULL);
INSERT INTO prompt_template (app_id, scene, content, variables) VALUES ('chat', 'rag', '请基于以下参考资料回答问题：\n\n{{context}}\n\n问题：', '["context"]');
