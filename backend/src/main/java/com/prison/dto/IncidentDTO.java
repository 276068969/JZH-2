package com.prison.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class IncidentDTO {
    @NotBlank(message = "事件标题不能为空")
    private String incidentTitle;

    @NotBlank(message = "事件类型不能为空")
    private String incidentType;

    @NotBlank(message = "严重程度不能为空")
    private String severity;

    private Long areaId;
    private Long reportGuardId;
    private Long relatedPrisonerId;

    @NotNull(message = "发生时间不能为空")
    private LocalDateTime occurTime;

    private String description;
    private String handlerResult;
    private String status;
}