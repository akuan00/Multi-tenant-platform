package com.company.ai.platform.embedding.model;

import lombok.Data;

@Data
public class EmbeddingRequest {
    private String appId;
    private String text;
}
