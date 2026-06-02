package com.company.ai.platform.prompt.model;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("prompt_template")
public class PromptTemplate {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String appId;
    private String scene;
    private String content;
    private String variables;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
