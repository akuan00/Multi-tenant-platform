# 知识库入库流程设计文档

## 目标

实现文档上传 → 文本提取 → 分块 → 向量化 → 入库的完整管道，使 RAG 引擎真正可用。

## 架构

新增 `ai-platform/ai-knowledge` 模块，与 ai-rag（查询侧）形成入库/查询对称关系。使用 Apache Tika 统一解析多种文件格式，固定大小分块策略，同步入库。

## 技术栈

- Apache Tika 2.9.x（文件解析：PDF/DOCX/XLSX/TXT/MD/CSV）
- LangChain4j Document Splitters（分块参考，实际用自定义固定大小分块）
- 现有：ai-embedding（Spring AI EmbeddingModel）、ai-vector（pgvector）、common-storage（OSS）

---

## 模块结构

```
ai-knowledge/src/main/java/com/company/ai/platform/knowledge/
├── KnowledgeService.java              # 接口
├── impl/KnowledgeServiceImpl.java     # 入库全流程编排
├── parser/DocumentParser.java         # 接口：提取文本
├── parser/impl/TikaDocumentParser.java # Tika 实现
├── chunker/TextChunker.java           # 固定大小分块
├── entity/KnowledgeDocument.java      # MyBatis-Plus 实体
├── entity/KnowledgeChunk.java         # MyBatis-Plus 实体
├── mapper/KnowledgeDocumentMapper.java
├── mapper/KnowledgeChunkMapper.java
└── model/
    ├── IngestRequest.java             # 入库请求
    ├── IngestResult.java              # 入库结果
    └── DocumentVO.java                # 文档列表视图
```

## 依赖

```xml
ai-knowledge 依赖：
  ├── common-core
  ├── ai-tenant
  ├── ai-embedding
  ├── ai-vector
  ├── common-storage
  ├── tika-core (2.9.x)
  ├── tika-parsers-standard-package (2.9.x)
  └── mybatis-plus (继承自 parent)
```

## 入库流程

```
1. 上传文件 → OssStorageService.upload(file, "knowledge", appId) → 获得 objectKey
2. 创建 knowledge_document 记录（status=PROCESSING）
3. 从文件流 → TikaDocumentParser.parse(inputStream, fileType) → 提取纯文本
4. TextChunker.split(text, chunkSize=500, overlap=100) → List<String> chunks
5. EmbeddingService.embedBatch(chunks) → List<float[]> embeddings
6. 批量写入 knowledge_chunk 表（chunk_index, content, document_id, app_id）
7. 组装 VectorDocument 列表 → VectorStoreService.add(appId, docs)
8. 更新 knowledge_document：status=COMPLETED, chunk_count=N
9. 返回 IngestResult(documentId, chunkCount, status)
```

异常处理：
- 任何步骤失败 → knowledge_document.status=FAILED，记录 error_message
- 向量写入失败 → 回滚 knowledge_chunk 关系数据

## DocumentParser

接口：
```java
public interface DocumentParser {
    String parse(InputStream inputStream, String fileType);
}
```

TikaDocumentParser 实现：
- 使用 Tika AutoDetectParser 自动检测文件格式
- 支持：PDF, DOCX, XLSX, TXT, Markdown, CSV
- 无需按文件类型分发，Tika 统一处理
- 提取纯文本，去除格式/图片/表格标记

## TextChunker

固定大小分块策略：
- chunkSize：500 字符（可配置）
- overlap：100 字符（可配置）
- 按字符数切割，overlap 确保上下文连续性
- 返回 `List<String>`，每个元素是一个 chunk

```java
public class TextChunker {
    public static List<String> split(String text, int chunkSize, int overlap) {
        // 按 chunkSize 切割，每段起始位置偏移 chunkSize - overlap
    }
}
```

## VectorDocument 元数据

每个 chunk 写入向量库时携带 metadata：
```json
{
  "documentId": 123,
  "appId": "decorate",
  "chunkIndex": 0
}
```

chunkId 使用 knowledge_chunk 表的自增 id。

## 删除流程

`deleteDocument(appId, documentId)`：
1. 查询 knowledge_chunk 获取所有 chunkId
2. 从 vec_{appId} 删除对应向量（需给 VectorStoreService 增加 delete 方法）
3. 删除 knowledge_chunk 记录
4. 删除 knowledge_document 记录

## API 端点

GatewayController 新增：

| 方法 | 路径 | 说明 |
|------|------|------|
| POST | /api/v1/knowledge/upload | 上传文档并同步入库（multipart/form-data） |
| DELETE | /api/v1/knowledge/document/{documentId} | 删除文档及关联向量 |
| GET | /api/v1/knowledge/documents | 列出租户所有文档 |

### 请求/响应模型

**IngestRequest**（upload 参数）：
- `file`：MultipartFile（必填）
- `title`：String（可选，默认用文件名）

**IngestResult**（入库响应）：
- `documentId`：Long
- `title`：String
- `chunkCount`：int
- `status`：String（COMPLETED / FAILED）

**DocumentVO**（文档列表项）：
- `id`：Long
- `title`：String
- `fileType`：String
- `chunkCount`：int
- `status`：String
- `createdAt`：LocalDateTime

## 数据库变更（V3 迁移脚本）

```sql
-- V1 遗漏了 app_id 列，补充
ALTER TABLE knowledge_document ADD COLUMN IF NOT EXISTS app_id VARCHAR(50);
ALTER TABLE knowledge_chunk ADD COLUMN IF NOT EXISTS app_id VARCHAR(50);

-- 索引
CREATE INDEX IF NOT EXISTS idx_knowledge_document_app_id ON knowledge_document(app_id);
CREATE INDEX IF NOT EXISTS idx_knowledge_chunk_app_id ON knowledge_chunk(app_id);

-- 失败原因记录
ALTER TABLE knowledge_document ADD COLUMN IF NOT EXISTS error_message TEXT;

-- status 合法值约束
ALTER TABLE knowledge_document ADD CONSTRAINT chk_knowledge_doc_status
  CHECK (status IN ('PROCESSING', 'COMPLETED', 'FAILED'));
```

## VectorStoreService 变更

新增方法：
```java
void delete(String appId, List<Long> chunkIds);
```

PgVectorStoreService 实现：
```java
// DELETE FROM vec_{appId} WHERE chunk_id IN (...)
```
