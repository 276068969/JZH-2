package com.prison.dto;

import lombok.Data;

import java.time.LocalDate;

@Data
public class FollowUpQueryDTO {

    private String keyword;
    private String followUpStatus;
    private LocalDate followUpStartDate;
    private LocalDate followUpEndDate;
    private Long areaId;
    private String dangerLevel;
    private Boolean onlyKeyAttention;
    private String prisonerStatus;
    private String activeFilter;

    private Integer page = 1;
    private Integer size = 10;
}
