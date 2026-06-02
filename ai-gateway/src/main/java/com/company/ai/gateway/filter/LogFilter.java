package com.company.ai.gateway.filter;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Slf4j
@Component
@Order(3)
public class LogFilter implements Filter {

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        long startTime = System.currentTimeMillis();

        try {
            chain.doFilter(request, response);
        } finally {
            long duration = System.currentTimeMillis() - startTime;
            log.info("{} {} appId={} duration={}ms status={}",
                    httpRequest.getMethod(),
                    httpRequest.getRequestURI(),
                    MDC.get("appId"),
                    duration,
                    ((jakarta.servlet.http.HttpServletResponse) response).getStatus());
        }
    }
}
