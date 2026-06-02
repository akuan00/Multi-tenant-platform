package com.company.ai.platform.workflow;

import com.company.ai.platform.workflow.model.WorkflowRequest;
import com.company.ai.platform.workflow.model.WorkflowResult;

public interface WorkflowService {
    WorkflowResult run(WorkflowRequest request);
    void registerWorkflow(String workflowId, Object graph);
}
