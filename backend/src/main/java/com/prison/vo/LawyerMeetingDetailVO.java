package com.prison.vo;

import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class LawyerMeetingDetailVO {
    private Long id;
    private String visitorName;
    private String idCard;
    private String phone;
    private String lawyerEmail;
    private Long prisonerId;
    private String prisonerName;
    private String prisonerNumber;
    private LocalDate visitDate;
    private String visitTimeSlot;
    private String status;
    private String statusText;
    private String purpose;
    private String approveRemark;
    private LocalDateTime approveTime;
    private String approveGuardName;

    private String lawyerLicenseNo;
    private LocalDate lawyerLicenseValidDate;
    private Boolean lawyerLicenseExpired;
    private String lawFirmName;
    private String powerOfAttorneyNo;
    private String caseType;
    private String caseTypeText;
    private String meetingStage;
    private String meetingStageText;
    private String meetingSecurityLevel;
    private String meetingSecurityLevelText;
    private String roomTypeRequired;
    private String roomTypeRequiredText;
    private Boolean needsTranslator;
    private Boolean recordingRequired;
    private Boolean isLegalAid;
    private Boolean isUrgentLawyerMeeting;
    private Boolean hasAssistant;
    private String assistantLawyerName;
    private String assistantLawyerLicenseNo;

    private List<VerificationItem> verificationChecklist;

    private LocalDateTime createTime;

    @Data
    public static class VerificationItem {
        private String code;
        private String name;
        private Boolean passed;
        private String missingRemark;

        public VerificationItem(String code, String name, Boolean passed) {
            this.code = code;
            this.name = name;
            this.passed = passed;
        }

        public VerificationItem(String code, String name, Boolean passed, String missingRemark) {
            this.code = code;
            this.name = name;
            this.passed = passed;
            this.missingRemark = missingRemark;
        }
    }
}
