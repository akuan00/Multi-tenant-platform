package com.company.ai.platform.workflow.model;

import lombok.Data;
import java.util.Map;

@Data
public class WorkflowResult {
    private String workflowId;
    private Map<String, Object> output;
    private String status;

    public static WorkflowResult of(String workflowId, Map<String, Object> output) {
        WorkflowResult result = new WorkflowResult();
        result.setWorkflowId(workflowId);
        result.setOutput(output);
        result.setStatus("completed");
        return result;
    }
}
