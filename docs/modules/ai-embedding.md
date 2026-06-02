# ai-embedding — 向量嵌入模块

> 路径: `ai-platform/ai-embedding/`

## 定位

AI 中台的文本向量化引擎，将文本转为 embedding 向量，供 RAG 检索和语义搜索使用。

## 核心类

| 类 | 职责 |
|---|---|
| `EmbeddingService.java` | 接口：`embed(text)` → float[]，`embedBatch(texts)` → List<float[]> |
| `impl/TenantEmbeddingService.java` | 基于 Spring AI EmbeddingModel 的实现 |

## 关键设计

- 使用 Spring AI 的 EmbeddingModel（而非 LangChain4j），与 ai-llm 中 Spring AI 依赖保持兼容
- 单条嵌入：`embed(text)` 返回 float[] 向量
- 批量嵌入：`embedBatch(texts)` 批量处理

## 依赖

- common-core
- spring-ai-starter-model-openai（EmbeddingModel）
