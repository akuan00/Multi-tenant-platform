package com.company.ai.platform.rag.model;

import lombok.Data;

@Data
public class RagQuery {
    private String question;
    private int topK = 5;
}
