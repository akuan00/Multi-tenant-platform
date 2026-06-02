# ai-business-starter — 业务扩展启动器

> 路径: `ai-business/ai-business-starter/`

## 定位

业务模块的扩展基座，定义业务扩展接口和默认健康检查端点，新业务模块继承此启动器。

## 核心类

| 类 | 职责 |
|---|---|
| `extension/BusinessExtension.java` | 业务扩展接口：getAppId(), getDescription(), registerWorkflow(), getDefaultConfig() |
| `controller/HealthController.java` | GET /api/v1/health → 返回 status, appId, timestamp |

## 关键设计

### BusinessExtension 接口

新业务模块实现此接口即可接入平台：

```java
public interface BusinessExtension {
    String getAppId();                    // 业务标识（如 "decorate"）
    String getDescription();              // 业务描述
    void registerWorkflow(...);           // 注册该业务的工作流
    Map<String, String> getDefaultConfig(); // 默认配置
}
```

### 健康检查

- 端点：GET /api/v1/health
- 响应：`{"status":"UP","appId":"xxx","timestamp":"2026-06-02T..."}`

## 依赖

- common-core

## 扩展方式

1. 新建 `ai-business/ai-business-xxx` 模块
2. 依赖 ai-business-starter
3. 实现 BusinessExtension 接口
4. 在 ai-app 中引入新模块依赖即可自动注册
