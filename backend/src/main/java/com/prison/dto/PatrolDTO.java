package com.prison.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class PatrolDTO {
    @NotNull(message = "巡查时间不能为空")
    private LocalDateTime patrolTime;

    @NotNull(message = "巡查警员不能为空")
    private Long guardId;

    @NotNull(message = "巡查监区不能为空")
    private Long areaId;

    @NotBlank(message = "巡查类型不能为空")
    private String patrolType;

    private String result;
    private String description;
}