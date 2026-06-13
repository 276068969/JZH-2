package com.prison.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class PrisonerTransferDTO {

    @NotNull(message = "服刑人员ID不能为空")
    private Long prisonerId;

    private Long fromAreaId;

    private Long fromCellId;

    private Long toAreaId;

    private Long toCellId;

    private String transferType;

    private LocalDateTime transferTime;

    private String transferReason;

    private Long operatorId;

    private String operatorName;

    private String remark;
}
