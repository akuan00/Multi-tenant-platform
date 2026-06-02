package com.company.ai.platform.agent.tool;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Component
public class ToolRegistry {

    private final Map<String, Object> toolsByName = new ConcurrentHashMap<>();
    private final Map<String, List<String>> toolsByAppId = new ConcurrentHashMap<>();

    public void registerTool(String name, Object toolInstance) {
        toolsByName.put(name, toolInstance);
        log.info("Registered tool: {}", name);
    }

    public void bindToolsToApp(String appId, List<String> toolNames) {
        toolsByAppId.put(appId, new ArrayList<>(toolNames));
        log.info("Bound tools to appId={}: {}", appId, toolNames);
    }

    public List<Object> getToolsForApp(String appId) {
        List<String> toolNames = toolsByAppId.getOrDefault(appId, List.of("default"));
        List<Object> result = new ArrayList<>();
        for (String name : toolNames) {
            Object tool = toolsByName.get(name);
            if (tool != null) {
                result.add(tool);
            } else {
                log.warn("Tool '{}' not found for appId={}", name, appId);
            }
        }
        return result;
    }

    public Object getTool(String name) {
        return toolsByName.get(name);
    }

    public Set<String> getAllToolNames() {
        return toolsByName.keySet();
    }
}
