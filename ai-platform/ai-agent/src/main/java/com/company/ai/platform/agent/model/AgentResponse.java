package com.company.ai.platform.agent.model;

import lombok.Data;
import java.util.List;

@Data
public class AgentResponse {
    private String content;
    private List<ToolCall> toolCalls;
    private Integer totalTokens;

    @Data
    public static class ToolCall {
        private String toolId;
        private String toolName;
        private String arguments;
        private String result;
    }

    public static AgentResponse of(String content) {
        AgentResponse response = new AgentResponse();
        response.setContent(content);
        return response;
    }
}
