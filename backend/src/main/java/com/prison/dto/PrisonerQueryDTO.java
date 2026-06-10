package com.prison.dto;

import lombok.Data;

@Data
public class PrisonerQueryDTO {
    private String keyword;
    private Long areaId;
    private String dangerLevel;
    private String status;
    private String gender;
    private String crimeType;
    private Integer minAge;
    private Integer maxAge;
    private Integer page = 1;
    private Integer size = 10;
}
