package com.company.ai.platform.workflow.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("workflow_definition")
public class WorkflowDefinitionEntity {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String appId;
    private String workflowName;
    private String graphConfig;
    private LocalDateTime createdAt;
}
