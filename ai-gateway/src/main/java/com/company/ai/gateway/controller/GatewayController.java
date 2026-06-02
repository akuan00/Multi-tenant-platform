package com.company.ai.gateway.controller;

import com.company.ai.common.core.result.R;
import com.company.ai.platform.agent.AgentService;
import com.company.ai.platform.agent.model.AgentRequest;
import com.company.ai.platform.agent.model.AgentResponse;
import com.company.ai.platform.llm.LlmService;
import com.company.ai.platform.llm.model.ChatRequest;
import com.company.ai.platform.llm.model.ChatResponse;
import com.company.ai.platform.rag.RagService;
import com.company.ai.platform.rag.model.RagQuery;
import com.company.ai.platform.rag.model.RagResult;
import com.company.ai.platform.workflow.WorkflowService;
import com.company.ai.platform.workflow.model.WorkflowRequest;
import com.company.ai.platform.workflow.model.WorkflowResult;
import com.company.ai.tenant.context.TenantContext;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class GatewayController {

    private final LlmService llmService;
    private final RagService ragService;
    private final AgentService agentService;
    private final WorkflowService workflowService;

    @PostMapping("/chat")
    public R<ChatResponse> chat(@RequestBody ChatRequest request) {
        request.setAppId(TenantContext.getAppId());
        return R.ok(llmService.chat(request));
    }

    @PostMapping(value = "/chat/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> chatStream(@RequestBody ChatRequest request) {
        request.setAppId(TenantContext.getAppId());
        return llmService.chatStream(request);
    }

    @PostMapping("/rag/query")
    public R<RagResult> ragQuery(@RequestBody RagQuery query) {
        String appId = TenantContext.getAppId();
        return R.ok(ragService.query(appId, query));
    }

    @PostMapping("/agent/run")
    public R<AgentResponse> agentRun(@RequestBody AgentRequest request) {
        request.setAppId(TenantContext.getAppId());
        return R.ok(agentService.execute(request));
    }

    @PostMapping("/workflow/run")
    public R<WorkflowResult> workflowRun(@RequestBody WorkflowRequest request) {
        request.setAppId(TenantContext.getAppId());
        return R.ok(workflowService.run(request));
    }
}
