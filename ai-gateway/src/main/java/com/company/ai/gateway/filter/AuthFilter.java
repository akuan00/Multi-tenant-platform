package com.company.ai.gateway.filter;

import com.company.ai.common.core.exception.BizException;
import com.company.ai.common.core.result.ResultCode;
import com.company.ai.tenant.entity.TenantApp;
import com.company.ai.tenant.mapper.TenantAppMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Set;

@Slf4j
@Component
@Order(1)
@RequiredArgsConstructor
public class AuthFilter implements Filter {

    private final TenantAppMapper tenantAppMapper;

    private static final Set<String> WHITELIST = Set.of(
            "/api/v1/health",
            "/swagger-ui",
            "/v3/api-docs"
    );

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        String path = httpRequest.getRequestURI();

        if (isWhitelisted(path)) {
            chain.doFilter(request, response);
            return;
        }

        String appId = httpRequest.getHeader("X-App-Id");
        if (appId == null || appId.isBlank()) {
            throw new BizException(ResultCode.TENANT_NOT_FOUND);
        }

        LambdaQueryWrapper<TenantApp> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(TenantApp::getAppId, appId).eq(TenantApp::getStatus, 1);
        TenantApp app = tenantAppMapper.selectOne(wrapper);
        if (app == null) {
            throw new BizException(ResultCode.TENANT_NOT_FOUND);
        }

        chain.doFilter(request, response);
    }

    private boolean isWhitelisted(String path) {
        return WHITELIST.stream().anyMatch(path::startsWith);
    }
}
