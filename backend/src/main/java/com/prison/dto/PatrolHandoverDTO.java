package com.prison.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class PatrolHandoverDTO {

    @NotNull(message = "监区不能为空")
    private Long areaId;

    @NotBlank(message = "监区名称不能为空")
    private String areaName;

    @NotBlank(message = "班次类型不能为空")
    private String shiftType;

    @NotNull(message = "班次开始时间不能为空")
    private LocalDateTime shiftStartTime;

    @NotNull(message = "班次结束时间不能为空")
    private LocalDateTime shiftEndTime;

    @NotNull(message = "交班警员不能为空")
    private Long outgoingGuardId;

    @NotBlank(message = "交班警员姓名不能为空")
    private String outgoingGuardName;

    private Long incomingGuardId;

    private String incomingGuardName;

    private String keyAreaStatus;

    private String unfinishedItems;

    private String riskPoints;

    private Integer patrolCount;

    private Integer abnormalCount;

    private String remark;
}
