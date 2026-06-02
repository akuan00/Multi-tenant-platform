package com.company.ai.platform.llm.impl;

import com.company.ai.common.core.exception.BizException;
import com.company.ai.common.core.result.ResultCode;
import com.company.ai.platform.llm.LlmService;
import com.company.ai.platform.llm.factory.ChatModelFactory;
import com.company.ai.platform.llm.factory.StreamingChatModelFactory;
import com.company.ai.platform.llm.model.ChatRequest;
import com.company.ai.platform.llm.model.ChatResponse;
import com.company.ai.tenant.context.TenantContext;
import com.company.ai.tenant.service.TenantConfigService;
import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.data.message.SystemMessage;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.chat.StreamingChatModel;
import dev.langchain4j.model.chat.response.StreamingChatResponseHandler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class TenantLlmService implements LlmService {

    private final ChatModelFactory chatModelFactory;
    private final StreamingChatModelFactory streamingChatModelFactory;
    private final TenantConfigService tenantConfigService;

    @Override
    public ChatResponse chat(ChatRequest request) {
        String appId = resolveAppId(request);
        ChatModel chatModel = chatModelFactory.getChatModel(appId);
        List<ChatMessage> messages = buildMessages(request);

        dev.langchain4j.model.chat.response.ChatResponse response = chatModel.chat(messages);
        String content = response.aiMessage().text();
        String modelName = tenantConfigService.getConfigValue(appId, "llm.model").orElse("unknown");

        ChatResponse chatResponse = ChatResponse.of(content, modelName);
        if (response.tokenUsage() != null) {
            chatResponse.setPromptTokens(response.tokenUsage().inputTokenCount());
            chatResponse.setCompletionTokens(response.tokenUsage().outputTokenCount());
        }
        return chatResponse;
    }

    @Override
    public Flux<String> chatStream(ChatRequest request) {
        String appId = resolveAppId(request);
        StreamingChatModel streamingChatModel = streamingChatModelFactory.getStreamingChatModel(appId);
        List<ChatMessage> messages = buildMessages(request);

        return Flux.create(sink -> streamingChatModel.chat(messages, new StreamingChatResponseHandler() {
            @Override
            public void onPartialResponse(String partialResponse) {
                if (partialResponse != null && !partialResponse.isEmpty()) {
                    sink.next(partialResponse);
                }
            }

            @Override
            public void onCompleteResponse(dev.langchain4j.model.chat.response.ChatResponse response) {
                sink.complete();
            }

            @Override
            public void onError(Throwable error) {
                log.error("Streaming chat error for appId={}", appId, error);
                sink.error(error);
            }
        }));
    }

    private String resolveAppId(ChatRequest request) {
        String appId = request.getAppId();
        if (appId == null || appId.isBlank()) {
            appId = TenantContext.getAppId();
        }
        if (appId == null) {
            throw new BizException(ResultCode.TENANT_NOT_FOUND);
        }
        return appId;
    }

    private List<ChatMessage> buildMessages(ChatRequest request) {
        List<ChatMessage> messages = new ArrayList<>();
        if (request.getSystemPrompt() != null && !request.getSystemPrompt().isBlank()) {
            messages.add(SystemMessage.from(request.getSystemPrompt()));
        }
        if (request.getHistory() != null) {
            for (ChatRequest.ChatMessage msg : request.getHistory()) {
                switch (msg.getRole().toLowerCase()) {
                    case "user" -> messages.add(UserMessage.from(msg.getContent()));
                    case "assistant" -> messages.add(AiMessage.from(msg.getContent()));
                    default -> log.warn("Unknown message role: {}", msg.getRole());
                }
            }
        }
        messages.add(UserMessage.from(request.getMessage()));
        return messages;
    }
}
