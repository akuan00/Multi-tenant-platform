package com.company.ai.platform.agent.memory;

import dev.langchain4j.memory.ChatMemory;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.store.memory.chat.ChatMemoryStore;
import dev.langchain4j.store.memory.chat.InMemoryChatMemoryStore;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Component
public class AgentMemoryManager {

    private static final int DEFAULT_MAX_MESSAGES = 20;

    private final ChatMemoryStore memoryStore = new InMemoryChatMemoryStore();
    private final Map<String, ChatMemory> chatMemories = new ConcurrentHashMap<>();

    public ChatMemory getOrCreateMemory(String appId, String sessionId) {
        String memoryKey = appId + ":" + sessionId;
        return chatMemories.computeIfAbsent(memoryKey, key ->
                MessageWindowChatMemory.builder()
                        .id(memoryKey)
                        .chatMemoryStore(memoryStore)
                        .maxMessages(DEFAULT_MAX_MESSAGES)
                        .build()
        );
    }

    public void clearMemory(String appId, String sessionId) {
        String memoryKey = appId + ":" + sessionId;
        ChatMemory memory = chatMemories.remove(memoryKey);
        if (memory != null) {
            memory.clear();
            log.info("Cleared memory for appId={}, sessionId={}", appId, sessionId);
        }
    }

    public int getMessageCount(String appId, String sessionId) {
        String memoryKey = appId + ":" + sessionId;
        ChatMemory memory = chatMemories.get(memoryKey);
        return memory != null ? memory.messages().size() : 0;
    }
}
