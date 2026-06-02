package com.company.ai.platform.workflow.registry;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Component
public class WorkflowRegistry {

    private final Map<String, Object> workflows = new ConcurrentHashMap<>();

    public void register(String workflowId, Object graph) {
        workflows.put(workflowId, graph);
        log.info("Registered workflow: {}", workflowId);
    }

    public Object get(String workflowId) {
        return workflows.get(workflowId);
    }

    public boolean contains(String workflowId) {
        return workflows.containsKey(workflowId);
    }
}
