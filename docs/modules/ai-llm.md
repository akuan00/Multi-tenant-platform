# ai-llm — 大模型引擎模块

> 路径: `ai-platform/ai-llm/`

## 定位

AI 中台的 LLM 调用引擎，支持多模型路由、同步/流式对话、按租户自动切换模型配置。

## 核心类

| 类 | 职责 |
|---|---|
| `model/ChatRequest.java` | 对话请求：appId, message, systemPrompt, history(List<ChatMessage>), parameters |
| `model/ChatResponse.java` | 对话响应：content, model, promptTokens, completionTokens；静态工厂 `of(content, model)` |
| `LlmService.java` | 接口：`chat(ChatRequest)` → ChatResponse，`chatStream(ChatRequest)` → Flux<String> |
| `factory/ChatModelFactory.java` | 多模型工厂（核心）：按 appId 构建+缓存 OpenAiChatModel 实例 |
| `factory/StreamingChatModelFactory.java` | 流式模型工厂：同 ChatModelFactory 模式，构建 OpenAiStreamingChatModel |
| `impl/TenantLlmService.java` | 租户 LLM 服务：使用工厂获取模型，构建 LangChain4j 消息列表，处理流式响应 |

## 关键设计

### 多模型路由（ChatModelFactory）

```
请求 appId → 查 tenant_config → 解析 provider/model/baseUrl/apiKey
  → 构建 OpenAiChatModel（兼容 OpenAI 协议）→ 缓存 → 返回
```

- **Provider 映射**：所有 Provider 使用 OpenAI 兼容协议
  - `openai` → https://api.openai.com/v1
  - `qwen` → https://dashscope.aliyuncs.com/compatible-mode/v1
  - `doubao` → https://ark.cn-beijing.volces.com/api/v3
- **API Key 解析**：优先从 tenant_config 读取，fallback 到环境变量（OPENAI_API_KEY / DASHSCOPE_API_KEY / VOLCENGINE_API_KEY）
- **缓存**：ConcurrentHashMap 缓存模型实例，evict(appId) 支持配置热更新

### 消息构建（TenantLlmService.buildMessages）

1. 可选 SystemMessage（来自 systemPrompt 字段）
2. 历史消息（user/assistant 角色映射）
3. 当前 UserMessage

### 流式输出（chatStream）

- 使用 Reactor Flux + StreamingChatResponseHandler
- onPartialResponse → sink.next()
- onCompleteResponse → sink.complete()
- onError → sink.error()

## 当前租户模型配置

| appId | provider | model | baseUrl |
|-------|----------|-------|---------|
| chat | openai | gpt-4o | api.openai.com/v1 |
| decorate | qwen | qwen-max | dashscope.aliyuncs.com |
| archaeology | doubao | doubao-pro-32k | ark.cn-beijing.volces.com |
| draw | qwen | qwen-plus | dashscope.aliyuncs.com |

## 依赖

- common-core, ai-tenant
- langchain4j, langchain4j-open-ai
- spring-ai-starter-model-openai（Embedding 兼容）
- spring-boot-starter-webflux（流式响应）
