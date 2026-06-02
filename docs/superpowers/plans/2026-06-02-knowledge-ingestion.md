# Knowledge Base Ingestion Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Implement document upload → text extraction → chunking → embedding → vector storage pipeline, making the RAG engine fully functional.

**Architecture:** New `ai-knowledge` module under `ai-platform/`, using Apache Tika for multi-format parsing, fixed-size text chunking with overlap, Spring AI EmbeddingModel for vectorization, and pgvector for storage. Sync ingestion with status tracking in `knowledge_document` / `knowledge_chunk` tables.

**Tech Stack:** Apache Tika 3.1.0, Spring AI EmbeddingModel, MyBatis-Plus, pgvector, Aliyun OSS

---

## File Structure

**Create:**
- `ai-platform/ai-knowledge/pom.xml`
- `ai-platform/ai-knowledge/src/main/java/com/company/ai/platform/knowledge/KnowledgeService.java`
- `ai-platform/ai-knowledge/src/main/java/com/company/ai/platform/knowledge/impl/KnowledgeServiceImpl.java`
- `ai-platform/ai-knowledge/src/main/java/com/company/ai/platform/knowledge/parser/DocumentParser.java`
- `ai-platform/ai-knowledge/src/main/java/com/company/ai/platform/knowledge/parser/impl/TikaDocumentParser.java`
- `ai-platform/ai-knowledge/src/main/java/com/company/ai/platform/knowledge/chunker/TextChunker.java`
- `ai-platform/ai-knowledge/src/main/java/com/company/ai/platform/knowledge/entity/KnowledgeDocument.java`
- `ai-platform/ai-knowledge/src/main/java/com/company/ai/platform/knowledge/entity/KnowledgeChunk.java`
- `ai-platform/ai-knowledge/src/main/java/com/company/ai/platform/knowledge/mapper/KnowledgeDocumentMapper.java`
- `ai-platform/ai-knowledge/src/main/java/com/company/ai/platform/knowledge/mapper/KnowledgeChunkMapper.java`
- `ai-platform/ai-knowledge/src/main/java/com/company/ai/platform/knowledge/model/IngestResult.java`
- `ai-platform/ai-knowledge/src/main/java/com/company/ai/platform/knowledge/model/DocumentVO.java`
- `ai-app/src/main/resources/db/migration/V3__knowledge_ingestion.sql`

**Modify:**
- `pom.xml` (root) — add ai-knowledge to dependencyManagement
- `ai-platform/pom.xml` — add ai-knowledge module
- `ai-platform/ai-vector/src/main/java/com/company/ai/platform/vector/VectorStoreService.java` — add delete method
- `ai-platform/ai-vector/src/main/java/com/company/ai/platform/vector/impl/PgVectorStoreService.java` — implement delete
- `ai-gateway/src/main/java/com/company/ai/gateway/controller/GatewayController.java` — add knowledge endpoints
- `ai-gateway/pom.xml` — add ai-knowledge dependency
- `ai-app/pom.xml` — add ai-knowledge dependency

---

### Task 1: Add ai-knowledge module skeleton (POM + parent registration)

**Files:**
- Create: `ai-platform/ai-knowledge/pom.xml`
- Modify: `ai-platform/pom.xml` — add `<module>ai-knowledge</module>`
- Modify: `pom.xml` (root) — add ai-knowledge to dependencyManagement
- Modify: `ai-app/pom.xml` — add ai-knowledge dependency

- [ ] **Step 1: Create ai-knowledge POM**

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 https://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>

    <parent>
        <groupId>com.company</groupId>
        <artifactId>ai-platform</artifactId>
        <version>1.0.0-SNAPSHOT</version>
    </parent>

    <artifactId>ai-knowledge</artifactId>

    <dependencies>
        <dependency>
            <groupId>com.company</groupId>
            <artifactId>common-core</artifactId>
        </dependency>
        <dependency>
            <groupId>com.company</groupId>
            <artifactId>ai-tenant</artifactId>
        </dependency>
        <dependency>
            <groupId>com.company</groupId>
            <artifactId>ai-embedding</artifactId>
        </dependency>
        <dependency>
            <groupId>com.company</groupId>
            <artifactId>ai-vector</artifactId>
        </dependency>
        <dependency>
            <groupId>com.company</groupId>
            <artifactId>common-storage</artifactId>
        </dependency>

        <!-- Apache Tika for document parsing -->
        <dependency>
            <groupId>org.apache.tika</groupId>
            <artifactId>tika-core</artifactId>
        </dependency>
        <dependency>
            <groupId>org.apache.tika</groupId>
            <artifactId>tika-parsers-standard-package</artifactId>
            <version>${tika.version}</version>
        </dependency>
    </dependencies>
</project>
```

- [ ] **Step 2: Register ai-knowledge in ai-platform parent POM**

In `ai-platform/pom.xml`, add `<module>ai-knowledge</module>` to the `<modules>` block after `ai-prompt`:

```xml
    <modules>
        <module>ai-llm</module>
        <module>ai-embedding</module>
        <module>ai-vector</module>
        <module>ai-rag</module>
        <module>ai-agent</module>
        <module>ai-workflow</module>
        <module>ai-prompt</module>
        <module>ai-knowledge</module>
    </modules>
```

- [ ] **Step 3: Add ai-knowledge to root POM dependencyManagement**

In root `pom.xml`, add after the `ai-prompt` entry in `<dependencyManagement>`:

```xml
            <dependency>
                <groupId>com.company</groupId>
                <artifactId>ai-knowledge</artifactId>
                <version>${project.version}</version>
            </dependency>
```

Also add `tika-parsers-standard-package` to dependencyManagement (tika-core is already there):

```xml
            <dependency>
                <groupId>org.apache.tika</groupId>
                <artifactId>tika-parsers-standard-package</artifactId>
                <version>${tika.version}</version>
            </dependency>
```

- [ ] **Step 4: Add ai-knowledge dependency to ai-app/pom.xml**

In `ai-app/pom.xml`, add after the `ai-prompt` dependency:

```xml
        <dependency>
            <groupId>com.company</groupId>
            <artifactId>ai-knowledge</artifactId>
        </dependency>
```

- [ ] **Step 5: Verify compilation**

Run: `cd /Users/kuan/lenghu/aiCenter-demo/ai-center && mvn clean compile -pl ai-platform/ai-knowledge -am`
Expected: BUILD SUCCESS (module will be empty but POM resolves)

- [ ] **Step 6: Commit**

```bash
git add ai-platform/ai-knowledge/pom.xml ai-platform/pom.xml pom.xml ai-app/pom.xml
git commit -m "feat: add ai-knowledge module skeleton"
```

---

### Task 2: Database migration (V3)

**Files:**
- Create: `ai-app/src/main/resources/db/migration/V3__knowledge_ingestion.sql`

- [ ] **Step 1: Create V3 migration**

The V1 migration already created `knowledge_document` and `knowledge_chunk` tables but `knowledge_chunk` is missing `app_id`. We also need `error_message` on `knowledge_document` and a status constraint.

```sql
-- V3: Knowledge ingestion schema updates

-- Add error_message to knowledge_document
ALTER TABLE knowledge_document ADD COLUMN IF NOT EXISTS error_message TEXT;

-- Add app_id to knowledge_chunk for direct tenant-scoped queries
ALTER TABLE knowledge_chunk ADD COLUMN IF NOT EXISTS app_id VARCHAR(64);

-- Add status constraint
ALTER TABLE knowledge_document DROP CONSTRAINT IF EXISTS chk_knowledge_doc_status;
ALTER TABLE knowledge_document ADD CONSTRAINT chk_knowledge_doc_status
    CHECK (status IN ('PROCESSING', 'COMPLETED', 'FAILED'));

-- Update existing 'processing' to uppercase for consistency
UPDATE knowledge_document SET status = 'PROCESSING' WHERE status = 'processing';

-- Backfill app_id on knowledge_chunk from knowledge_document
UPDATE knowledge_chunk kc SET app_id = kd.app_id
FROM knowledge_document kd WHERE kc.document_id = kd.id AND kc.app_id IS NULL;

-- Indexes for common queries
CREATE INDEX IF NOT EXISTS idx_knowledge_document_app_id ON knowledge_document(app_id);
CREATE INDEX IF NOT EXISTS idx_knowledge_chunk_app_id ON knowledge_chunk(app_id);
CREATE INDEX IF NOT EXISTS idx_knowledge_chunk_document_id ON knowledge_chunk(document_id);
```

- [ ] **Step 2: Commit**

```bash
git add ai-app/src/main/resources/db/migration/V3__knowledge_ingestion.sql
git commit -m "feat: add V3 migration for knowledge ingestion"
```

---

### Task 3: Entity and Mapper classes

**Files:**
- Create: `ai-platform/ai-knowledge/src/main/java/com/company/ai/platform/knowledge/entity/KnowledgeDocument.java`
- Create: `ai-platform/ai-knowledge/src/main/java/com/company/ai/platform/knowledge/entity/KnowledgeChunk.java`
- Create: `ai-platform/ai-knowledge/src/main/java/com/company/ai/platform/knowledge/mapper/KnowledgeDocumentMapper.java`
- Create: `ai-platform/ai-knowledge/src/main/java/com/company/ai/platform/knowledge/mapper/KnowledgeChunkMapper.java`

- [ ] **Step 1: Create KnowledgeDocument entity**

```java
package com.company.ai.platform.knowledge.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("knowledge_document")
public class KnowledgeDocument {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String appId;
    private String title;
    private String fileUrl;
    private String fileType;
    private Integer chunkCount;
    private String status;
    private String errorMessage;
    private LocalDateTime createdAt;
}
```

- [ ] **Step 2: Create KnowledgeChunk entity**

```java
package com.company.ai.platform.knowledge.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("knowledge_chunk")
public class KnowledgeChunk {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long documentId;
    private String content;
    private Integer chunkIndex;
    private String appId;
}
```

- [ ] **Step 3: Create KnowledgeDocumentMapper**

```java
package com.company.ai.platform.knowledge.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.company.ai.platform.knowledge.entity.KnowledgeDocument;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface KnowledgeDocumentMapper extends BaseMapper<KnowledgeDocument> {
}
```

- [ ] **Step 4: Create KnowledgeChunkMapper**

```java
package com.company.ai.platform.knowledge.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.company.ai.platform.knowledge.entity.KnowledgeChunk;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface KnowledgeChunkMapper extends BaseMapper<KnowledgeChunk> {
}
```

- [ ] **Step 5: Verify compilation**

Run: `cd /Users/kuan/lenghu/aiCenter-demo/ai-center && mvn clean compile -pl ai-platform/ai-knowledge -am`
Expected: BUILD SUCCESS

- [ ] **Step 6: Commit**

```bash
git add ai-platform/ai-knowledge/src/
git commit -m "feat: add knowledge entity and mapper classes"
```

---

### Task 4: DocumentParser (Tika integration)

**Files:**
- Create: `ai-platform/ai-knowledge/src/main/java/com/company/ai/platform/knowledge/parser/DocumentParser.java`
- Create: `ai-platform/ai-knowledge/src/main/java/com/company/ai/platform/knowledge/parser/impl/TikaDocumentParser.java`

- [ ] **Step 1: Create DocumentParser interface**

```java
package com.company.ai.platform.knowledge.parser;

import java.io.InputStream;

public interface DocumentParser {
    String parse(InputStream inputStream, String fileType);
}
```

- [ ] **Step 2: Create TikaDocumentParser implementation**

```java
package com.company.ai.platform.knowledge.parser.impl;

import com.company.ai.platform.knowledge.parser.DocumentParser;
import lombok.extern.slf4j.Slf4j;
import org.apache.tika.Tika;
import org.springframework.stereotype.Component;

import java.io.InputStream;

@Slf4j
@Component
public class TikaDocumentParser implements DocumentParser {

    private final Tika tika = new Tika();

    @Override
    public String parse(InputStream inputStream, String fileType) {
        try {
            return tika.parseToString(inputStream);
        } catch (Exception e) {
            log.error("Failed to parse document of type: {}", fileType, e);
            throw new RuntimeException("Document parsing failed: " + e.getMessage(), e);
        }
    }
}
```

- [ ] **Step 3: Verify compilation**

Run: `cd /Users/kuan/lenghu/aiCenter-demo/ai-center && mvn clean compile -pl ai-platform/ai-knowledge -am`
Expected: BUILD SUCCESS

- [ ] **Step 4: Commit**

```bash
git add ai-platform/ai-knowledge/src/
git commit -m "feat: add TikaDocumentParser for multi-format parsing"
```

---

### Task 5: TextChunker (fixed-size chunking)

**Files:**
- Create: `ai-platform/ai-knowledge/src/main/java/com/company/ai/platform/knowledge/chunker/TextChunker.java`

- [ ] **Step 1: Create TextChunker**

```java
package com.company.ai.platform.knowledge.chunker;

import java.util.ArrayList;
import java.util.List;

public class TextChunker {

    public static List<String> split(String text, int chunkSize, int overlap) {
        if (text == null || text.isEmpty()) {
            return List.of();
        }
        if (chunkSize <= 0) {
            throw new IllegalArgumentException("chunkSize must be positive");
        }
        if (overlap < 0 || overlap >= chunkSize) {
            throw new IllegalArgumentException("overlap must be >= 0 and < chunkSize");
        }

        List<String> chunks = new ArrayList<>();
        int step = chunkSize - overlap;
        int start = 0;

        while (start < text.length()) {
            int end = Math.min(start + chunkSize, text.length());
            String chunk = text.substring(start, end).trim();
            if (!chunk.isEmpty()) {
                chunks.add(chunk);
            }
            start += step;
            if (end == text.length()) {
                break;
            }
        }

        return chunks;
    }
}
```

- [ ] **Step 2: Verify compilation**

Run: `cd /Users/kuan/lenghu/aiCenter-demo/ai-center && mvn clean compile -pl ai-platform/ai-knowledge -am`
Expected: BUILD SUCCESS

- [ ] **Step 3: Commit**

```bash
git add ai-platform/ai-knowledge/src/
git commit -m "feat: add TextChunker with fixed-size overlap strategy"
```

---

### Task 6: Model classes (IngestResult, DocumentVO)

**Files:**
- Create: `ai-platform/ai-knowledge/src/main/java/com/company/ai/platform/knowledge/model/IngestResult.java`
- Create: `ai-platform/ai-knowledge/src/main/java/com/company/ai/platform/knowledge/model/DocumentVO.java`

- [ ] **Step 1: Create IngestResult**

```java
package com.company.ai.platform.knowledge.model;

import lombok.Data;

@Data
public class IngestResult {
    private Long documentId;
    private String title;
    private Integer chunkCount;
    private String status;

    public static IngestResult of(Long documentId, String title, Integer chunkCount, String status) {
        IngestResult result = new IngestResult();
        result.setDocumentId(documentId);
        result.setTitle(title);
        result.setChunkCount(chunkCount);
        result.setStatus(status);
        return result;
    }
}
```

- [ ] **Step 2: Create DocumentVO**

```java
package com.company.ai.platform.knowledge.model;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class DocumentVO {
    private Long id;
    private String title;
    private String fileType;
    private Integer chunkCount;
    private String status;
    private LocalDateTime createdAt;
}
```

- [ ] **Step 3: Verify compilation**

Run: `cd /Users/kuan/lenghu/aiCenter-demo/ai-center && mvn clean compile -pl ai-platform/ai-knowledge -am`
Expected: BUILD SUCCESS

- [ ] **Step 4: Commit**

```bash
git add ai-platform/ai-knowledge/src/
git commit -m "feat: add IngestResult and DocumentVO models"
```

---

### Task 7: VectorStoreService — add delete method

**Files:**
- Modify: `ai-platform/ai-vector/src/main/java/com/company/ai/platform/vector/VectorStoreService.java`
- Modify: `ai-platform/ai-vector/src/main/java/com/company/ai/platform/vector/impl/PgVectorStoreService.java`

- [ ] **Step 1: Add delete method to VectorStoreService interface**

Add to the existing interface after `ensureCollection`:

```java
    void delete(String appId, List<Long> chunkIds);
```

The full interface becomes:

```java
package com.company.ai.platform.vector;

import com.company.ai.platform.vector.model.VectorDocument;
import com.company.ai.platform.vector.model.VectorSearchResult;
import java.util.List;

public interface VectorStoreService {
    void add(String appId, List<VectorDocument> docs);
    List<VectorSearchResult> search(String appId, float[] query, int topK);
    void ensureCollection(String appId, int dimension);
    void delete(String appId, List<Long> chunkIds);
}
```

- [ ] **Step 2: Implement delete in PgVectorStoreService**

Add this method to the existing class after `search`:

```java
    @Override
    public void delete(String appId, List<Long> chunkIds) {
        if (chunkIds == null || chunkIds.isEmpty()) {
            return;
        }
        String tableName = "vec_" + appId;
        String placeholders = String.join(",", chunkIds.stream().map(id -> "?").toList());
        jdbcTemplate.update(
                "DELETE FROM " + tableName + " WHERE chunk_id IN (" + placeholders + ")",
                chunkIds.toArray());
        log.info("Deleted {} chunks from {}", chunkIds.size(), tableName);
    }
```

- [ ] **Step 3: Verify compilation**

Run: `cd /Users/kuan/lenghu/aiCenter-demo/ai-center && mvn clean compile`
Expected: BUILD SUCCESS

- [ ] **Step 4: Commit**

```bash
git add ai-platform/ai-vector/src/
git commit -m "feat: add delete method to VectorStoreService for knowledge cleanup"
```

---

### Task 8: KnowledgeService interface and implementation

**Files:**
- Create: `ai-platform/ai-knowledge/src/main/java/com/company/ai/platform/knowledge/KnowledgeService.java`
- Create: `ai-platform/ai-knowledge/src/main/java/com/company/ai/platform/knowledge/impl/KnowledgeServiceImpl.java`

- [ ] **Step 1: Create KnowledgeService interface**

```java
package com.company.ai.platform.knowledge;

import com.company.ai.platform.knowledge.model.DocumentVO;
import com.company.ai.platform.knowledge.model.IngestResult;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface KnowledgeService {
    IngestResult ingest(String appId, MultipartFile file, String title);
    void deleteDocument(String appId, Long documentId);
    List<DocumentVO> listDocuments(String appId);
}
```

- [ ] **Step 2: Create KnowledgeServiceImpl**

```java
package com.company.ai.platform.knowledge.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.company.ai.common.core.exception.BizException;
import com.company.ai.common.core.result.ResultCode;
import com.company.ai.common.storage.service.OssStorageService;
import com.company.ai.platform.embedding.EmbeddingService;
import com.company.ai.platform.knowledge.KnowledgeService;
import com.company.ai.platform.knowledge.chunker.TextChunker;
import com.company.ai.platform.knowledge.entity.KnowledgeChunk;
import com.company.ai.platform.knowledge.entity.KnowledgeDocument;
import com.company.ai.platform.knowledge.mapper.KnowledgeChunkMapper;
import com.company.ai.platform.knowledge.mapper.KnowledgeDocumentMapper;
import com.company.ai.platform.knowledge.model.DocumentVO;
import com.company.ai.platform.knowledge.model.IngestResult;
import com.company.ai.platform.knowledge.parser.DocumentParser;
import com.company.ai.platform.vector.VectorStoreService;
import com.company.ai.platform.vector.model.VectorDocument;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class KnowledgeServiceImpl implements KnowledgeService {

    private final KnowledgeDocumentMapper documentMapper;
    private final KnowledgeChunkMapper chunkMapper;
    private final DocumentParser documentParser;
    private final EmbeddingService embeddingService;
    private final VectorStoreService vectorStoreService;
    private final OssStorageService ossStorageService;

    private static final int CHUNK_SIZE = 500;
    private static final int OVERLAP = 100;

    @Override
    public IngestResult ingest(String appId, MultipartFile file, String title) {
        // Step 1: Upload file to OSS
        String fileUrl;
        try {
            fileUrl = ossStorageService.upload(file, "knowledge", appId);
        } catch (Exception e) {
            log.error("Failed to upload file for appId={}", appId, e);
            throw new RuntimeException("File upload failed", e);
        }

        // Step 2: Create document record (PROCESSING)
        String fileType = extractFileType(file.getOriginalFilename());
        String docTitle = (title != null && !title.isBlank()) ? title : file.getOriginalFilename();

        KnowledgeDocument doc = new KnowledgeDocument();
        doc.setAppId(appId);
        doc.setTitle(docTitle);
        doc.setFileUrl(fileUrl);
        doc.setFileType(fileType);
        doc.setChunkCount(0);
        doc.setStatus("PROCESSING");
        documentMapper.insert(doc);

        // Step 3-8: Parse, chunk, embed, store
        try {
            String text = documentParser.parse(file.getInputStream(), fileType);

            List<String> chunks = TextChunker.split(text, CHUNK_SIZE, OVERLAP);
            if (chunks.isEmpty()) {
                doc.setStatus("COMPLETED");
                doc.setChunkCount(0);
                documentMapper.updateById(doc);
                return IngestResult.of(doc.getId(), doc.getTitle(), 0, "COMPLETED");
            }

            List<float[]> embeddings = embeddingService.embedBatch(chunks);

            // Ensure vector collection exists (dimension from first embedding)
            int dimension = embeddings.get(0).length;
            vectorStoreService.ensureCollection(appId, dimension);

            // Save chunks to relational DB and vector store
            for (int i = 0; i < chunks.size(); i++) {
                KnowledgeChunk chunk = new KnowledgeChunk();
                chunk.setDocumentId(doc.getId());
                chunk.setContent(chunks.get(i));
                chunk.setChunkIndex(i);
                chunk.setAppId(appId);
                chunkMapper.insert(chunk);

                VectorDocument vectorDoc = new VectorDocument();
                vectorDoc.setChunkId(chunk.getId());
                vectorDoc.setContent(chunks.get(i));
                vectorDoc.setEmbedding(embeddings.get(i));
                vectorDoc.setMetadata(Map.of(
                        "documentId", doc.getId(),
                        "appId", appId,
                        "chunkIndex", i
                ));
                vectorStoreService.add(appId, List.of(vectorDoc));
            }

            doc.setStatus("COMPLETED");
            doc.setChunkCount(chunks.size());
            documentMapper.updateById(doc);

            return IngestResult.of(doc.getId(), doc.getTitle(), chunks.size(), "COMPLETED");

        } catch (Exception e) {
            log.error("Ingestion failed for document id={}, appId={}", doc.getId(), appId, e);
            doc.setStatus("FAILED");
            doc.setErrorMessage(e.getMessage());
            documentMapper.updateById(doc);
            return IngestResult.of(doc.getId(), doc.getTitle(), 0, "FAILED");
        }
    }

    @Override
    @Transactional
    public void deleteDocument(String appId, Long documentId) {
        KnowledgeDocument doc = documentMapper.selectById(documentId);
        if (doc == null || !doc.getAppId().equals(appId)) {
            throw new BizException(ResultCode.KNOWLEDGE_NOT_FOUND);
        }

        // Get chunk IDs for vector deletion
        List<KnowledgeChunk> chunks = chunkMapper.selectList(
                new LambdaQueryWrapper<KnowledgeChunk>().eq(KnowledgeChunk::getDocumentId, documentId));

        if (!chunks.isEmpty()) {
            List<Long> chunkIds = chunks.stream().map(KnowledgeChunk::getId).toList();
            vectorStoreService.delete(appId, chunkIds);
        }

        // Delete chunks and document (CASCADE would also work, but explicit is clearer)
        chunkMapper.delete(new LambdaQueryWrapper<KnowledgeChunk>().eq(KnowledgeChunk::getDocumentId, documentId));
        documentMapper.deleteById(documentId);

        // Delete file from OSS
        if (doc.getFileUrl() != null) {
            try {
                ossStorageService.delete(doc.getFileUrl());
            } catch (Exception e) {
                log.warn("Failed to delete OSS file: {}", doc.getFileUrl(), e);
            }
        }
    }

    @Override
    public List<DocumentVO> listDocuments(String appId) {
        List<KnowledgeDocument> docs = documentMapper.selectList(
                new LambdaQueryWrapper<KnowledgeDocument>()
                        .eq(KnowledgeDocument::getAppId, appId)
                        .orderByDesc(KnowledgeDocument::getCreatedAt));

        return docs.stream().map(doc -> {
            DocumentVO vo = new DocumentVO();
            vo.setId(doc.getId());
            vo.setTitle(doc.getTitle());
            vo.setFileType(doc.getFileType());
            vo.setChunkCount(doc.getChunkCount());
            vo.setStatus(doc.getStatus());
            vo.setCreatedAt(doc.getCreatedAt());
            return vo;
        }).toList();
    }

    private String extractFileType(String filename) {
        if (filename == null || !filename.contains(".")) {
            return "unknown";
        }
        return filename.substring(filename.lastIndexOf(".") + 1).toLowerCase();
    }
}
```

- [ ] **Step 3: Verify compilation**

Run: `cd /Users/kuan/lenghu/aiCenter-demo/ai-center && mvn clean compile -pl ai-platform/ai-knowledge -am`
Expected: BUILD SUCCESS

- [ ] **Step 4: Commit**

```bash
git add ai-platform/ai-knowledge/src/
git commit -m "feat: add KnowledgeService with ingest, delete, list operations"
```

---

### Task 9: GatewayController — add knowledge endpoints

**Files:**
- Modify: `ai-gateway/src/main/java/com/company/ai/gateway/controller/GatewayController.java`
- Modify: `ai-gateway/pom.xml` — add ai-knowledge dependency

- [ ] **Step 1: Add ai-knowledge dependency to ai-gateway POM**

In `ai-gateway/pom.xml`, add after the existing `ai-workflow` dependency:

```xml
        <dependency>
            <groupId>com.company</groupId>
            <artifactId>ai-knowledge</artifactId>
        </dependency>
```

- [ ] **Step 2: Add knowledge endpoints to GatewayController**

Add the import at the top of the file:

```java
import com.company.ai.platform.knowledge.KnowledgeService;
import com.company.ai.platform.knowledge.model.DocumentVO;
import com.company.ai.platform.knowledge.model.IngestResult;
import org.springframework.web.multipart.MultipartFile;
```

Add the service field:

```java
    private final KnowledgeService knowledgeService;
```

Add three endpoint methods after the `updateTenantConfig` method:

```java
    @PostMapping("/knowledge/upload")
    public R<IngestResult> knowledgeUpload(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "title", required = false) String title) {
        String appId = TenantContext.getAppId();
        return R.ok(knowledgeService.ingest(appId, file, title));
    }

    @DeleteMapping("/knowledge/document/{documentId}")
    public R<Void> deleteKnowledgeDocument(@PathVariable Long documentId) {
        String appId = TenantContext.getAppId();
        knowledgeService.deleteDocument(appId, documentId);
        return R.ok();
    }

    @GetMapping("/knowledge/documents")
    public R<java.util.List<DocumentVO>> listKnowledgeDocuments() {
        String appId = TenantContext.getAppId();
        return R.ok(knowledgeService.listDocuments(appId));
    }
```

- [ ] **Step 3: Verify full project compilation**

Run: `cd /Users/kuan/lenghu/aiCenter-demo/ai-center && mvn clean compile`
Expected: BUILD SUCCESS

- [ ] **Step 4: Commit**

```bash
git add ai-gateway/pom.xml ai-gateway/src/
git commit -m "feat: add knowledge management endpoints to gateway"
```

---

### Task 10: Add mybatis-plus dependency to ai-knowledge POM

The KnowledgeServiceImpl uses MyBatis-Plus (`BaseMapper`, `LambdaQueryWrapper`), so ai-knowledge needs the dependency.

**Files:**
- Modify: `ai-platform/ai-knowledge/pom.xml`

- [ ] **Step 1: Add mybatis-plus dependency**

Add to the `<dependencies>` block in `ai-platform/ai-knowledge/pom.xml`:

```xml
        <dependency>
            <groupId>com.baomidou</groupId>
            <artifactId>mybatis-plus-spring-boot3-starter</artifactId>
        </dependency>
```

- [ ] **Step 2: Verify compilation**

Run: `cd /Users/kuan/lenghu/aiCenter-demo/ai-center && mvn clean compile`
Expected: BUILD SUCCESS

- [ ] **Step 3: Commit**

```bash
git add ai-platform/ai-knowledge/pom.xml
git commit -m "fix: add mybatis-plus dependency to ai-knowledge"
```

---

### Task 11: Full build verification and package

- [ ] **Step 1: Clean compile all modules**

Run: `cd /Users/kuan/lenghu/aiCenter-demo/ai-center && mvn clean compile`
Expected: BUILD SUCCESS

- [ ] **Step 2: Package with skipTests**

Run: `cd /Users/kuan/lenghu/aiCenter-demo/ai-center && mvn clean package -DskipTests`
Expected: BUILD SUCCESS, ai-app JAR generated

- [ ] **Step 3: Commit any remaining fixes if needed**

If any compilation issues were fixed during verification, commit them.

---

### Task 12: Update documentation

**Files:**
- Create: `docs/modules/ai-knowledge.md`
- Modify: `AGENTS.md` — add ai-knowledge to module index

- [ ] **Step 1: Create ai-knowledge module doc**

```markdown
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
| `entity/KnowledgeDocument.java` | @TableName("knowledge_document") |
| `entity/KnowledgeChunk.java` | @TableName("knowledge_chunk") |
| `mapper/KnowledgeDocumentMapper.java` | BaseMapper<KnowledgeDocument> |
| `mapper/KnowledgeChunkMapper.java` | BaseMapper<KnowledgeChunk> |
| `model/IngestResult.java` | 入库结果：documentId, title, chunkCount, status |
| `model/DocumentVO.java` | 文档列表视图：id, title, fileType, chunkCount, status, createdAt |

## 入库流程

```
上传文件 → OSS 存储 → 创建文档记录(PROCESSING)
  → Tika 提取纯文本 → TextChunker 分块(500字/100重叠)
  → EmbeddingService.embedBatch() → VectorStoreService.add()
  → 更新文档记录(COMPLETED, chunkCount)
```

失败时：文档记录 status=FAILED，记录 error_message。

## 依赖

- common-core, ai-tenant
- ai-embedding, ai-vector
- common-storage (OSS)
- tika-core, tika-parsers-standard-package
- mybatis-plus
```

- [ ] **Step 2: Update AGENTS.md module index**

Add ai-knowledge row to the AI 中台模块 table and update the project structure tree.

- [ ] **Step 3: Commit**

```bash
git add docs/modules/ai-knowledge.md AGENTS.md
git commit -m "docs: add ai-knowledge module documentation"
```
