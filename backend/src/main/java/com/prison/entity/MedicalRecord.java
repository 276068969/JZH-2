package com.prison.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("medical_records")
public class MedicalRecord {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long prisonerId;
    private LocalDate recordDate;
    private String diagnosis;
    private String treatment;
    private String hospital;
    private String doctorName;
    private String medicalType;
    private String result;
    private String medicine;
    private LocalDate followUpDate;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    @TableLogic
    private Integer deleted;
}