package com.company.ai.platform.agent.memory;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.data.message.SystemMessage;
import dev.langchain4j.data.message.UserMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class ChatMessageCodec {

    private final ObjectMapper objectMapper;

    private static final String TYPE_SYSTEM = "system";
    private static final String TYPE_USER = "user";
    private static final String TYPE_AI = "ai";

    public String toJson(List<ChatMessage> messages) {
        List<Map<String, String>> list = messages.stream().map(msg -> {
            if (msg instanceof SystemMessage sm) {
                return Map.of("type", TYPE_SYSTEM, "content", sm.text());
            } else if (msg instanceof UserMessage um) {
                return Map.of("type", TYPE_USER, "content", um.singleText());
            } else if (msg instanceof AiMessage am) {
                return Map.of("type", TYPE_AI, "content", am.text() != null ? am.text() : "");
            }
            return Map.of("type", "unknown", "content", "");
        }).toList();
        try {
            return objectMapper.writeValueAsString(list);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to serialize chat messages", e);
        }
    }

    public List<ChatMessage> fromJson(String json) {
        try {
            List<Map<String, String>> list = objectMapper.readValue(json,
                    objectMapper.getTypeFactory().constructCollectionType(List.class, Map.class));
            List<ChatMessage> messages = new ArrayList<>();
            for (Map<String, String> item : list) {
                String type = item.get("type");
                String content = item.get("content");
                switch (type) {
                    case TYPE_SYSTEM -> messages.add(SystemMessage.from(content));
                    case TYPE_USER -> messages.add(UserMessage.from(content));
                    case TYPE_AI -> messages.add(AiMessage.from(content));
                    default -> log.warn("Unknown message type in Redis: {}", type);
                }
            }
            return messages;
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to deserialize chat messages", e);
        }
    }
}
