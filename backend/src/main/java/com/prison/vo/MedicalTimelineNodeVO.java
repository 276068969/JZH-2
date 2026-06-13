package com.prison.vo;

import lombok.Data;

import java.time.LocalDate;

@Data
public class MedicalTimelineNodeVO {

    private Long recordId;

    private LocalDate eventDate;

    private String nodeType;

    private String medicalType;
    private String medicalTypeLabel;

    private String diagnosis;
    private String treatment;
    private String hospital;
    private String doctorName;

    private String result;
    private String resultLabel;
    private String resultTagType;

    private String medicine;

    private LocalDate followUpDate;
    private String followUpStatus;
    private String followUpStatusLabel;
    private String followUpTagType;
    private LocalDate actualFollowUpDate;
    private String followUpResult;
    private String followUpRemark;

    private String color;
    private String icon;
}
