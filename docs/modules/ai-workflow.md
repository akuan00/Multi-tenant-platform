# ai-workflow — 工作流编排模块

> 路径: `ai-platform/ai-workflow/`

## 定位

AI 中台的工作流编排引擎，基于 LangGraph4j 实现多业务独立工作流，当前为骨架实现。

## 核心类

| 类 | 职责 |
|---|---|
| `model/WorkflowRequest.java` | 工作流请求：appId, workflowId, input |
| `model/WorkflowResult.java` | 工作流结果：workflowId, output, status |
| `WorkflowService.java` | 接口：run(request), registerWorkflow(workflowId, graph) |
| `registry/WorkflowRegistry.java` | 工作流注册表：ConcurrentHashMap<String, Object> |
| `impl/LangGraphWorkflowService.java` | LangGraph4j 实现（骨架），当前返回 status="executed" |

## 关键设计

### 工作流注册机制

- `registerWorkflow(workflowId, graph)`：注册 LangGraph4j 图
- 工作流 ID 从 tenant_config 中获取（`workflow.{workflowId}.graph` 配置项）

### 骨架状态

当前为骨架实现，`run()` 方法：
1. 解析 workflowId
2. 从 WorkflowRegistry 查找对应图
3. 返回 `WorkflowResult(workflowId, input, "executed")`

### 后续扩展

- 每个业务定义独立工作流图
- 装修：需求理解 → RAG 风格库 → 户型解析 → 方案生成 → 绘图
- 考古：文物识别 → RAG 文献 → 年代推断 → 报告生成
- 聊天：意图识别 → 多轮对话 → 回答

## 依赖

- common-core, ai-tenant
- langgraph4j（1.0-beta5）
