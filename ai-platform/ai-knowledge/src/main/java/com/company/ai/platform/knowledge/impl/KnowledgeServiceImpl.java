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

        // Delete chunks and document
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
