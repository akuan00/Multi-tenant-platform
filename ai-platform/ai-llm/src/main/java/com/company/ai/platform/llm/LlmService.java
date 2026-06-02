package com.company.ai.platform.llm;

import com.company.ai.platform.llm.model.ChatRequest;
import com.company.ai.platform.llm.model.ChatResponse;
import reactor.core.publisher.Flux;

public interface LlmService {
    ChatResponse chat(ChatRequest request);
    Flux<String> chatStream(ChatRequest request);
}
