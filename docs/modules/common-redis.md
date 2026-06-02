# common-redis — Redis 公共模块

> 路径: `ai-common/common-redis/`

## 定位

统一 Redis 配置和租户感知的缓存服务，自动为 key 添加 appId 前缀。

## 核心类

| 类 | 职责 |
|---|---|
| `config/RedisConfig.java` | @Configuration，注册 StringRedisTemplate Bean |
| `service/TenantRedisService.java` | 租户感知的 Redis 操作服务 |

## 关键设计

- **TenantRedisService** 自动从 TenantContext 获取 appId，为所有 key 添加前缀
- 示例：`decorate:prompt:xxx`、`archaeology:config:yyy`
- 方法：`set(key, value, timeout)`、`get(key)`、`delete(key)`、`hasKey(key)`

## 依赖

- common-core
- ai-tenant（获取 TenantContext）
- spring-boot-starter-data-redis
