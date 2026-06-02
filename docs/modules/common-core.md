# common-core — 公共核心模块

> 路径: `ai-common/common-core/`

## 定位

全项目基础依赖，提供统一响应、异常处理、错误码、工具类。所有模块均直接或间接依赖此模块。

## 核心类

| 类 | 职责 |
|---|---|
| `result/ResultCode.java` | 错误码枚举（SUCCESS, TENANT_NOT_FOUND, MODEL_NOT_CONFIGURED 等） |
| `result/R.java` | 统一响应包装器，含 `code`、`message`、`data`、`appId`、`traceId` 字段 |
| `exception/BizException.java` | 业务异常，携带 ResultCode |
| `exception/GlobalExceptionHandler.java` | @RestControllerAdvice，捕获 BizException 和通用 Exception |
| `util/TraceIdUtil.java` | ThreadLocal 生成 traceId，用于链路追踪 |

## 关键设计

- **R<T>** 响应格式：`{"code":200,"message":"success","data":{...},"appId":"decorate","traceId":"abc123"}`
- **GlobalExceptionHandler**：BizException 返回对应 code+message，未知异常返回 500 + 内部错误
- **TraceIdUtil**：每次请求生成唯一 traceId，自动写入 MDC 供日志输出

## 依赖

无业务依赖，仅 Spring Web + Lombok
