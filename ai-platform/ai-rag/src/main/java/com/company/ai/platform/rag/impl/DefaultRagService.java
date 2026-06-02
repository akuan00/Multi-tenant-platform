package com.company.ai.platform.rag.impl;

import com.company.ai.platform.embedding.EmbeddingService;
import com.company.ai.platform.llm.LlmService;
import com.company.ai.platform.llm.model.ChatRequest;
import com.company.ai.platform.llm.model.ChatResponse;
import com.company.ai.platform.prompt.PromptService;
import com.company.ai.platform.rag.RagService;
import com.company.ai.platform.rag.model.RagQuery;
import com.company.ai.platform.rag.model.RagResult;
import com.company.ai.platform.vector.VectorStoreService;
import com.company.ai.platform.vector.model.VectorSearchResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class DefaultRagService implements RagService {

    private final EmbeddingService embeddingService;
    private final VectorStoreService vectorStoreService;
    private final LlmService llmService;
    private final PromptService promptService;

    @Override
    public RagResult query(String appId, RagQuery ragQuery) {
        float[] queryEmbedding = embeddingService.embed(ragQuery.getQuestion());
        List<VectorSearchResult> searchResults = vectorStoreService.search(appId, queryEmbedding, ragQuery.getTopK());

        StringBuilder contextBuilder = new StringBuilder();
        for (VectorSearchResult result : searchResults) {
            contextBuilder.append(result.getContent()).append("\n\n");
        }

        String systemPrompt = promptService.renderPrompt(appId, "rag",
                Map.of("context", contextBuilder.toString()));

        ChatRequest chatRequest = new ChatRequest();
        chatRequest.setAppId(appId);
        chatRequest.setMessage(ragQuery.getQuestion());
        chatRequest.setSystemPrompt(systemPrompt);
        ChatResponse chatResponse = llmService.chat(chatRequest);

        RagResult result = new RagResult();
        result.setAnswer(chatResponse.getContent());
        result.setSources(searchResults.stream().map(sr -> {
            RagResult.SourceReference ref = new RagResult.SourceReference();
            ref.setChunkId(sr.getChunkId());
            ref.setContent(sr.getContent());
            ref.setSimilarity(sr.getSimilarity());
            return ref;
        }).toList());
        return result;
    }
}
