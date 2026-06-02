package com.company.ai.platform.knowledge.model;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class DocumentVO {
    private Long id;
    private String title;
    private String fileType;
    private Integer chunkCount;
    private String status;
    private LocalDateTime createdAt;
}
