package com.company.ai.platform.workflow.definition;

import org.bsc.langgraph4j.CompiledGraph;
import org.bsc.langgraph4j.state.AgentState;

public interface WorkflowDefinition {
    String getWorkflowId();
    String getDescription();
    String[] getAppIds();
    CompiledGraph<AgentState> buildGraph(WorkflowContext context);
}
