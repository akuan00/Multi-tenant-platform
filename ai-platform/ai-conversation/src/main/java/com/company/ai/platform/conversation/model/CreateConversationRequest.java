package com.company.ai.platform.conversation.model;

import lombok.Data;

@Data
public class CreateConversationRequest {
    private String userId;
    private String title;
}
