package com.company.ai.platform.agent.model;

import lombok.Data;
import java.util.List;
import java.util.Map;

@Data
public class AgentRequest {
    private String appId;
    private String message;
    private String agentName;
    private Map<String, Object> context;
    private List<String> toolIds;
}
