package com.company.ai.platform.rag.model;

import lombok.Data;
import java.util.List;

@Data
public class RagResult {
    private String answer;
    private List<SourceReference> sources;

    @Data
    public static class SourceReference {
        private Long chunkId;
        private String content;
        private double similarity;
    }
}
