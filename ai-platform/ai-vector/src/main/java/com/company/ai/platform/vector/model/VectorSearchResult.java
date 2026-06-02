package com.company.ai.platform.vector.model;

import lombok.Data;
import java.util.Map;

@Data
public class VectorSearchResult {
    private Long chunkId;
    private String content;
    private double similarity;
    private Map<String, Object> metadata;
}
