# 企业级 Java AI 多业务后台架构

> 一后台支撑 N 个 AI 应用 | Spring Boot 3.4.3 + LangChain4j 1.0.0 + Spring AI 1.0.0

## 架构概览

```
┌─────────────────────────────────────────────────────────┐
│                    【前端/APP 层】                        │
│       AI装修APP  AI考古APP  AI聊天APP  AI绘图APP          │
└──────────────────────┬──────────────────────────────────┘
                       ↓
┌──────────────────────────────────────────────────────────┐
│                【网关统一入口层】  ai-gateway              │
│     鉴权(X-App-Id) · 限流(100/min) · CORS · 请求日志      │
└──────────────────────┬──────────────────────────────────┘
                       ↓
┌──────────────────────────────────────────────────────────┐
│                【业务应用层】  ai-business-starter         │
│  BusinessExtension 接口，新业务实现接口即可接入              │
└──────────────────────┬──────────────────────────────────┘
                       ↓
┌──────────────────────────────────────────────────────────┐
│              【AI 能力中台层】  ai-platform                │
│  ┌────────┐ ┌────────┐ ┌────────┐ ┌────────────────┐    │
│  │ai-llm  │ │ai-rag  │ │ai-agent│ │ai-workflow     │    │
│  │多模型  │ │检索增强│ │智能体  │ │工作流编排      │    │
│  └────────┘ └────────┘ └────────┘ └────────────────┘    │
│  ┌────────┐ ┌────────┐ ┌────────┐                       │
│  │ai-prompt│ │ai-embedding│ │ai-vector│                  │
│  │提示词  │ │向量嵌入│ │向量存储│                         │
│  └────────┘ └────────┘ └────────┘                       │
└──────────────────────┬──────────────────────────────────┘
                       ↓
┌──────────────────────────────────────────────────────────┐
│              【基础支撑层】  ai-common                     │
│  ┌────────────┐ ┌────────────┐ ┌────────────┐           │
│  │common-core │ │common-redis│ │common-storage│          │
│  │统一响应/异常│ │租户缓存   │ │OSS 文件存储 │           │
│  └────────────┘ └────────────┘ └────────────┘           │
└──────────────────────┬──────────────────────────────────┘
                       ↓
┌──────────────────────────────────────────────────────────┐
│              【多租户核心】  ai-tenant                     │
│  TenantContext(ThreadLocal) · TenantInterceptor · 配置缓存  │
└──────────────────────────────────────────────────────────┘
                       ↓
┌──────────────────────────────────────────────────────────┐
│              【数据存储层】                                │
│  PostgreSQL + pgvector  ·  Redis 7  ·  Aliyun OSS         │
└──────────────────────────────────────────────────────────┘
```

## 核心设计理念

- **业务隔离**：每个 APP 独立模型配置、独立向量表、独立提示词、独立工具集
- **能力复用**：所有 AI 引擎（LLM/RAG/Agent/Workflow）中台化，业务层只做编排
- **统一调度**：通过 `TenantContext.getAppId()` 全链路自动切换配置，无需 if/else
- **平滑扩展**：新业务只需实现 BusinessExtension 接口 + 添加配置，AI 中台零修改

## 多租户机制

```
HTTP 请求 → X-App-Id Header → TenantInterceptor → TenantContext.set(appId)
  → 全链路: ChatModelFactory / VectorStore / PromptService / ToolRegistry 自动按 appId 切换
```

## 多模型路由

所有 Provider 使用 OpenAI 兼容协议，ChatModelFactory 按 appId 动态构建模型实例：

| appId | Provider | Model | 适用场景 |
|-------|----------|-------|---------|
| chat | OpenAI | gpt-4o | 通用对话 |
| decorate | 通义千问 | qwen-max | 中文设计专长 |
| archaeology | 豆包 | doubao-pro-32k | 高性价比推理 |
| draw | 通义千问 | qwen-plus | 图像描述 |

## 模块文档索引

每个模块的详细文档（核心类、关键设计、依赖关系、配置说明）：

### 公共模块 (ai-common)

| 模块 | 文档 | 说明 |
|------|------|------|
| common-core | [docs/modules/common-core.md](ai-center/docs/modules/common-core.md) | 统一响应、异常处理、错误码、工具类 |
| common-redis | [docs/modules/common-redis.md](ai-center/docs/modules/common-redis.md) | Redis 配置 + 租户感知缓存（自动 appId 前缀） |
| common-storage | [docs/modules/common-storage.md](ai-center/docs/modules/common-storage.md) | Aliyun OSS 文件上传/预签名/删除 |

### 租户模块 (ai-tenant)

| 模块 | 文档 | 说明 |
|------|------|------|
| ai-tenant | [docs/modules/ai-tenant.md](ai-center/docs/modules/ai-tenant.md) | 多租户上下文、拦截器、配置服务（Redis 缓存 24h） |

### AI 中台模块 (ai-platform)

| 模块 | 文档 | 说明 |
|------|------|------|
| ai-llm | [docs/modules/ai-llm.md](ai-center/docs/modules/ai-llm.md) | LLM 引擎：多模型路由、同步/流式对话 |
| ai-embedding | [docs/modules/ai-embedding.md](ai-center/docs/modules/ai-embedding.md) | 向量嵌入：文本 → float[] |
| ai-vector | [docs/modules/ai-vector.md](ai-center/docs/modules/ai-vector.md) | 向量存储：pgvector 动态表 + 余弦相似度检索 |
| ai-rag | [docs/modules/ai-rag.md](ai-center/docs/modules/ai-rag.md) | RAG 引擎：Embedding → 检索 → 提示词 → LLM 生成 |
| ai-prompt | [docs/modules/ai-prompt.md](ai-center/docs/modules/ai-prompt.md) | 提示词管理：按场景模板 + {{变量}} 渲染 |
| ai-agent | [docs/modules/ai-agent.md](ai-center/docs/modules/ai-agent.md) | 智能体：LangChain4j AiServices + 记忆 + 工具调用 |
| ai-workflow | [docs/modules/ai-workflow.md](ai-center/docs/modules/ai-workflow.md) | 工作流编排：LangGraph4j（骨架） |
| ai-knowledge | [docs/modules/ai-knowledge.md](ai-center/docs/modules/ai-knowledge.md) | 知识库入库：上传→解析→分块→向量化→入库 |

### 网关模块 (ai-gateway)

| 模块 | 文档 | 说明 |
|------|------|------|
| ai-gateway | [docs/modules/ai-gateway.md](ai-center/docs/modules/ai-gateway.md) | 统一入口：鉴权、限流、CORS、日志、API 路由 |

### 业务模块 (ai-business)

| 模块 | 文档 | 说明 |
|------|------|------|
| ai-business-starter | [docs/modules/ai-business-starter.md](ai-center/docs/modules/ai-business-starter.md) | 业务扩展基座：BusinessExtension 接口 + 健康检查 |

### 应用模块 (ai-app)

| 模块 | 文档 | 说明 |
|------|------|------|
| ai-app | [docs/modules/ai-app.md](ai-center/docs/modules/ai-app.md) | Spring Boot 入口：配置、数据库迁移、Docker 编排 |

## API 端点

| 方法 | 路径 | 说明 |
|------|------|------|
| POST | /api/v1/chat | 同步对话 |
| POST | /api/v1/chat/stream | 流式对话（SSE） |
| POST | /api/v1/rag/query | RAG 检索问答 |
| POST | /api/v1/agent/run | Agent 执行（含记忆+工具） |
| DELETE | /api/v1/agent/memory/{sessionId} | 清除 Agent 会话记忆 |
| POST | /api/v1/workflow/run | 执行工作流 |
| GET | /api/v1/tenant/config | 获取租户配置 |
| PUT | /api/v1/tenant/config | 更新租户配置（热刷新模型缓存） |
| POST | /api/v1/knowledge/upload | 上传文档并同步入库 |
| DELETE | /api/v1/knowledge/document/{documentId} | 删除文档及关联向量 |
| GET | /api/v1/knowledge/documents | 列出租户所有文档 |
| GET | /api/v1/health | 健康检查 |

## 技术栈

| 类别 | 技术 | 版本 |
|------|------|------|
| 核心框架 | Spring Boot | 3.4.3 |
| AI 框架 | LangChain4j | 1.0.0 |
| AI 框架 | Spring AI | 1.0.0 |
| 工作流 | LangGraph4j | 1.0-beta5 |
| ORM | MyBatis-Plus | 3.5.9 |
| 数据库迁移 | Flyway | — |
| 向量数据库 | PostgreSQL + pgvector | pg16 |
| 缓存 | Redis | 7-alpine |
| 对象存储 | Aliyun OSS | — |
| Java | — | 21 |

## 项目结构

```
ai-center/
├── pom.xml                          # 父 POM（版本管理）
├── ai-common/
│   ├── common-core/                 # 统一响应/异常/工具
│   ├── common-redis/                # Redis + 租户缓存
│   └── common-storage/              # OSS 文件存储
├── ai-platform/
│   ├── ai-tenant/                   # 多租户核心
│   ├── ai-prompt/                   # 提示词管理
│   ├── ai-llm/                      # LLM 多模型引擎
│   ├── ai-embedding/                # 向量嵌入
│   ├── ai-vector/                   # 向量存储（pgvector）
│   ├── ai-rag/                      # RAG 检索增强
│   ├── ai-agent/                    # 智能体（LangChain4j）
│   └── ai-workflow/                 # 工作流（LangGraph4j）
│   └── ai-knowledge/                # 知识库入库（Tika+分块+Embedding）
├── ai-gateway/                      # API 网关
├── ai-business/
│   └── ai-business-starter/         # 业务扩展基座
├── ai-app/                          # Spring Boot 入口
├── docker/
│   └── docker-compose.yml           # PostgreSQL + Redis
└── docs/
    ├── modules/                     # 模块详细文档
    └── superpowers/
        ├── specs/                   # 架构设计文档
        └── plans/                   # 实施计划
```

## 当前阶段状态

- [x] 架构设计（5 层 + 2 中心）
- [x] Maven 多模块骨架（14 个模块编译通过）
- [x] 多租户核心（TenantContext + Interceptor + 配置缓存）
- [x] AI 中台引擎（LLM/RAG/Agent/Workflow/Prompt/Embedding/Vector）
- [x] 多模型适配（OpenAI/Qwen/Doubao 统一 OpenAI 协议）
- [x] LangChain4j Agent（记忆 + 工具调用）
- [x] API 网关（鉴权/限流/CORS/日志）
- [x] 知识库入库流程（上传→解析→分块→向量化→入库）
- [ ] 业务模块具体实现（装修/考古/聊天/绘图工作流）
- [ ] 向量库知识库入库流程
- [ ] 生产级记忆持久化（Redis/DB 替换 InMemory）
- [ ] LangGraph4j 工作流完整实现
