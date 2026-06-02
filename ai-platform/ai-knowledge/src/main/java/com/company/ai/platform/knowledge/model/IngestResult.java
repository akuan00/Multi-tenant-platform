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
