package com.prison.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("patrol_handovers")
public class PatrolHandover {
    @TableId(type = IdType.AUTO)
    private Long id;

    private Long areaId;

    private String areaName;

    private String shiftType;

    private LocalDateTime shiftStartTime;

    private LocalDateTime shiftEndTime;

    private Long outgoingGuardId;

    private String outgoingGuardName;

    private Long incomingGuardId;

    private String incomingGuardName;

    private String keyAreaStatus;

    private String unfinishedItems;

    private String riskPoints;

    private Integer patrolCount;

    private Integer abnormalCount;

    private String status;

    private LocalDateTime handoverTime;

    private String remark;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    @TableLogic
    private Integer deleted;
}
