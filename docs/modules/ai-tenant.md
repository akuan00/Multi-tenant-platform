# ai-tenant — 多租户核心模块

> 路径: `ai-platform/ai-tenant/`（注意：包名在 ai-platform 下但逻辑独立）

## 定位

多租户隔离的核心实现：上下文管理、请求拦截、租户配置的数据库访问与缓存。

## 核心类

| 类 | 职责 |
|---|---|
| `context/TenantContext.java` | ThreadLocal<String> 存储 appId，提供静态 set/get/clear 方法 |
| `interceptor/TenantInterceptor.java` | HandlerInterceptor，读取 X-App-Id 请求头，设置 TenantContext + MDC |
| `entity/TenantApp.java` | @TableName("app_info")，租户应用实体 |
| `entity/TenantConfig.java` | @TableName("tenant_config")，租户配置实体（configKey/configValue/configType） |
| `mapper/TenantAppMapper.java` | BaseMapper<TenantApp> |
| `mapper/TenantConfigMapper.java` | BaseMapper<TenantConfig> |
| `service/TenantConfigService.java` | 接口：getConfigValue, getConfigsByType, getConfigValueOrThrow, saveConfig |
| `service/impl/TenantConfigServiceImpl.java` | Redis 缓存实现（24h TTL），缓存前缀 `tenant:config:{appId}:{key}` |

## 关键设计

- **TenantContext**：基于 ThreadLocal，整个请求链路可通过 `TenantContext.getAppId()` 获取当前租户
- **TenantInterceptor**：注册在 WebMvcConfig 中，拦截 `/api/v1/**` 路径
- **TenantConfigService**：所有配置查询带 Redis 缓存，避免频繁查库
- **配置热更新**：通过 GatewayController PUT /tenant/config 触发 chatModelFactory.evict(appId)

## 数据库表

- `app_info`：id, app_id, app_name, description, status
- `tenant_config`：id, app_id, config_key, config_value, config_type, description

## 依赖

- common-core
- common-redis
- mybatis-plus
