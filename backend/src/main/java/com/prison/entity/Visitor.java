package com.prison.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("visitors")
public class Visitor {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String visitorName;
    private String idCard;
    private String phone;
    private String relation;
    private Long prisonerId;
    private LocalDate visitDate;
    private String visitTimeSlot;
    private String status;
    @TableField(exist = false)
    private String idCardPhoto;
    @TableField(exist = false)
    private Integer visitorCount;
    @TableField(exist = false)
    private String purpose;
    @TableField(exist = false)
    private Long approveGuardId;
    @TableField(exist = false)
    private String approveRemark;
    @TableField(exist = false)
    private LocalDateTime approveTime;
    @TableField(exist = false)
    private String visitType;
    @TableField(exist = false)
    private LocalDateTime actualStartTime;
    @TableField(exist = false)
    private LocalDateTime actualEndTime;
    @TableField(exist = false)
    private String remark;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    @TableLogic
    private Integer deleted;
}