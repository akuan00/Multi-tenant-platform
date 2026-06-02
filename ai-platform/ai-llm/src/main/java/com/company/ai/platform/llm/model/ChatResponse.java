package com.company.ai.platform.llm.model;

import lombok.Data;

@Data
public class ChatResponse {
    private String content;
    private String model;
    private Integer promptTokens;
    private Integer completionTokens;

    public static ChatResponse of(String content, String model) {
        ChatResponse response = new ChatResponse();
        response.setContent(content);
        response.setModel(model);
        return response;
    }
}
