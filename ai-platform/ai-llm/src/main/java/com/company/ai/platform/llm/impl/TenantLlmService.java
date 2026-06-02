package com.company.ai.platform.llm.impl;

import com.company.ai.common.core.exception.BizException;
import com.company.ai.common.core.result.ResultCode;
import com.company.ai.platform.llm.LlmService;
import com.company.ai.platform.llm.model.ChatRequest;
import com.company.ai.platform.llm.model.ChatResponse;
import com.company.ai.tenant.context.TenantContext;
import com.company.ai.tenant.service.TenantConfigService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class TenantLlmService implements LlmService {

    private final TenantConfigService tenantConfigService;
    private final ChatModel chatModel;

    @Override
    public ChatResponse chat(ChatRequest request) {
        String appId = resolveAppId(request);
        List<Message> messages = buildMessages(request);
        Prompt prompt = new Prompt(messages);
        var response = chatModel.call(prompt);
        String content = response.getResult().getOutput().getText();
        return ChatResponse.of(content, "default");
    }

    @Override
    public Flux<String> chatStream(ChatRequest request) {
        List<Message> messages = buildMessages(request);
        Prompt prompt = new Prompt(messages);
        return chatModel.stream(prompt)
                .map(response -> {
                    var output = response.getResult().getOutput();
                    return output.getText();
                })
                .filter(text -> text != null && !text.isEmpty());
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

    private List<Message> buildMessages(ChatRequest request) {
        List<Message> messages = new ArrayList<>();
        if (request.getSystemPrompt() != null && !request.getSystemPrompt().isBlank()) {
            messages.add(new SystemMessage(request.getSystemPrompt()));
        }
        if (request.getHistory() != null) {
            for (ChatRequest.ChatMessage msg : request.getHistory()) {
                switch (msg.getRole().toLowerCase()) {
                    case "user" -> messages.add(new UserMessage(msg.getContent()));
                    case "assistant" -> messages.add(new AssistantMessage(msg.getContent()));
                    default -> log.warn("Unknown message role: {}", msg.getRole());
                }
            }
        }
        messages.add(new UserMessage(request.getMessage()));
        return messages;
    }
}
