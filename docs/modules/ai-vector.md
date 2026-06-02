# ai-vector — 向量存储模块

> 路径: `ai-platform/ai-vector/`

## 定位

AI 中台的向量存储引擎，基于 PostgreSQL + pgvector 实现，按租户隔离 Collection，支持动态建表和余弦相似度检索。

## 核心类

| 类 | 职责 |
|---|---|
| `model/VectorDocument.java` | 向量文档：chunkId, content, embedding(float[]), metadata(Map) |
| `model/VectorSearchResult.java` | 检索结果：chunkId, content, similarity, metadata |
| `VectorStoreService.java` | 接口：add(appId, docs), search(appId, query, topK), ensureCollection(appId, dimension) |
| `impl/PgVectorStoreService.java` | pgvector 实现：动态建表、ivfflat 索引、余弦相似度搜索 |

## 关键设计

### 动态表创建

- 每个租户自动创建独立表：`vec_{appId}`（如 vec_decorate, vec_archaeology）
- 表结构：id, chunk_id, content, embedding(vector({dimension})), metadata(jsonb), created_at
- 自动创建 ivfflat 索引（当数据量 > 100 时）加速相似度搜索

### 余弦相似度搜索

```sql
SELECT chunk_id, content, metadata,
       1 - (embedding <=> :queryVector) AS similarity
FROM vec_{appId}
ORDER BY embedding <=> :queryVector
LIMIT :topK
```

### 多租户隔离

- 每个 appId 独立表，完全隔离数据
- `ensureCollection(appId, dimension)` 幂等操作，表不存在则创建

## 依赖

- common-core
- spring-boot-starter-jdbc（JdbcTemplate）
- pgvector 驱动
