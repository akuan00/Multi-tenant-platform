package com.company.ai.tenant.context;

public class TenantContext {
    private static final ThreadLocal<String> APP_ID = new ThreadLocal<>();

    public static void setAppId(String appId) {
        APP_ID.set(appId);
    }

    public static String getAppId() {
        return APP_ID.get();
    }

    public static void clear() {
        APP_ID.remove();
    }
}
