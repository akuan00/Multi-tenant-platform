package com.company.ai.platform.vector.model;

import lombok.Data;
import java.util.Map;

@Data
public class VectorDocument {
    private Long chunkId;
    private String content;
    private float[] embedding;
    private Map<String, Object> metadata;
}
