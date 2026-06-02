package com.company.ai.platform.agent.impl;

import com.company.ai.platform.agent.AgentService;
import com.company.ai.platform.agent.model.AgentRequest;
import com.company.ai.platform.agent.model.AgentResponse;
import com.company.ai.platform.llm.LlmService;
import com.company.ai.platform.llm.model.ChatRequest;
import com.company.ai.platform.llm.model.ChatResponse;
import com.company.ai.platform.prompt.PromptService;
import com.company.ai.tenant.context.TenantContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class DefaultAgentService implements AgentService {

    private final LlmService llmService;
    private final PromptService promptService;

    @Override
    public AgentResponse execute(AgentRequest request) {
        String appId = request.getAppId();
        if (appId == null || appId.isBlank()) {
            appId = TenantContext.getAppId();
        }

        String systemPrompt = promptService.getSystemPrompt(appId,
                "agent." + request.getAgentName());

        ChatRequest chatRequest = new ChatRequest();
        chatRequest.setAppId(appId);
        chatRequest.setMessage(request.getMessage());
        chatRequest.setSystemPrompt(systemPrompt);

        ChatResponse chatResponse = llmService.chat(chatRequest);
        return AgentResponse.of(chatResponse.getContent());
    }
}
