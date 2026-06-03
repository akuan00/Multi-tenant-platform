package com.company.ai.platform.workflow.definition;

import com.company.ai.platform.agent.AgentService;
import com.company.ai.platform.llm.LlmService;
import com.company.ai.platform.prompt.PromptService;
import com.company.ai.platform.rag.RagService;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class WorkflowContext {
    private final LlmService llmService;
    private final AgentService agentService;
    private final RagService ragService;
    private final PromptService promptService;
}
