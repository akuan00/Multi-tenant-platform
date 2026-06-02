# Redis 记忆持久化设计文档

## 目标

将 Agent 记忆从 InMemoryChatMemoryStore 替换为 Redis，实现重启不丢失、多实例共享、自动过期。

## 架构

使用 langchain4j-redis 模块提供的 RedisChatMemoryStore，替换 AgentMemoryManager 中的 InMemoryChatMemoryStore。移除内存中的 ConcurrentHashMap 缓存层。

## 技术栈

- langchain4j-redis（LangChain4j 1.0.0 的 Redis 记忆存储模块）
- Spring Data Redis（已有 common-redis 模块）

## 变更范围

仅修改 `ai-agent` 模块：

1. **AgentMemoryManager**：InMemoryChatMemoryStore → RedisChatMemoryStore，移除 ConcurrentHashMap
2. **ai-agent/pom.xml**：新增 langchain4j-redis 依赖

## Redis 存储

- Key 格式：由 RedisChatMemoryStore 管理（基于 memoryId = `appId:sessionId`）
- TTL：24 小时自动过期
- 序列化：langchain4j-redis 内置 ChatMessage 序列化

## AgentMemoryManager 改造

```java
// 之前：InMemoryChatMemoryStore + ConcurrentHashMap 缓存
// 之后：RedisChatMemoryStore，无内存缓存

private final ChatMemoryStore memoryStore; // RedisChatMemoryStore Bean

public ChatMemory getOrCreateMemory(String appId, String sessionId) {
    String memoryKey = appId + ":" + sessionId;
    return MessageWindowChatMemory.builder()
            .id(memoryKey)
            .chatMemoryStore(memoryStore)
            .maxMessages(DEFAULT_MAX_MESSAGES)
            .build();
}
```

不再需要 ConcurrentHashMap：MessageWindowChatMemory 每次从 Redis 读取消息，无需本地缓存。
