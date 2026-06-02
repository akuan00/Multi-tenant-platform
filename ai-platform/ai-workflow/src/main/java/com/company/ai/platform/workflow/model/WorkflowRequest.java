package com.company.ai.platform.workflow.model;

import lombok.Data;
import java.util.Map;

@Data
public class WorkflowRequest {
    private String appId;
    private String workflowId;
    private Map<String, Object> input;
}
