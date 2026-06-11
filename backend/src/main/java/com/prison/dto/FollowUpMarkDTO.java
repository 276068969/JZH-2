package com.prison.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;

@Data
public class FollowUpMarkDTO {

    @NotNull(message = "医疗记录ID不能为空")
    private Long medicalRecordId;

    @NotNull(message = "复诊状态不能为空")
    private String followUpStatus;

    private LocalDate actualFollowUpDate;

    private String followUpResult;

    private String followUpRemark;

    private LocalDate nextFollowUpDate;
}
