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
