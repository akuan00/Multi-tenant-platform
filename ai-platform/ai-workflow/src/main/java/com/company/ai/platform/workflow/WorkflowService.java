package com.company.ai.platform.workflow;

import com.company.ai.platform.workflow.model.WorkflowRequest;
import com.company.ai.platform.workflow.model.WorkflowResult;
import org.bsc.langgraph4j.CompiledGraph;
import org.bsc.langgraph4j.state.AgentState;

import java.util.Set;

public interface WorkflowService {
    WorkflowResult run(WorkflowRequest request);
    void registerWorkflow(String workflowId, CompiledGraph<AgentState> graph);
    Set<String> listWorkflowIds();
}
