package com.company.ai.platform.workflow.node;

import com.company.ai.platform.llm.LlmService;
import com.company.ai.platform.llm.model.ChatRequest;
import com.company.ai.platform.llm.model.ChatResponse;
import org.bsc.langgraph4j.action.AsyncNodeAction;
import org.bsc.langgraph4j.state.AgentState;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public class LlmNodeAction implements AsyncNodeAction<AgentState> {

    private final LlmService llmService;
    private final String systemPromptScene;
    private final Map<String, Object> parameters;

    public LlmNodeAction(LlmService llmService, String systemPromptScene, Map<String, Object> parameters) {
        this.llmService = llmService;
        this.systemPromptScene = systemPromptScene;
        this.parameters = parameters;
    }

    @Override
    public CompletableFuture<Map<String, Object>> apply(AgentState state) {
        return CompletableFuture.supplyAsync(() -> {
            ChatRequest request = new ChatRequest();
            request.setAppId(state.<String>value("appId").orElse(""));
            request.setMessage(state.<String>value("message").orElse(""));

            String systemPrompt = state.<String>value("systemPrompt").orElse(null);
            if (systemPromptScene != null && (systemPrompt == null || systemPrompt.isBlank())) {
                // Will use scene-based prompt from PromptService if available in state
                systemPrompt = state.<String>value("renderedPrompt").orElse(null);
            }
            request.setSystemPrompt(systemPrompt);
            request.setParameters(parameters);

            // Build history from state if available
            Object historyObj = state.data().get("history");
            if (historyObj instanceof java.util.List<?> historyList && !historyList.isEmpty()) {
                // History is passed through state as ChatMessage list
                request.setHistory(castHistory(historyList));
            }

            ChatResponse response = llmService.chat(request);

            Map<String, Object> result = new HashMap<>();
            result.put("answer", response.getContent());
            result.put("model", response.getModel());
            if (response.getPromptTokens() != null) {
                result.put("promptTokens", response.getPromptTokens());
            }
            if (response.getCompletionTokens() != null) {
                result.put("completionTokens", response.getCompletionTokens());
            }
            return result;
        });
    }

    @SuppressWarnings("unchecked")
    private java.util.List<ChatRequest.ChatMessage> castHistory(java.util.List<?> historyList) {
        java.util.List<ChatRequest.ChatMessage> messages = new ArrayList<>();
        for (Object item : historyList) {
            if (item instanceof ChatRequest.ChatMessage cm) {
                messages.add(cm);
            }
        }
        return messages;
    }
}
