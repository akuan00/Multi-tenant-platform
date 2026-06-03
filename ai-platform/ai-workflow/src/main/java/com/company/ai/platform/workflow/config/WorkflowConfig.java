package com.company.ai.platform.workflow.config;

import com.company.ai.platform.agent.AgentService;
import com.company.ai.platform.llm.LlmService;
import com.company.ai.platform.prompt.PromptService;
import com.company.ai.platform.rag.RagService;
import com.company.ai.platform.workflow.definition.WorkflowContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class WorkflowConfig {

    @Bean
    public WorkflowContext workflowContext(LlmService llmService,
                                           AgentService agentService,
                                           RagService ragService,
                                           PromptService promptService) {
        return WorkflowContext.builder()
                .llmService(llmService)
                .agentService(agentService)
                .ragService(ragService)
                .promptService(promptService)
                .build();
    }
}
