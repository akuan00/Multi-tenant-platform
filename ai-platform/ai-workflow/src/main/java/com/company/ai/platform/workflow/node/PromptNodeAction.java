package com.company.ai.platform.workflow.node;

import com.company.ai.platform.prompt.PromptService;
import org.bsc.langgraph4j.action.AsyncNodeAction;
import org.bsc.langgraph4j.state.AgentState;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public class PromptNodeAction implements AsyncNodeAction<AgentState> {

    private final PromptService promptService;
    private final String scene;
    private final Map<String, String> staticVariables;

    public PromptNodeAction(PromptService promptService, String scene, Map<String, String> staticVariables) {
        this.promptService = promptService;
        this.scene = scene;
        this.staticVariables = staticVariables;
    }

    @Override
    public CompletableFuture<Map<String, Object>> apply(AgentState state) {
        return CompletableFuture.supplyAsync(() -> {
            String appId = state.<String>value("appId").orElse("");

            Map<String, String> variables = new HashMap<>();
            if (staticVariables != null) {
                variables.putAll(staticVariables);
            }
            // Merge state values into variables (e.g., context from RAG node)
            mergeFromState(state, variables);

            String renderedPrompt = promptService.renderPrompt(appId, scene, variables);

            Map<String, Object> result = new HashMap<>();
            result.put("renderedPrompt", renderedPrompt);
            result.put("systemPrompt", renderedPrompt);
            return result;
        });
    }

    private void mergeFromState(AgentState state, Map<String, String> variables) {
        for (Map.Entry<String, Object> entry : state.data().entrySet()) {
            if (entry.getValue() instanceof String value && !variables.containsKey(entry.getKey())) {
                // Only merge string values that aren't already set
                String placeholder = "{{" + entry.getKey() + "}}";
                // Check if the scene template might reference this variable
                if (!entry.getKey().equals("appId") && !entry.getKey().equals("message")) {
                    variables.putIfAbsent(entry.getKey(), value);
                }
            }
        }
    }
}
