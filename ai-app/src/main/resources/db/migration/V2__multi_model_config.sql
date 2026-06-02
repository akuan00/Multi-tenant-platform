-- Add multi-model provider configuration for each tenant

-- Chat app: uses OpenAI GPT-4o
INSERT INTO tenant_config (app_id, config_key, config_value, config_type) VALUES ('chat', 'llm.provider', 'openai', 'MODEL');
INSERT INTO tenant_config (app_id, config_key, config_value, config_type) VALUES ('chat', 'llm.model', 'gpt-4o', 'MODEL');
INSERT INTO tenant_config (app_id, config_key, config_value, config_type) VALUES ('chat', 'llm.temperature', '0.7', 'MODEL');
INSERT INTO tenant_config (app_id, config_key, config_value, config_type) VALUES ('chat', 'llm.baseUrl', 'https://api.openai.com/v1', 'MODEL');

-- Decorate app: uses Qwen (通义千问) for Chinese design expertise
INSERT INTO tenant_config (app_id, config_key, config_value, config_type) VALUES ('decorate', 'llm.provider', 'qwen', 'MODEL');
INSERT INTO tenant_config (app_id, config_key, config_value, config_type) VALUES ('decorate', 'llm.model', 'qwen-max', 'MODEL');
INSERT INTO tenant_config (app_id, config_key, config_value, config_type) VALUES ('decorate', 'llm.baseUrl', 'https://dashscope.aliyuncs.com/compatible-mode/v1', 'MODEL');
INSERT INTO tenant_config (app_id, config_key, config_value, config_type) VALUES ('decorate', 'llm.temperature', '0.8', 'MODEL');

-- Archaeology app: uses Doubao (豆包) for cost-effective reasoning
INSERT INTO tenant_config (app_id, config_key, config_value, config_type) VALUES ('archaeology', 'llm.provider', 'doubao', 'MODEL');
INSERT INTO tenant_config (app_id, config_key, config_value, config_type) VALUES ('archaeology', 'llm.model', 'doubao-pro-32k', 'MODEL');
INSERT INTO tenant_config (app_id, config_key, config_value, config_type) VALUES ('archaeology', 'llm.baseUrl', 'https://ark.cn-beijing.volces.com/api/v3', 'MODEL');
INSERT INTO tenant_config (app_id, config_key, config_value, config_type) VALUES ('archaeology', 'llm.temperature', '0.7', 'MODEL');

-- Draw app: uses Qwen for image description
INSERT INTO tenant_config (app_id, config_key, config_value, config_type) VALUES ('draw', 'llm.provider', 'qwen', 'MODEL');
INSERT INTO tenant_config (app_id, config_key, config_value, config_type) VALUES ('draw', 'llm.model', 'qwen-plus', 'MODEL');
INSERT INTO tenant_config (app_id, config_key, config_value, config_type) VALUES ('draw', 'llm.baseUrl', 'https://dashscope.aliyuncs.com/compatible-mode/v1', 'MODEL');
INSERT INTO tenant_config (app_id, config_key, config_value, config_type) VALUES ('draw', 'llm.temperature', '0.9', 'MODEL');
