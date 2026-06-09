package com.prison.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;

@Data
public class MedicalRecordDTO {
    @NotNull(message = "服刑人员不能为空")
    private Long prisonerId;

    @NotNull(message = "就诊日期不能为空")
    private LocalDate recordDate;

    private String diagnosis;
    private String treatment;
    private String hospital;
    private String doctorName;
    private String medicalType;
    private String result;
    private String medicine;
    private LocalDate followUpDate;
}