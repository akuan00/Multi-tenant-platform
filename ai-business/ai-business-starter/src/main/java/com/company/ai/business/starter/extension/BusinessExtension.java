package com.company.ai.business.starter.extension;

import java.util.Map;

public interface BusinessExtension {
    String getAppId();
    String getDescription();
    void registerWorkflow(Object workflowRegistry);
    Map<String, String> getDefaultConfig();
}
