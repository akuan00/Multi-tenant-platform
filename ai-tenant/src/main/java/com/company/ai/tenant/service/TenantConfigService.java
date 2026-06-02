package com.company.ai.tenant.service;

import com.company.ai.tenant.entity.TenantConfig;
import java.util.List;
import java.util.Optional;

public interface TenantConfigService {
    Optional<String> getConfigValue(String appId, String configKey);
    List<TenantConfig> getConfigsByType(String appId, String configType);
    String getConfigValueOrThrow(String appId, String configKey);
    void saveConfig(TenantConfig config);
}
