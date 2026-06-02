package com.company.ai.business.starter.controller;

import com.company.ai.common.core.result.R;
import com.company.ai.tenant.context.TenantContext;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/v1")
public class HealthController {

    @GetMapping("/health")
    public R<Map<String, Object>> health() {
        String appId = TenantContext.getAppId();
        return R.ok(Map.of(
                "status", "UP",
                "appId", appId != null ? appId : "unknown",
                "timestamp", java.time.Instant.now().toString()
        ));
    }
}
