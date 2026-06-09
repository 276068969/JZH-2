package com.prison.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("prisoners")
public class Prisoner {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String prisonerNumber;
    private String name;
    private String gender;
    private String idCard;
    private LocalDate birthDate;
    private String nativePlace;
    private String crimeType;
    private Integer sentenceTerm;
    private LocalDate entryDate;
    private LocalDate releaseDate;
    private Long areaId;
    private Long cellId;
    private String educationLevel;
    private String maritalStatus;
    private String occupation;
    private String healthStatus;
    private String dangerLevel;
    private String status;
    private String photoUrl;
    private String remark;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    @TableLogic
    private Integer deleted;
}