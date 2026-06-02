package com.company.ai.common.redis.service;

import com.company.ai.tenant.context.TenantContext;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class TenantRedisService {

    private final StringRedisTemplate redisTemplate;

    public void set(String key, String value, long timeout, TimeUnit unit) {
        redisTemplate.opsForValue().set(tenantKey(key), value, timeout, unit);
    }

    public String get(String key) {
        return redisTemplate.opsForValue().get(tenantKey(key));
    }

    public Boolean delete(String key) {
        return redisTemplate.delete(tenantKey(key));
    }

    public Boolean hasKey(String key) {
        return redisTemplate.hasKey(tenantKey(key));
    }

    private String tenantKey(String key) {
        String appId = TenantContext.getAppId();
        if (appId == null) {
            return key;
        }
        return appId + ":" + key;
    }
}
