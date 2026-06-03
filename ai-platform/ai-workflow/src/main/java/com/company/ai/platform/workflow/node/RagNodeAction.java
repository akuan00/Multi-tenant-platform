package com.company.ai.platform.workflow.node;

import com.company.ai.platform.rag.RagService;
import com.company.ai.platform.rag.model.RagQuery;
import com.company.ai.platform.rag.model.RagResult;
import org.bsc.langgraph4j.action.AsyncNodeAction;
import org.bsc.langgraph4j.state.AgentState;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public class RagNodeAction implements AsyncNodeAction<AgentState> {

    private final RagService ragService;
    private final int topK;

    public RagNodeAction(RagService ragService, int topK) {
        this.ragService = ragService;
        this.topK = topK;
    }

    @Override
    public CompletableFuture<Map<String, Object>> apply(AgentState state) {
        return CompletableFuture.supplyAsync(() -> {
            String appId = state.<String>value("appId").orElse("");
            String question = state.<String>value("message").orElse("");

            RagQuery query = new RagQuery();
            query.setQuestion(question);
            query.setTopK(topK);

            RagResult ragResult = ragService.query(appId, query);

            Map<String, Object> result = new HashMap<>();
            result.put("ragAnswer", ragResult.getAnswer());
            if (ragResult.getSources() != null) {
                result.put("sources", ragResult.getSources());
                // Build context string from sources for downstream LLM nodes
                StringBuilder context = new StringBuilder();
                for (var source : ragResult.getSources()) {
                    context.append(source.getContent()).append("\n\n");
                }
                result.put("context", context.toString().trim());
            }
            return result;
        });
    }
}
