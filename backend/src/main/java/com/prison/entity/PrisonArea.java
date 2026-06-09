package com.prison.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("prison_areas")
public class PrisonArea {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String areaName;
    private String areaCode;
    private String areaType;
    private Integer capacity;
    private Integer currentPopulation;
    private String address;
    private String description;
    private String status;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    @TableLogic
    private Integer deleted;
}