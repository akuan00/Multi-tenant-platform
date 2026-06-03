package com.company.ai.platform.workflow.impl;

import com.company.ai.common.core.exception.BizException;
import com.company.ai.common.core.result.ResultCode;
import com.company.ai.platform.workflow.WorkflowService;
import com.company.ai.platform.workflow.model.WorkflowRequest;
import com.company.ai.platform.workflow.model.WorkflowResult;
import com.company.ai.platform.workflow.registry.WorkflowRegistry;
import com.company.ai.tenant.context.TenantContext;
import com.company.ai.tenant.service.TenantConfigService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bsc.langgraph4j.CompiledGraph;
import org.bsc.langgraph4j.state.AgentState;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

@Slf4j
@Service
@RequiredArgsConstructor
public class LangGraphWorkflowService implements WorkflowService {

    private final WorkflowRegistry workflowRegistry;
    private final TenantConfigService tenantConfigService;

    @Override
    public WorkflowResult run(WorkflowRequest request) {
        String appId = request.getAppId();
        if (appId == null || appId.isBlank()) {
            appId = TenantContext.getAppId();
        }

        String workflowId = resolveWorkflowId(appId, request);

        CompiledGraph<AgentState> graph = workflowRegistry.get(workflowId);
        if (graph == null) {
            throw new BizException(ResultCode.WORKFLOW_NOT_FOUND);
        }

        log.info("Running workflow: appId={}, workflowId={}", appId, workflowId);

        Map<String, Object> input = buildInput(appId, request);

        try {
            Optional<AgentState> result = graph.invoke(input);
            Map<String, Object> output = result.map(AgentState::data).orElse(Map.of());

            // Filter out internal keys from output
            Map<String, Object> filteredOutput = new HashMap<>(output);
            filteredOutput.remove("appId");

            WorkflowResult workflowResult = WorkflowResult.of(workflowId, appId, filteredOutput);
            workflowResult.setNodeCount(countNodes(output));
            return workflowResult;
        } catch (Exception e) {
            log.error("Workflow execution failed: appId={}, workflowId={}", appId, workflowId, e);
            return WorkflowResult.failed(workflowId, appId, e.getMessage());
        }
    }

    @Override
    public void registerWorkflow(String workflowId, CompiledGraph<AgentState> graph) {
        workflowRegistry.register(workflowId, graph);
    }

    @Override
    public Set<String> listWorkflowIds() {
        return workflowRegistry.getWorkflowIds();
    }

    private String resolveWorkflowId(String appId, WorkflowRequest request) {
        String workflowId = request.getWorkflowId();
        if (workflowId == null || workflowId.isBlank()) {
            workflowId = tenantConfigService.getConfigValue(appId, "workflow.default")
                    .orElse(null);
        }
        if (workflowId == null) {
            throw new BizException(ResultCode.WORKFLOW_NOT_FOUND);
        }
        return workflowId;
    }

    private Map<String, Object> buildInput(String appId, WorkflowRequest request) {
        Map<String, Object> input = new HashMap<>();
        if (request.getInput() != null) {
            input.putAll(request.getInput());
        }
        input.putIfAbsent("appId", appId);
        input.putIfAbsent("message", input.getOrDefault("message", ""));
        return input;
    }

    private int countNodes(Map<String, Object> output) {
        // Estimate node count from the number of non-internal state keys
        return (int) output.keySet().stream()
                .filter(k -> !k.equals("appId") && !k.equals("message"))
                .count();
    }
}
