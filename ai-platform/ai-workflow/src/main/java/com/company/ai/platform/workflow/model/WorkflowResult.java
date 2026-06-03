package com.company.ai.platform.workflow.model;

import lombok.Data;
import java.util.Map;

@Data
public class WorkflowResult {
    private String workflowId;
    private Map<String, Object> output;
    private String status;
    private String appId;
    private Integer nodeCount;

    public static WorkflowResult of(String workflowId, Map<String, Object> output) {
        WorkflowResult result = new WorkflowResult();
        result.setWorkflowId(workflowId);
        result.setOutput(output);
        result.setStatus("completed");
        return result;
    }

    public static WorkflowResult of(String workflowId, String appId, Map<String, Object> output) {
        WorkflowResult result = of(workflowId, output);
        result.setAppId(appId);
        return result;
    }

    public static WorkflowResult failed(String workflowId, String appId, String errorMessage) {
        WorkflowResult result = new WorkflowResult();
        result.setWorkflowId(workflowId);
        result.setAppId(appId);
        result.setStatus("failed");
        result.setOutput(Map.of("error", errorMessage));
        return result;
    }
}
