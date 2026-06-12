package com.prison.dto;

import lombok.Data;

import java.time.LocalDate;

@Data
public class PatrolAbnormalQueryDTO {

    private LocalDate startDate;
    private LocalDate endDate;
    private String patrolType;
    private Long areaId;
    private Long guardId;

    private Integer page = 1;
    private Integer size = 10;
}
