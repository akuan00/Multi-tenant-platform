package com.company.ai.platform.agent.memory;

import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.store.memory.chat.ChatMemoryStore;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class RedisChatMemoryStore implements ChatMemoryStore {

    private static final String KEY_PREFIX = "agent:memory:";
    private static final Duration TTL = Duration.ofHours(24);

    private final StringRedisTemplate redisTemplate;
    private final ChatMessageCodec messageCodec;

    @Override
    public List<ChatMessage> getMessages(Object memoryId) {
        String key = key(memoryId);
        String json = redisTemplate.opsForValue().get(key);
        if (json == null || json.isEmpty()) {
            return List.of();
        }
        List<ChatMessage> messages = messageCodec.fromJson(json);
        log.debug("Loaded {} messages from Redis key={}", messages.size(), key);
        return messages;
    }

    @Override
    public void updateMessages(Object memoryId, List<ChatMessage> messages) {
        String key = key(memoryId);
        String json = messageCodec.toJson(messages);
        redisTemplate.opsForValue().set(key, json, TTL);
        log.debug("Updated {} messages to Redis key={}", messages.size(), key);
    }

    @Override
    public void deleteMessages(Object memoryId) {
        String key = key(memoryId);
        redisTemplate.delete(key);
        log.debug("Deleted messages from Redis key={}", key);
    }

    private String key(Object memoryId) {
        return KEY_PREFIX + memoryId;
    }
}
