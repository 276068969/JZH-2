package com.prison.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("visitors")
public class Visitor {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String visitorName;
    private String idCard;
    private String phone;
    private String relation;
    private Long prisonerId;
    private LocalDate visitDate;
    private String visitTimeSlot;
    private String status;
    @TableField(exist = false)
    private String idCardPhoto;
    @TableField(exist = false)
    private Integer visitorCount;
    @TableField(exist = false)
    private String purpose;
    @TableField(exist = false)
    private Long approveGuardId;
    @TableField(exist = false)
    private String approveRemark;
    @TableField(exist = false)
    private LocalDateTime approveTime;
    @TableField(exist = false)
    private String visitType;
    @TableField(exist = false)
    private LocalDateTime actualStartTime;
    @TableField(exist = false)
    private LocalDateTime actualEndTime;
    @TableField(exist = false)
    private String remark;

    @TableField(exist = false)
    private Boolean isLawyerVisit;

    @TableField(exist = false)
    private String statusText;

    @TableField(exist = false)
    private String caseTypeText;

    @TableField(exist = false)
    private String meetingStageText;

    @TableField(exist = false)
    private String meetingSecurityLevelText;

    @TableField(exist = false)
    private String roomTypeText;

    @TableField(exist = false)
    private Boolean lawyerLicenseExpired;

    private String lawyerLicenseNo;
    private String lawFirmName;
    private String powerOfAttorneyNo;
    private String caseType;
    private Boolean needsTranslator;
    private Boolean recordingRequired;

    private LocalDate lawyerLicenseValidDate;
    private Boolean isLegalAid;
    private String assistantLawyerName;
    private String assistantLawyerLicenseNo;
    private String meetingSecurityLevel;
    private Boolean isUrgentLawyerMeeting;
    private String lawyerEmail;
    private String meetingStage;
    private String roomTypeRequired;
    private Boolean hasAssistant;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    @TableLogic
    private Integer deleted;
}