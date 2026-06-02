package com.company.ai.gateway.filter;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

@Slf4j
@Component
@Order(2)
@RequiredArgsConstructor
public class RateLimitFilter implements Filter {

    private final StringRedisTemplate redisTemplate;
    private static final int MAX_REQUESTS = 100;
    private static final int WINDOW_SECONDS = 60;

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        String appId = httpRequest.getHeader("X-App-Id");

        if (appId != null && !appId.isBlank()) {
            String key = "ratelimit:" + appId;
            String countStr = redisTemplate.opsForValue().get(key);
            int count = countStr != null ? Integer.parseInt(countStr) : 0;

            if (count >= MAX_REQUESTS) {
                log.warn("Rate limit exceeded for appId={}", appId);
                ((HttpServletResponse) response).sendError(429, "Rate limit exceeded");
                return;
            }

            if (count == 0) {
                redisTemplate.opsForValue().set(key, "1", WINDOW_SECONDS, TimeUnit.SECONDS);
            } else {
                redisTemplate.opsForValue().increment(key);
            }
        }

        chain.doFilter(request, response);
    }
}
