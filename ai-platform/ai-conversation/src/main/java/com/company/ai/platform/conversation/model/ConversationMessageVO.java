package com.company.ai.platform.conversation.model;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ConversationMessageVO {
    private Long id;
    private String role;
    private String content;
    private Integer tokens;
    private LocalDateTime createdAt;
}
