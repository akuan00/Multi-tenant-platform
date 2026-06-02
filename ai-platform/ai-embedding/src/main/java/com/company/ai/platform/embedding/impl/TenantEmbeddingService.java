package com.company.ai.platform.embedding.impl;

import com.company.ai.platform.embedding.EmbeddingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class TenantEmbeddingService implements EmbeddingService {

    private final EmbeddingModel embeddingModel;

    @Override
    public float[] embed(String text) {
        var response = embeddingModel.embedForResponse(List.of(text));
        return response.getResult().getOutput();
    }

    @Override
    public List<float[]> embedBatch(List<String> texts) {
        var response = embeddingModel.embedForResponse(texts);
        return response.getResults().stream()
                .map(result -> result.getOutput())
                .toList();
    }
}
