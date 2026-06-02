# ai-rag — RAG 检索增强生成模块

> 路径: `ai-platform/ai-rag/`

## 定位

AI 中台的 RAG 引擎，组合 embedding → 向量检索 → 提示词渲染 → LLM 生成，实现知识库增强问答。

## 核心类

| 类 | 职责 |
|---|---|
| `model/RagQuery.java` | RAG 查询：question, topK(默认5) |
| `model/RagResult.java` | RAG 结果：answer, sources(List<SourceReference>) |
| `model/SourceReference.java` | 引用来源：chunkId, content, similarity |
| `RagService.java` | 接口：`query(appId, ragQuery)` → RagResult |
| `impl/DefaultRagService.java` | 默认实现：完整 RAG Pipeline |

## 关键设计

### RAG Pipeline 流程

```
用户问题 → EmbeddingService.embed() → VectorStoreService.search()
  → 拼接上下文 → PromptService.renderPrompt("rag", {question, context})
  → LlmService.chat() → 返回 RagResult(answer + sources)
```

1. **Embedding**：将用户问题向量化
2. **检索**：在租户专属向量表中检索 topK 相似文档
3. **提示词渲染**：使用 rag 场景模板，将检索结果作为上下文注入
4. **LLM 生成**：基于增强提示词生成回答

### 多租户隔离

- appId 贯穿全流程，确保 embedding、向量检索、提示词、LLM 配置均按租户切换

## 依赖

- common-core, ai-tenant
- ai-embedding, ai-vector, ai-prompt, ai-llm
