package com.prison.vo;

import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

@Data
public class ObservationListVO {

    private Long prisonerId;

    private String prisonerNumber;

    private String prisonerName;

    private String gender;

    private Integer age;

    private String crimeType;

    private String prisonerStatus;

    private LocalDate entryDate;

    private LocalDate releaseDate;

    private Long areaId;

    private String areaName;

    private Long cellId;

    private String cellNumber;

    private String dangerLevel;

    private Set<String> riskCategories;

    private List<String> riskReasons;

    private Integer riskScore;

    private Integer recentIncidentCount;

    private Integer unresolvedHighIncidentCount;

    private LocalDateTime lastIncidentTime;

    private String lastIncidentType;

    private String lastIncidentSeverity;

    private Integer ongoingTreatmentCount;

    private LocalDate lastMedicalDate;

    private String lastDiagnosis;

    private String lastTreatmentStatus;
}
