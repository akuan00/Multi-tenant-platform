package com.company.ai.tenant.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.company.ai.common.core.exception.BizException;
import com.company.ai.common.core.result.ResultCode;
import com.company.ai.tenant.entity.TenantConfig;
import com.company.ai.tenant.mapper.TenantConfigMapper;
import com.company.ai.tenant.service.TenantConfigService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class TenantConfigServiceImpl implements TenantConfigService {

    private final TenantConfigMapper tenantConfigMapper;
    private final StringRedisTemplate redisTemplate;

    private static final String CACHE_PREFIX = "tenant:config:";
    private static final long CACHE_TTL_HOURS = 24;

    @Override
    public Optional<String> getConfigValue(String appId, String configKey) {
        String cacheKey = CACHE_PREFIX + appId + ":" + configKey;
        String cached = redisTemplate.opsForValue().get(cacheKey);
        if (cached != null) {
            return Optional.of(cached);
        }

        LambdaQueryWrapper<TenantConfig> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(TenantConfig::getAppId, appId)
               .eq(TenantConfig::getConfigKey, configKey);
        TenantConfig config = tenantConfigMapper.selectOne(wrapper);

        if (config != null) {
            redisTemplate.opsForValue().set(cacheKey, config.getConfigValue(), CACHE_TTL_HOURS, TimeUnit.HOURS);
            return Optional.of(config.getConfigValue());
        }
        return Optional.empty();
    }

    @Override
    public List<TenantConfig> getConfigsByType(String appId, String configType) {
        LambdaQueryWrapper<TenantConfig> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(TenantConfig::getAppId, appId)
               .eq(TenantConfig::getConfigType, configType);
        return tenantConfigMapper.selectList(wrapper);
    }

    @Override
    public String getConfigValueOrThrow(String appId, String configKey) {
        return getConfigValue(appId, configKey)
                .orElseThrow(() -> new BizException(ResultCode.MODEL_NOT_CONFIGURED));
    }

    @Override
    public void saveConfig(TenantConfig config) {
        tenantConfigMapper.insertOrUpdate(config);
        String cacheKey = CACHE_PREFIX + config.getAppId() + ":" + config.getConfigKey();
        redisTemplate.delete(cacheKey);
    }
}
