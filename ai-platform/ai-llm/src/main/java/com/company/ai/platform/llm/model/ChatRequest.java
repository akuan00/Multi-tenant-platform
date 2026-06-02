package com.company.ai.platform.llm.model;

import lombok.Data;
import java.util.List;
import java.util.Map;

@Data
public class ChatRequest {
    private String appId;
    private String message;
    private String systemPrompt;
    private List<ChatMessage> history;
    private Map<String, Object> parameters;

    @Data
    public static class ChatMessage {
        private String role;
        private String content;
    }
}
