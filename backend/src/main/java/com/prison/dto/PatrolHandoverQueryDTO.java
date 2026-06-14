package com.prison.dto;

import lombok.Data;

import java.time.LocalDate;

@Data
public class PatrolHandoverQueryDTO {

    private Integer page = 1;

    private Integer size = 10;

    private Long areaId;

    private String shiftType;

    private String status;

    private Long guardId;

    private LocalDate startDate;

    private LocalDate endDate;
}
