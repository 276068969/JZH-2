package com.prison.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class PrisonerTransferQueryDTO {

    private Integer page = 1;

    private Integer size = 10;

    private String keyword;

    private Long prisonerId;

    private Long fromAreaId;

    private Long toAreaId;

    private Long fromCellId;

    private Long toCellId;

    private String transferType;

    private LocalDateTime startTime;

    private LocalDateTime endTime;
}
