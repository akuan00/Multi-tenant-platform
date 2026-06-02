package com.company.ai.tenant.interceptor;

import com.company.ai.common.core.exception.BizException;
import com.company.ai.common.core.result.ResultCode;
import com.company.ai.common.core.util.TraceIdUtil;
import com.company.ai.tenant.context.TenantContext;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Slf4j
@Component
public class TenantInterceptor implements HandlerInterceptor {

    private static final String APP_ID_HEADER = "X-App-Id";

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        String appId = request.getHeader(APP_ID_HEADER);
        if (appId == null || appId.isBlank()) {
            throw new BizException(ResultCode.TENANT_NOT_FOUND);
        }
        TenantContext.setAppId(appId);
        TraceIdUtil.setTraceId(generateTraceId());
        MDC.put("appId", appId);
        MDC.put("traceId", TraceIdUtil.getTraceId());
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        TenantContext.clear();
        TraceIdUtil.clear();
        MDC.clear();
    }

    private String generateTraceId() {
        return java.util.UUID.randomUUID().toString().replace("-", "").substring(0, 16);
    }
}
