# ai-agent — 智能体模块

> 路径: `ai-platform/ai-agent/`

## 定位

AI 中台的智能体引擎，基于 LangChain4j AiServices 实现，支持对话记忆、工具调用、按租户绑定工具集。

## 核心类

| 类 | 职责 |
|---|---|
| `model/AgentRequest.java` | Agent 请求：appId, message, agentName, sessionId, context, toolIds |
| `model/AgentResponse.java` | Agent 响应：content, toolExecutions, sessionId, memoryMessageCount |
| `AgentService.java` | 接口：execute(request), clearMemory(appId, sessionId) |
| `impl/LangChain4jAgentService.java` | LangChain4j 实现：构建 AiServices + Memory + Tools |
| `tool/ToolRegistry.java` | 工具注册中心：ConcurrentHashMap<String, Object>，按 appId 绑定工具实例 |
| `tool/DefaultTools.java` | 默认工具集：@Tool 注解的方法 |
| `memory/AgentMemoryManager.java` | 记忆管理器：InMemoryChatMemoryStore + MessageWindowChatMemory |

## 关键设计

### LangChain4j AiServices 构建

```java
Agent agent = AiServices.builder(Agent.class)
    .chatModel(chatModel)
    .chatMemory(chatMemory)
    .tools(toolInstances)
    .systemMessageProvider(msg -> SystemMessage.from(systemPrompt))
    .build();
```

- **Agent 内部接口**：`interface Agent { String chat(String message); }`

### 对话记忆（AgentMemoryManager）

- 存储后端：InMemoryChatMemoryStore（生产环境可替换为 Redis/DB）
- 窗口策略：MessageWindowChatMemory（20 条消息滑动窗口）
- 记忆 key：`{appId}:{sessionId}`
- **clearMemory(appId, sessionId)**：清除指定会话记忆

### 工具调用（ToolRegistry + DefaultTools）

**ToolRegistry**：
- `registerTool(appId, toolInstance)`：为租户注册工具
- `getTools(appId, toolIds)`：获取租户指定工具列表
- 未指定 toolIds 时返回该租户所有注册工具

**DefaultTools**（内置默认工具）：
| @Tool 方法 | 功能 |
|---|---|
| `getCurrentDateTime()` | 获取当前日期时间 |
| `calculate(expression)` | 执行数学表达式计算 |
| `generateUUID()` | 生成随机 UUID |

### 多租户隔离

- 每个租户独立 ChatModel（通过 ChatModelFactory）
- 每个租户独立记忆空间（appId:sessionId 隔离）
- 每个租户独立工具集（ToolRegistry 按 appId 绑定）

## 依赖

- common-core, ai-tenant
- ai-llm（ChatModelFactory）
- ai-prompt（系统提示词）
- langchain4j, langchain4j-open-ai
