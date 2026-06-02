package com.company.ai.gateway.controller;

import com.company.ai.common.core.result.R;
import com.company.ai.platform.agent.AgentService;
import com.company.ai.platform.agent.model.AgentRequest;
import com.company.ai.platform.agent.model.AgentResponse;
import com.company.ai.platform.llm.LlmService;
import com.company.ai.platform.llm.factory.ChatModelFactory;
import com.company.ai.platform.llm.model.ChatRequest;
import com.company.ai.platform.llm.model.ChatResponse;
import com.company.ai.platform.rag.RagService;
import com.company.ai.platform.rag.model.RagQuery;
import com.company.ai.platform.rag.model.RagResult;
import com.company.ai.platform.workflow.WorkflowService;
import com.company.ai.platform.workflow.model.WorkflowRequest;
import com.company.ai.platform.workflow.model.WorkflowResult;
import com.company.ai.tenant.context.TenantContext;
import com.company.ai.tenant.entity.TenantConfig;
import com.company.ai.tenant.service.TenantConfigService;
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
    private final ChatModelFactory chatModelFactory;
    private final TenantConfigService tenantConfigService;

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

    @DeleteMapping("/agent/memory/{sessionId}")
    public R<Void> clearAgentMemory(@PathVariable String sessionId) {
        String appId = TenantContext.getAppId();
        agentService.clearMemory(appId, sessionId);
        return R.ok();
    }

    @PostMapping("/workflow/run")
    public R<WorkflowResult> workflowRun(@RequestBody WorkflowRequest request) {
        request.setAppId(TenantContext.getAppId());
        return R.ok(workflowService.run(request));
    }

    @GetMapping("/tenant/config")
    public R<java.util.List<TenantConfig>> getTenantConfig(
            @RequestParam(required = false) String configType) {
        String appId = TenantContext.getAppId();
        if (configType != null) {
            return R.ok(tenantConfigService.getConfigsByType(appId, configType));
        }
        return R.ok(tenantConfigService.getConfigsByType(appId, "MODEL"));
    }

    @PutMapping("/tenant/config")
    public R<Void> updateTenantConfig(@RequestBody TenantConfig config) {
        String appId = TenantContext.getAppId();
        config.setAppId(appId);
        tenantConfigService.saveConfig(config);
        if (config.getConfigKey().startsWith("llm.")) {
            chatModelFactory.evict(appId);
        }
        return R.ok();
    }
}
