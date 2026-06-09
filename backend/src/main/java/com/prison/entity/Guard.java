package com.prison.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("guards")
public class Guard {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String guardNumber;
    private String name;
    private String gender;
    private String idCard;
    private String phone;
    private String email;
    private String position;
    private Long areaId;
    private LocalDate entryDate;
    private String status;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    @TableLogic
    private Integer deleted;
}