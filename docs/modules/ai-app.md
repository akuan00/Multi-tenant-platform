# ai-app — 应用启动模块

> 路径: `ai-app/`

## 定位

Spring Boot 应用入口，聚合所有模块，包含全局配置、数据库迁移、Docker 编排。

## 核心类

| 类 | 职责 |
|---|---|
| `AiApplication.java` | @SpringBootApplication + @ComponentScan("com.company.ai") |
| `config/WebMvcConfig.java` | 注册 TenantInterceptor，拦截 /api/v1/**，排除 /health |
| `config/MyBatisPlusConfig.java` | @Configuration + @MapperScan("com.company.ai.**.mapper") |

## 配置文件

| 文件 | 说明 |
|---|---|
| `application.yml` | 主配置：Spring AI OpenAI、MyBatis-Plus、SpringDoc、MDC 日志格式 |
| `application-dev.yml` | 开发环境：PostgreSQL/Redis/Flyway/OSS 连接配置 |

## 数据库迁移

| 文件 | 说明 |
|---|---|
| `V1__init_schema.sql` | 初始化所有表 + 默认数据（4 个租户、配置、提示词模板） |
| `V2__multi_model_config.sql` | 多模型 Provider 配置（OpenAI/Qwen/Doubao） |

### V1 主要表

- `app_info`：租户应用信息
- `tenant_config`：租户配置（模型、参数等）
- `prompt_template`：提示词模板
- `conversation`：对话记录
- `knowledge_base`：知识库元数据
- `knowledge_chunk`：知识库分块

### V2 配置数据

| appId | provider | model | baseUrl |
|-------|----------|-------|---------|
| chat | openai | gpt-4o | api.openai.com/v1 |
| decorate | qwen | qwen-max | dashscope.aliyuncs.com |
| archaeology | doubao | doubao-pro-32k | ark.cn-beijing.volces.com |
| draw | qwen | qwen-plus | dashscope.aliyuncs.com |

## Docker

`docker/docker-compose.yml`：
- **PostgreSQL + pgvector**：pgvector/pgvector:pg16，端口 5432，数据库 ai_center
- **Redis**：redis:7-alpine，端口 6379

## 构建

```bash
cd ai-center
mvn clean compile          # 编译验证
mvn clean package -DskipTests  # 打包（产出 ai-app-1.0.0-SNAPSHOT.jar，约 57M）
```

## 依赖

聚合所有模块：common-core, common-redis, common-storage, ai-tenant, ai-prompt, ai-llm, ai-embedding, ai-vector, ai-rag, ai-agent, ai-workflow, ai-gateway, ai-business-starter
