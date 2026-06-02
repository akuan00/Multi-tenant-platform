package com.company.ai.platform.llm.factory;

import com.company.ai.common.core.exception.BizException;
import com.company.ai.common.core.result.ResultCode;
import com.company.ai.tenant.service.TenantConfigService;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.openai.OpenAiChatModel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Component
@RequiredArgsConstructor
public class ChatModelFactory {

    private final TenantConfigService tenantConfigService;

    private final Map<String, ChatModel> modelCache = new ConcurrentHashMap<>();

    public ChatModel getChatModel(String appId) {
        return modelCache.computeIfAbsent(appId, this::buildChatModel);
    }

    public void evict(String appId) {
        modelCache.remove(appId);
        log.info("Evicted chat model cache for appId={}", appId);
    }

    public void evictAll() {
        modelCache.clear();
        log.info("Evicted all chat model cache");
    }

    private ChatModel buildChatModel(String appId) {
        String provider = tenantConfigService.getConfigValue(appId, "llm.provider").orElse("openai");
        String apiKey = resolveApiKey(appId, provider);
        String modelName = tenantConfigService.getConfigValue(appId, "llm.model").orElse("gpt-4o");
        String baseUrl = resolveBaseUrl(appId, provider);
        Double temperature = tenantConfigService.getConfigValue(appId, "llm.temperature")
                .map(Double::parseDouble).orElse(0.7);
        Double topP = tenantConfigService.getConfigValue(appId, "llm.topP")
                .map(Double::parseDouble).orElse(null);
        Integer maxTokens = tenantConfigService.getConfigValue(appId, "llm.maxTokens")
                .map(Integer::parseInt).orElse(null);

        log.info("Building chat model for appId={}, provider={}, model={}", appId, provider, modelName);

        var builder = OpenAiChatModel.builder()
                .apiKey(apiKey)
                .modelName(modelName)
                .baseUrl(baseUrl)
                .temperature(temperature);

        if (topP != null) {
            builder.topP(topP);
        }
        if (maxTokens != null) {
            builder.maxTokens(maxTokens);
        }

        return builder.build();
    }

    private String resolveApiKey(String appId, String provider) {
        return tenantConfigService.getConfigValue(appId, "llm.apiKey")
                .orElseGet(() -> switch (provider.toLowerCase()) {
                    case "qwen", "dashscope" -> System.getenv("DASHSCOPE_API_KEY");
                    case "doubao", "volcengine" -> System.getenv("VOLCENGINE_API_KEY");
                    default -> System.getenv("OPENAI_API_KEY");
                });
    }

    private String resolveBaseUrl(String appId, String provider) {
        return tenantConfigService.getConfigValue(appId, "llm.baseUrl")
                .orElseGet(() -> switch (provider.toLowerCase()) {
                    case "qwen", "dashscope" -> "https://dashscope.aliyuncs.com/compatible-mode/v1";
                    case "doubao", "volcengine" -> "https://ark.cn-beijing.volces.com/api/v3";
                    default -> System.getenv().getOrDefault("OPENAI_BASE_URL", "https://api.openai.com/v1");
                });
    }
}
