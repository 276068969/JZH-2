package com.prison.vo;

import lombok.Data;

import java.time.LocalDate;

@Data
public class FollowUpWorkbenchVO {

    private Long id;

    private Long prisonerId;
    private String prisonerNumber;
    private String prisonerName;
    private String gender;
    private Long areaId;
    private String areaName;
    private Long cellId;
    private String cellNumber;
    private String dangerLevel;
    private String prisonerStatus;

    private LocalDate recordDate;
    private String diagnosis;
    private String treatment;
    private String hospital;
    private String doctorName;
    private String medicalType;
    private String result;
    private String medicine;

    private LocalDate followUpDate;
    private String followUpStatus;
    private LocalDate actualFollowUpDate;
    private String followUpResult;
    private String followUpRemark;

    private Integer missedFollowUpCount;
    private Boolean isKeyAttention;
    private String keyAttentionReason;

    private Long daysUntilFollowUp;
    private Long daysOverdue;
}
