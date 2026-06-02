package com.company.ai.platform.agent.impl;

import com.company.ai.common.core.exception.BizException;
import com.company.ai.common.core.result.ResultCode;
import com.company.ai.platform.agent.AgentService;
import com.company.ai.platform.agent.memory.AgentMemoryManager;
import com.company.ai.platform.agent.model.AgentRequest;
import com.company.ai.platform.agent.model.AgentResponse;
import com.company.ai.platform.agent.tool.DefaultTools;
import com.company.ai.platform.agent.tool.ToolRegistry;
import com.company.ai.platform.llm.factory.ChatModelFactory;
import com.company.ai.platform.prompt.PromptService;
import com.company.ai.tenant.context.TenantContext;
import dev.langchain4j.memory.ChatMemory;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.service.AiServices;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class LangChain4jAgentService implements AgentService {

    private final ChatModelFactory chatModelFactory;
    private final PromptService promptService;
    private final AgentMemoryManager memoryManager;
    private final ToolRegistry toolRegistry;
    private final DefaultTools defaultTools;

    @Override
    public AgentResponse execute(AgentRequest request) {
        String appId = resolveAppId(request);
        String sessionId = resolveSessionId(request);

        String systemPrompt = resolveSystemPrompt(appId, request.getAgentName());
        ChatModel chatModel = chatModelFactory.getChatModel(appId);
        ChatMemory chatMemory = memoryManager.getOrCreateMemory(appId, sessionId);
        List<Object> tools = resolveTools(appId, request.getToolIds());

        Agent agent = buildAgent(chatModel, chatMemory, systemPrompt, tools);
        String result = agent.chat(request.getMessage());

        AgentResponse response = AgentResponse.of(result);
        response.setSessionId(sessionId);
        response.setMemoryMessageCount(memoryManager.getMessageCount(appId, sessionId));
        return response;
    }

    @Override
    public void clearMemory(String appId, String sessionId) {
        memoryManager.clearMemory(appId, sessionId);
    }

    private String resolveAppId(AgentRequest request) {
        String appId = request.getAppId();
        if (appId == null || appId.isBlank()) {
            appId = TenantContext.getAppId();
        }
        if (appId == null) {
            throw new BizException(ResultCode.TENANT_NOT_FOUND);
        }
        return appId;
    }

    private String resolveSessionId(AgentRequest request) {
        String sessionId = request.getSessionId();
        if (sessionId == null || sessionId.isBlank()) {
            sessionId = java.util.UUID.randomUUID().toString().replace("-", "").substring(0, 16);
        }
        return sessionId;
    }

    private String resolveSystemPrompt(String appId, String agentName) {
        if (agentName != null && !agentName.isBlank()) {
            String prompt = promptService.getSystemPrompt(appId, "agent." + agentName);
            if (!prompt.isEmpty()) {
                return prompt;
            }
        }
        return promptService.getSystemPrompt(appId, "system");
    }

    private List<Object> resolveTools(String appId, List<String> requestedToolIds) {
        List<Object> tools = new ArrayList<>();
        tools.add(defaultTools);

        if (requestedToolIds != null && !requestedToolIds.isEmpty()) {
            tools.addAll(toolRegistry.getToolsForApp(appId));
        } else {
            List<Object> appTools = toolRegistry.getToolsForApp(appId);
            if (!appTools.isEmpty()) {
                tools.addAll(appTools);
            }
        }

        return tools;
    }

    private Agent buildAgent(ChatModel chatModel, ChatMemory chatMemory,
                             String systemPrompt, List<Object> tools) {
        var builder = AiServices.builder(Agent.class)
                .chatModel(chatModel)
                .chatMemory(chatMemory);

        if (systemPrompt != null && !systemPrompt.isEmpty()) {
            builder.systemMessageProvider(memoryId -> systemPrompt);
        }

        if (!tools.isEmpty()) {
            builder.tools(tools);
        }

        return builder.build();
    }

    public interface Agent {
        String chat(String message);
    }
}
