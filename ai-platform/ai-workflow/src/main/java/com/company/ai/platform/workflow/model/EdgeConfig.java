package com.company.ai.platform.workflow.model;

import lombok.Data;

import java.util.Map;

@Data
public class EdgeConfig {
    private String from;
    private String to;
    private String condition;  // optional: state key for conditional routing
    private Map<String, String> mappings;  // condition value -> target node
}
