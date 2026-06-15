package com.prison.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("sys_logs")
public class SysLog {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String operatorUsername;

    private String operatorRealName;

    private String module;

    private String action;

    private String detail;

    private String targetType;

    private Long targetId;

    private String targetName;

    private String ipAddress;

    private String status;

    private String failReason;

    private String requestMethod;

    private String requestUrl;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableLogic
    private Integer deleted;
}
