package com.company.ai.platform.workflow.node;

import com.company.ai.platform.agent.AgentService;
import com.company.ai.platform.llm.LlmService;
import com.company.ai.platform.prompt.PromptService;
import com.company.ai.platform.rag.RagService;
import com.company.ai.platform.workflow.model.NodeConfig;
import org.bsc.langgraph4j.action.AsyncNodeAction;
import org.bsc.langgraph4j.state.AgentState;

import java.util.List;
import java.util.Map;

public class NodeActionFactory {

    public static AsyncNodeAction<AgentState> create(NodeConfig nodeConfig,
                                                      LlmService llmService,
                                                      AgentService agentService,
                                                      RagService ragService,
                                                      PromptService promptService) {
        String type = nodeConfig.getType();
        Map<String, Object> config = nodeConfig.getConfig();

        return switch (type) {
            case "llm" -> createLlmNode(llmService, config);
            case "agent" -> createAgentNode(agentService, config);
            case "rag" -> createRagNode(ragService, config);
            case "prompt" -> createPromptNode(promptService, config);
            default -> throw new IllegalArgumentException("Unknown node type: " + type);
        };
    }

    @SuppressWarnings("unchecked")
    private static LlmNodeAction createLlmNode(LlmService llmService, Map<String, Object> config) {
        String scene = config != null ? (String) config.get("scene") : null;
        Map<String, Object> params = config != null ? (Map<String, Object>) config.get("parameters") : null;
        return new LlmNodeAction(llmService, scene, params);
    }

    @SuppressWarnings("unchecked")
    private static AgentNodeAction createAgentNode(AgentService agentService, Map<String, Object> config) {
        String agentName = config != null ? (String) config.get("agentName") : "default";
        List<String> toolIds = config != null ? (List<String>) config.get("toolIds") : List.of();
        return new AgentNodeAction(agentService, agentName, toolIds);
    }

    private static RagNodeAction createRagNode(RagService ragService, Map<String, Object> config) {
        int topK = 5;
        if (config != null && config.get("topK") instanceof Number n) {
            topK = n.intValue();
        }
        return new RagNodeAction(ragService, topK);
    }

    @SuppressWarnings("unchecked")
    private static PromptNodeAction createPromptNode(PromptService promptService, Map<String, Object> config) {
        String scene = config != null ? (String) config.get("scene") : "system";
        Map<String, String> variables = config != null ? (Map<String, String>) config.get("variables") : null;
        return new PromptNodeAction(promptService, scene, variables);
    }
}
