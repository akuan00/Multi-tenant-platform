package com.company.ai.platform.workflow.definition;

import com.company.ai.platform.workflow.model.EdgeConfig;
import com.company.ai.platform.workflow.model.NodeConfig;
import com.company.ai.platform.workflow.node.NodeActionFactory;
import org.bsc.langgraph4j.CompiledGraph;
import org.bsc.langgraph4j.GraphStateException;
import org.bsc.langgraph4j.StateGraph;
import org.bsc.langgraph4j.action.AsyncNodeAction;
import org.bsc.langgraph4j.state.AgentState;

import java.util.List;
import java.util.Map;

import static org.bsc.langgraph4j.StateGraph.END;
import static org.bsc.langgraph4j.StateGraph.START;
import static org.bsc.langgraph4j.action.AsyncEdgeAction.edge_async;

public abstract class AbstractWorkflowDefinition implements WorkflowDefinition {

    @Override
    public CompiledGraph<AgentState> buildGraph(WorkflowContext context) {
        try {
            StateGraph<AgentState> graph = new StateGraph<>(AgentState::new);

            List<NodeConfig> nodes = defineNodes();
            List<EdgeConfig> edges = defineEdges();

            // Add nodes
            for (NodeConfig nodeConfig : nodes) {
                AsyncNodeAction<AgentState> action = NodeActionFactory.create(
                        nodeConfig, context.getLlmService(), context.getAgentService(),
                        context.getRagService(), context.getPromptService());
                graph.addNode(nodeConfig.getId(), action);
            }

            // Add edges
            for (EdgeConfig edgeConfig : edges) {
                if (edgeConfig.getCondition() != null && edgeConfig.getMappings() != null) {
                    String stateKey = edgeConfig.getCondition();
                    Map<String, String> mappings = edgeConfig.getMappings();
                    graph.addConditionalEdges(
                            edgeConfig.getFrom(),
                            edge_async(state -> {
                                Object value = state.data().get(stateKey);
                                String key = value != null ? value.toString() : "end";
                                return mappings.getOrDefault(key, END);
                            }),
                            mappings
                    );
                } else {
                    String to = edgeConfig.getTo();
                    if (START.equals(edgeConfig.getFrom())) {
                        graph.addEdge(START, to);
                    } else if (END.equals(to)) {
                        graph.addEdge(edgeConfig.getFrom(), END);
                    } else {
                        graph.addEdge(edgeConfig.getFrom(), to);
                    }
                }
            }

            return graph.compile();
        } catch (GraphStateException e) {
            throw new RuntimeException("Failed to build workflow graph: " + getWorkflowId(), e);
        }
    }

    protected abstract List<NodeConfig> defineNodes();
    protected abstract List<EdgeConfig> defineEdges();
}
