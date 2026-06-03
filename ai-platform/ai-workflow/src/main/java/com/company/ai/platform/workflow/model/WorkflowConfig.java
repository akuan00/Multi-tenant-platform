package com.company.ai.platform.workflow.model;

import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
public class WorkflowConfig {
    private List<NodeConfig> nodes;
    private List<EdgeConfig> edges;
    private Map<String, String> stateSchema;
}
