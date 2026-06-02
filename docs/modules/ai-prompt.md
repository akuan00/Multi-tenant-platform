# ai-prompt — 提示词管理模块

> 路径: `ai-platform/ai-prompt/`

## 定位

AI 中台的提示词管理中心，按租户+场景管理提示词模板，支持变量渲染。

## 核心类

| 类 | 职责 |
|---|---|
| `model/PromptTemplate.java` | @TableName("prompt_template")：appId, scene, content, variables |
| `mapper/PromptTemplateMapper.java` | BaseMapper<PromptTemplate> |
| `PromptService.java` | 接口：getSystemPrompt(appId, scene), renderPrompt(appId, scene, variables) |
| `impl/DatabasePromptService.java` | 数据库实现：Redis 缓存（1h TTL）+ 模板变量替换 |

## 关键设计

### 模板变量语法

使用 `{{key}}` 占位符，渲染时替换为实际值：

```
你是一个{{domain}}专家，请根据以下资料回答用户问题：
{{context}}
用户问题：{{question}}
```

### 缓存策略

- 提示词查询结果缓存 1 小时（Redis）
- 缓存 key：`prompt:{appId}:{scene}`

### 数据库表

- `prompt_template`：id, app_id, scene, content, variables, created_at, updated_at

## 依赖

- common-core, ai-tenant
- common-redis
- mybatis-plus
