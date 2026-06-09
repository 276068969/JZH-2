package com.prison.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("incidents")
public class Incident {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String incidentTitle;
    private String incidentType;
    private String severity;
    private Long areaId;
    private Long reportGuardId;
    private Long relatedPrisonerId;
    private LocalDateTime occurTime;
    private String description;
    private String handlerResult;
    private String status;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    @TableLogic
    private Integer deleted;
}