package com.company.ai.platform.workflow.model;

import lombok.Data;

import java.util.Map;

@Data
public class NodeConfig {
    private String id;
    private String type;  // llm, agent, rag, prompt
    private Map<String, Object> config;
}
