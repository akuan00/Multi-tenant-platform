package com.company.ai.gateway.controller;

import com.company.ai.common.core.result.R;
import com.company.ai.platform.agent.AgentService;
import com.company.ai.platform.agent.model.AgentRequest;
import com.company.ai.platform.agent.model.AgentResponse;
import com.company.ai.platform.conversation.ConversationService;
import com.company.ai.platform.conversation.model.ConversationMessageVO;
import com.company.ai.platform.conversation.model.ConversationVO;
import com.company.ai.platform.conversation.model.CreateConversationRequest;
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
import com.company.ai.platform.knowledge.KnowledgeService;
import com.company.ai.platform.knowledge.model.DocumentVO;
import com.company.ai.platform.knowledge.model.IngestResult;
import com.company.ai.tenant.context.TenantContext;
import com.company.ai.tenant.entity.TenantConfig;
import com.company.ai.tenant.service.TenantConfigService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import reactor.core.publisher.Flux;

import java.util.List;

@Slf4j
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
    private final KnowledgeService knowledgeService;
    private final ConversationService conversationService;

    @PostMapping("/chat")
    public R<ChatResponse> chat(@RequestBody ChatRequest request) {
        String appId = TenantContext.getAppId();
        request.setAppId(appId);

        // Resolve or create conversation
        resolveConversation(request, appId);

        ChatResponse response = llmService.chat(request);

        // Persist messages if conversation is active
        if (request.getConversationId() != null) {
            conversationService.saveUserMessage(request.getConversationId(), request.getMessage());
            Integer totalTokens = (response.getPromptTokens() != null && response.getCompletionTokens() != null)
                    ? response.getPromptTokens() + response.getCompletionTokens() : null;
            conversationService.saveAssistantMessage(request.getConversationId(), response.getContent(), totalTokens);
        }

        response.setConversationId(request.getConversationId());
        return R.ok(response);
    }

    @PostMapping(value = "/chat/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> chatStream(@RequestBody ChatRequest request) {
        String appId = TenantContext.getAppId();
        request.setAppId(appId);

        // Resolve or create conversation
        resolveConversation(request, appId);

        final Long convId = request.getConversationId();

        // Persist user message synchronously before streaming
        if (convId != null) {
            conversationService.saveUserMessage(convId, request.getMessage());
        }

        StringBuilder fullResponse = new StringBuilder();

        Flux<String> stream = llmService.chatStream(request)
                .doOnNext(fullResponse::append)
                .doOnComplete(() -> {
                    if (convId != null) {
                        conversationService.saveAssistantMessage(convId, fullResponse.toString(), null);
                    }
                })
                .doOnError(error -> log.error("Stream error for conversation {}", convId, error));

        // Prepend conversationId metadata event so client knows which conversation was created
        if (convId != null) {
            stream = stream.startWith("data: {\"type\":\"conversationId\",\"conversationId\":" + convId + "}\n\n");
        }

        return stream;
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
    public R<List<TenantConfig>> getTenantConfig(
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

    @PostMapping("/knowledge/upload")
    public R<IngestResult> knowledgeUpload(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "title", required = false) String title) {
        String appId = TenantContext.getAppId();
        return R.ok(knowledgeService.ingest(appId, file, title));
    }

    @DeleteMapping("/knowledge/document/{documentId}")
    public R<Void> deleteKnowledgeDocument(@PathVariable Long documentId) {
        String appId = TenantContext.getAppId();
        knowledgeService.deleteDocument(appId, documentId);
        return R.ok();
    }

    @GetMapping("/knowledge/documents")
    public R<List<DocumentVO>> listKnowledgeDocuments() {
        String appId = TenantContext.getAppId();
        return R.ok(knowledgeService.listDocuments(appId));
    }

    // -- Conversation endpoints --

    @PostMapping("/conversations")
    public R<ConversationVO> createConversation(@RequestBody CreateConversationRequest request) {
        String appId = TenantContext.getAppId();
        return R.ok(conversationService.createConversation(appId, request));
    }

    @GetMapping("/conversations")
    public R<List<ConversationVO>> listConversations(
            @RequestParam(required = false) String userId) {
        String appId = TenantContext.getAppId();
        return R.ok(conversationService.listConversations(appId, userId));
    }

    @GetMapping("/conversations/{id}/messages")
    public R<List<ConversationMessageVO>> getConversationMessages(@PathVariable Long id) {
        String appId = TenantContext.getAppId();
        return R.ok(conversationService.getMessages(appId, id));
    }

    @DeleteMapping("/conversations/{id}")
    public R<Void> deleteConversation(@PathVariable Long id) {
        String appId = TenantContext.getAppId();
        conversationService.deleteConversation(appId, id);
        return R.ok();
    }

    // -- Private helpers --

    private void resolveConversation(ChatRequest request, String appId) {
        if (request.getConversationId() != null) {
            // Load history from DB, override client-sent history
            List<ConversationMessageVO> history = conversationService.getMessages(appId, request.getConversationId());
            request.setHistory(toChatMessages(history));
        } else if (request.getUserId() != null) {
            // Auto-create conversation
            Long convId = conversationService.ensureConversation(appId, null, request.getUserId(), request.getMessage());
            request.setConversationId(convId);
        }
    }

    private List<ChatRequest.ChatMessage> toChatMessages(List<ConversationMessageVO> messages) {
        return messages.stream().map(m -> {
            ChatRequest.ChatMessage cm = new ChatRequest.ChatMessage();
            cm.setRole(m.getRole());
            cm.setContent(m.getContent());
            return cm;
        }).toList();
    }
}
