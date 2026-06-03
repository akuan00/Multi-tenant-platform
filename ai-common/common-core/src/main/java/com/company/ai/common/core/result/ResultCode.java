package com.company.ai.common.core.result;

import lombok.Getter;
import lombok.AllArgsConstructor;

@Getter
@AllArgsConstructor
public enum ResultCode {
    SUCCESS(200, "success"),
    BAD_REQUEST(400, "bad request"),
    UNAUTHORIZED(401, "unauthorized"),
    FORBIDDEN(403, "forbidden"),
    NOT_FOUND(404, "not found"),
    TOO_MANY_REQUESTS(429, "too many requests"),
    INTERNAL_ERROR(500, "internal error"),
    TENANT_NOT_FOUND(1001, "tenant not found"),
    MODEL_NOT_CONFIGURED(1002, "model not configured for this tenant"),
    KNOWLEDGE_NOT_FOUND(1003, "knowledge base not found"),
    WORKFLOW_NOT_FOUND(1004, "workflow not found"),
    CONVERSATION_NOT_FOUND(1005, "conversation not found");

    private final int code;
    private final String message;
}
