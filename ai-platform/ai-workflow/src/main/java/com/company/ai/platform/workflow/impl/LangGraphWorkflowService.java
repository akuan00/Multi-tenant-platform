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
import org.springframework.stereotype.Service;

import java.util.Map;

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

        String workflowId = request.getWorkflowId();
        if (workflowId == null || workflowId.isBlank()) {
            workflowId = tenantConfigService.getConfigValue(appId, "workflow.default")
                    .orElse(null);
        }

        if (workflowId == null || !workflowRegistry.contains(workflowId)) {
            throw new BizException(ResultCode.WORKFLOW_NOT_FOUND);
        }

        log.info("Running workflow: appId={}, workflowId={}", appId, workflowId);

        Object graph = workflowRegistry.get(workflowId);
        return WorkflowResult.of(workflowId, Map.of("status", "executed", "appId", appId));
    }

    @Override
    public void registerWorkflow(String workflowId, Object graph) {
        workflowRegistry.register(workflowId, graph);
    }
}
