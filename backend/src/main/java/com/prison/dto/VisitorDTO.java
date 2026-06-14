package com.prison.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;

@Data
public class VisitorDTO {
    @NotBlank(message = "访客姓名不能为空")
    private String visitorName;

    @NotBlank(message = "身份证号不能为空")
    private String idCard;

    private String phone;

    @NotBlank(message = "与服刑人员关系不能为空")
    private String relation;

    @NotNull(message = "服刑人员不能为空")
    private Long prisonerId;

    @NotNull(message = "探视日期不能为空")
    private LocalDate visitDate;

    @NotBlank(message = "探视时段不能为空")
    private String visitTimeSlot;

    private String status;
    private String idCardPhoto;
    private Integer visitorCount;
    private String purpose;
    private Long approveGuardId;
    private String approveRemark;
    private String visitType;
    private String remark;

    private String lawyerLicenseNo;
    private String lawFirmName;
    private String powerOfAttorneyNo;
    private String caseType;
    private Boolean needsTranslator;
    private Boolean recordingRequired;

    private java.time.LocalDate lawyerLicenseValidDate;
    private Boolean isLegalAid;
    private String assistantLawyerName;
    private String assistantLawyerLicenseNo;
    private String meetingSecurityLevel;
    private Boolean isUrgentLawyerMeeting;
    private String lawyerEmail;
    private String meetingStage;
    private String roomTypeRequired;
    private Boolean hasAssistant;
}

