package com.company.ai.tenant.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("tenant_config")
public class TenantConfig {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String appId;
    private String configKey;
    private String configValue;
    private String configType;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
