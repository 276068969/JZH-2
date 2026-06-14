package com.prison.dto;

import lombok.Data;

@Data
public class ObservationListQueryDTO {

    private String keyword;

    private Long areaId;

    private Long cellId;

    private String dangerLevel;

    private String prisonerStatus;

    private String riskCategory;

    private Integer incidentDaysThreshold = 30;

    private String sortField = "riskScore";

    private String sortOrder = "desc";

    private Integer page = 1;

    private Integer size = 10;
}
