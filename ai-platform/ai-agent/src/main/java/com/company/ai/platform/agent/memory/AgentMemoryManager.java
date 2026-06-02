package com.company.ai.platform.agent.memory;

import dev.langchain4j.memory.ChatMemory;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.store.memory.chat.ChatMemoryStore;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class AgentMemoryManager {

    private static final int DEFAULT_MAX_MESSAGES = 20;

    private final ChatMemoryStore memoryStore;

    public ChatMemory getOrCreateMemory(String appId, String sessionId) {
        String memoryKey = appId + ":" + sessionId;
        return MessageWindowChatMemory.builder()
                .id(memoryKey)
                .chatMemoryStore(memoryStore)
                .maxMessages(DEFAULT_MAX_MESSAGES)
                .build();
    }

    public void clearMemory(String appId, String sessionId) {
        String memoryKey = appId + ":" + sessionId;
        memoryStore.deleteMessages(memoryKey);
        log.info("Cleared memory for appId={}, sessionId={}", appId, sessionId);
    }

    public int getMessageCount(String appId, String sessionId) {
        String memoryKey = appId + ":" + sessionId;
        return memoryStore.getMessages(memoryKey).size();
    }
}
