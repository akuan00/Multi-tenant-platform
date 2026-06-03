package com.company.ai.platform.workflow.registry;

import lombok.extern.slf4j.Slf4j;
import org.bsc.langgraph4j.CompiledGraph;
import org.bsc.langgraph4j.state.AgentState;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Component
public class WorkflowRegistry {

    private final Map<String, CompiledGraph<AgentState>> workflows = new ConcurrentHashMap<>();

    public void register(String workflowId, CompiledGraph<AgentState> graph) {
        workflows.put(workflowId, graph);
        log.info("Registered workflow: {}", workflowId);
    }

    public CompiledGraph<AgentState> get(String workflowId) {
        return workflows.get(workflowId);
    }

    public boolean contains(String workflowId) {
        return workflows.containsKey(workflowId);
    }

    public Set<String> getWorkflowIds() {
        return workflows.keySet();
    }
}
