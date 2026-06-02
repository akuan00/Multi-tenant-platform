# ai-gateway — API 网关模块

> 路径: `ai-gateway/`

## 定位

统一 API 入口，负责鉴权、限流、CORS、日志记录，以及所有 API 端点的路由。

## 核心类

### 过滤器

| 类 | 职责 |
|---|---|
| `filter/CorsFilter.java` | @Order(HIGHEST_PRECEDENCE)，处理跨域请求 |
| `filter/AuthFilter.java` | 验证 X-App-Id 请求头，校验 app_info 表中是否存在该 appId |
| `filter/RateLimitFilter.java` | Redis 限流：100 req/60s per appId |
| `filter/LogFilter.java` | 记录请求日志：method, URI, appId, duration, status |

### 控制器

| 类 | 端点 | 说明 |
|---|---|---|
| `controller/GatewayController.java` | POST /api/v1/chat | 同步对话 |
| | POST /api/v1/chat/stream | 流式对话（SSE） |
| | POST /api/v1/rag/query | RAG 检索问答 |
| | POST /api/v1/agent/run | Agent 执行 |
| | DELETE /api/v1/agent/memory/{sessionId} | 清除 Agent 会话记忆 |
| | POST /api/v1/workflow/run | 执行工作流 |
| | GET /api/v1/tenant/config | 获取租户配置 |
| | PUT /api/v1/tenant/config | 更新租户配置（触发模型缓存刷新） |

## 关键设计

### 鉴权流程

```
请求 → AuthFilter → 校验 X-App-Id 头 → 查询 app_info 表
  → 不存在: 返回 401 → 存在: 放行 + TenantContext 设置 appId
```

白名单路径：`/health`, `/swagger-ui`, `/v3/api-docs`

### 限流策略

- Redis key：`rate_limit:{appId}`
- 窗口：60 秒
- 上限：100 次请求
- 超限返回 429 Too Many Requests

### 配置热更新

PUT /tenant/config 更新 `llm.*` 开头的配置时，自动调用 `chatModelFactory.evict(appId)` 刷新模型缓存。

## 依赖

- common-core, ai-tenant
- ai-llm, ai-rag, ai-agent, ai-workflow
- spring-boot-starter-web, spring-boot-starter-data-redis
