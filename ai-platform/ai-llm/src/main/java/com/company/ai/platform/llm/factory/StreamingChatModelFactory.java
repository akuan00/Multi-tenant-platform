package com.company.ai.platform.llm.factory;

import com.company.ai.tenant.service.TenantConfigService;
import dev.langchain4j.model.chat.StreamingChatModel;
import dev.langchain4j.model.openai.OpenAiStreamingChatModel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Component
@RequiredArgsConstructor
public class StreamingChatModelFactory {

    private final TenantConfigService tenantConfigService;

    private final Map<String, StreamingChatModel> modelCache = new ConcurrentHashMap<>();

    public StreamingChatModel getStreamingChatModel(String appId) {
        return modelCache.computeIfAbsent(appId, this::buildStreamingChatModel);
    }

    public void evict(String appId) {
        modelCache.remove(appId);
    }

    private StreamingChatModel buildStreamingChatModel(String appId) {
        String provider = tenantConfigService.getConfigValue(appId, "llm.provider").orElse("openai");
        String apiKey = resolveApiKey(appId, provider);
        String modelName = tenantConfigService.getConfigValue(appId, "llm.model").orElse("gpt-4o");
        String baseUrl = resolveBaseUrl(appId, provider);
        Double temperature = tenantConfigService.getConfigValue(appId, "llm.temperature")
                .map(Double::parseDouble).orElse(0.7);

        log.info("Building streaming chat model for appId={}, provider={}, model={}", appId, provider, modelName);

        return OpenAiStreamingChatModel.builder()
                .apiKey(apiKey)
                .modelName(modelName)
                .baseUrl(baseUrl)
                .temperature(temperature)
                .build();
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
