package com.prison.vo;

import lombok.Data;

import java.time.LocalDate;
import java.util.List;

@Data
public class MedicalTimelineVO {

    private Long prisonerId;
    private String prisonerNumber;
    private String prisonerName;
    private String gender;
    private LocalDate birthDate;
    private String idCard;
    private String nativePlace;
    private Long areaId;
    private String areaName;
    private Long cellId;
    private String cellNumber;
    private String dangerLevel;
    private String healthStatus;
    private String prisonerStatus;

    private Long totalRecords;
    private Long treatingCount;
    private Long recoveredCount;
    private Long followUpPendingCount;
    private Long followUpMissedCount;

    private List<MedicalTimelineNodeVO> nodes;
}
