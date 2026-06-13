package com.prison.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("prisoner_transfers")
public class PrisonerTransfer {
    @TableId(type = IdType.AUTO)
    private Long id;

    private Long prisonerId;

    private String prisonerNumber;

    private String prisonerName;

    private Long fromAreaId;

    private String fromAreaName;

    private Long fromCellId;

    private String fromCellNumber;

    private Long toAreaId;

    private String toAreaName;

    private Long toCellId;

    private String toCellNumber;

    private String transferType;

    private LocalDateTime transferTime;

    private String transferReason;

    private Long operatorId;

    private String operatorName;

    private String remark;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    @TableLogic
    private Integer deleted;
}
