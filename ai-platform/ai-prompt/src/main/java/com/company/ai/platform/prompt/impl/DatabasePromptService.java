package com.company.ai.platform.prompt.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.company.ai.platform.prompt.PromptService;
import com.company.ai.platform.prompt.mapper.PromptTemplateMapper;
import com.company.ai.platform.prompt.model.PromptTemplate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class DatabasePromptService implements PromptService {

    private final PromptTemplateMapper promptTemplateMapper;
    private final StringRedisTemplate redisTemplate;

    private static final String CACHE_PREFIX = "prompt:";
    private static final long CACHE_TTL_HOURS = 1;

    @Override
    public String getSystemPrompt(String appId, String scene) {
        String cacheKey = CACHE_PREFIX + appId + ":" + scene;
        String cached = redisTemplate.opsForValue().get(cacheKey);
        if (cached != null) {
            return cached;
        }

        LambdaQueryWrapper<PromptTemplate> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(PromptTemplate::getAppId, appId)
               .eq(PromptTemplate::getScene, scene);
        PromptTemplate template = promptTemplateMapper.selectOne(wrapper);

        if (template == null) {
            return "";
        }

        redisTemplate.opsForValue().set(cacheKey, template.getContent(), CACHE_TTL_HOURS, TimeUnit.HOURS);
        return template.getContent();
    }

    @Override
    public String renderPrompt(String appId, String scene, Map<String, String> variables) {
        String template = getSystemPrompt(appId, scene);
        if (template.isEmpty() || variables == null) {
            return template;
        }
        for (Map.Entry<String, String> entry : variables.entrySet()) {
            template = template.replace("{{" + entry.getKey() + "}}", entry.getValue());
        }
        return template;
    }
}
