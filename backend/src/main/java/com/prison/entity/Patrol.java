package com.prison.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("patrols")
public class Patrol {
    @TableId(type = IdType.AUTO)
    private Long id;
    private LocalDateTime patrolTime;
    private Long guardId;
    private Long areaId;
    private String patrolType;
    private String result;
    private String description;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    @TableLogic
    private Integer deleted;
}