# ai-knowledge — 知识库入库模块

> 路径: `ai-platform/ai-knowledge/`

## 定位

AI 中台的知识库入库引擎，实现文档上传 → 文本提取 → 分块 → 向量化 → 入库的完整管道，使 RAG 引擎真正可用。

## 核心类

| 类 | 职责 |
|---|---|
| `KnowledgeService.java` | 接口：ingest(appId, file, title), deleteDocument(appId, documentId), listDocuments(appId) |
| `impl/KnowledgeServiceImpl.java` | 入库全流程编排：上传→解析→分块→Embedding→向量写入 |
| `parser/DocumentParser.java` | 接口：parse(inputStream, fileType) → 纯文本 |
| `parser/impl/TikaDocumentParser.java` | Apache Tika 实现，支持 PDF/DOCX/XLSX/TXT/MD/CSV |
| `chunker/TextChunker.java` | 固定大小分块（500 字符，重叠 100 字符） |
| `entity/KnowledgeDocument.java` | @TableName("knowledge_document")：appId, title, fileUrl, fileType, chunkCount, status, errorMessage |
| `entity/KnowledgeChunk.java` | @TableName("knowledge_chunk")：documentId, content, chunkIndex, appId |
| `mapper/KnowledgeDocumentMapper.java` | BaseMapper<KnowledgeDocument> |
| `mapper/KnowledgeChunkMapper.java` | BaseMapper<KnowledgeChunk> |
| `model/IngestResult.java` | 入库结果：documentId, title, chunkCount, status（COMPLETED/FAILED） |
| `model/DocumentVO.java` | 文档列表视图：id, title, fileType, chunkCount, status, createdAt |

## 入库流程

```
上传文件 → OSS 存储 → 创建文档记录(PROCESSING)
  → Tika 提取纯文本 → TextChunker 分块(500字/100重叠)
  → EmbeddingService.embedBatch() → VectorStoreService.add()
  → 更新文档记录(COMPLETED, chunkCount)
```

失败时：文档记录 status=FAILED，记录 error_message。

## 删除流程

```
删除文档 → 查询关联 chunks → 删除向量库记录 → 删除关系记录 → 删除 OSS 文件
```

## API 端点

| 方法 | 路径 | 说明 |
|------|------|------|
| POST | /api/v1/knowledge/upload | 上传文档并同步入库（multipart/form-data） |
| DELETE | /api/v1/knowledge/document/{documentId} | 删除文档及关联向量 |
| GET | /api/v1/knowledge/documents | 列出租户所有文档 |

## 关键设计

- **TikaDocumentParser**：使用 Tika AutoDetectParser 自动检测格式，无需按文件类型分发
- **TextChunker**：固定大小分块，chunkSize=500, overlap=100，确保上下文连续性
- **向量元数据**：`{"documentId": 123, "appId": "decorate", "chunkIndex": 0}`
- **状态跟踪**：PROCESSING → COMPLETED/FAILED，error_message 记录失败原因

## 依赖

- common-core, ai-tenant
- ai-embedding, ai-vector
- common-storage (OSS)
- tika-core, tika-parsers-standard-package (3.1.0)
- mybatis-plus (3.5.9)
