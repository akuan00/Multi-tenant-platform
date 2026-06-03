package com.company.ai.platform.workflow.node;

import com.company.ai.platform.agent.AgentService;
import com.company.ai.platform.agent.model.AgentRequest;
import com.company.ai.platform.agent.model.AgentResponse;
import org.bsc.langgraph4j.action.AsyncNodeAction;
import org.bsc.langgraph4j.state.AgentState;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public class AgentNodeAction implements AsyncNodeAction<AgentState> {

    private final AgentService agentService;
    private final String agentName;
    private final List<String> toolIds;

    public AgentNodeAction(AgentService agentService, String agentName, List<String> toolIds) {
        this.agentService = agentService;
        this.agentName = agentName;
        this.toolIds = toolIds;
    }

    @Override
    public CompletableFuture<Map<String, Object>> apply(AgentState state) {
        return CompletableFuture.supplyAsync(() -> {
            AgentRequest request = new AgentRequest();
            request.setAppId(state.<String>value("appId").orElse(""));
            request.setMessage(state.<String>value("message").orElse(""));
            request.setAgentName(agentName);
            request.setToolIds(toolIds);

            String sessionId = state.<String>value("sessionId").orElse(null);
            request.setSessionId(sessionId);

            AgentResponse response = agentService.execute(request);

            Map<String, Object> result = new HashMap<>();
            result.put("answer", response.getContent());
            result.put("sessionId", response.getSessionId());
            if (response.getToolExecutions() != null && !response.getToolExecutions().isEmpty()) {
                result.put("toolExecutions", response.getToolExecutions());
            }
            return result;
        });
    }
}
