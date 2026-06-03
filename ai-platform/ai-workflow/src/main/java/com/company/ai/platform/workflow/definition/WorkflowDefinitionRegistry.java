package com.company.ai.platform.workflow.definition;

import com.company.ai.platform.workflow.entity.WorkflowDefinitionEntity;
import com.company.ai.platform.workflow.mapper.WorkflowDefinitionMapper;
import com.company.ai.platform.workflow.registry.WorkflowRegistry;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bsc.langgraph4j.CompiledGraph;
import org.bsc.langgraph4j.state.AgentState;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class WorkflowDefinitionRegistry implements CommandLineRunner {

    private final List<WorkflowDefinition> definitions;
    private final WorkflowRegistry workflowRegistry;
    private final WorkflowDefinitionMapper workflowDefinitionMapper;
    private final WorkflowContext workflowContext;
    private final ObjectMapper objectMapper;

    @Override
    public void run(String... args) {
        for (WorkflowDefinition definition : definitions) {
            try {
                CompiledGraph<AgentState> graph = definition.buildGraph(workflowContext);
                workflowRegistry.register(definition.getWorkflowId(), graph);

                // Upsert metadata into database for each bound appId
                for (String appId : definition.getAppIds()) {
                    upsertDefinition(appId, definition);
                }

                log.info("Registered workflow: {} for apps: {}",
                        definition.getWorkflowId(), String.join(", ", definition.getAppIds()));
            } catch (Exception e) {
                log.error("Failed to register workflow: {}", definition.getWorkflowId(), e);
            }
        }
    }

    private void upsertDefinition(String appId, WorkflowDefinition definition) {
        try {
            var existing = workflowDefinitionMapper.selectList(
                    new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<WorkflowDefinitionEntity>()
                            .eq(WorkflowDefinitionEntity::getAppId, appId)
                            .eq(WorkflowDefinitionEntity::getWorkflowName, definition.getWorkflowId()));

            String graphConfig = objectMapper.writeValueAsString(Map.of(
                    "description", definition.getDescription(),
                    "type", "code-defined"));

            if (existing.isEmpty()) {
                WorkflowDefinitionEntity entity = new WorkflowDefinitionEntity();
                entity.setAppId(appId);
                entity.setWorkflowName(definition.getWorkflowId());
                entity.setGraphConfig(graphConfig);
                entity.setCreatedAt(LocalDateTime.now());
                workflowDefinitionMapper.insert(entity);
            }
        } catch (Exception e) {
            log.warn("Failed to upsert workflow definition for {}/{}: {}",
                    appId, definition.getWorkflowId(), e.getMessage());
        }
    }
}
