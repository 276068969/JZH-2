package com.prison.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("cells")
public class Cell {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String cellNumber;
    private Long areaId;
    private String cellType;
    private Integer capacity;
    private Integer currentOccupancy;
    private String status;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    @TableLogic
    private Integer deleted;
}