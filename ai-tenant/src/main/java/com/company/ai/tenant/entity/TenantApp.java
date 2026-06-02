package com.company.ai.tenant.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("app_info")
public class TenantApp {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String appId;
    private String appName;
    private Integer status;
    private LocalDateTime createdAt;
}
