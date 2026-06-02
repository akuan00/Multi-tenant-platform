package com.company.ai.platform.prompt;

import java.util.Map;

public interface PromptService {
    String getSystemPrompt(String appId, String scene);
    String renderPrompt(String appId, String scene, Map<String, String> variables);
}
