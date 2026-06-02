package com.company.ai.common.core.util;

import java.util.UUID;

public class TraceIdUtil {
    private static final ThreadLocal<String> TRACE_ID = new ThreadLocal<>();

    public static void setTraceId(String traceId) {
        TRACE_ID.set(traceId);
    }

    public static String getTraceId() {
        String traceId = TRACE_ID.get();
        if (traceId == null) {
            traceId = generateTraceId();
            TRACE_ID.set(traceId);
        }
        return traceId;
    }

    public static void clear() {
        TRACE_ID.remove();
    }

    private static String generateTraceId() {
        return UUID.randomUUID().toString().replace("-", "").substring(0, 16);
    }
}
