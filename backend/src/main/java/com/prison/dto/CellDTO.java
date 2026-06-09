package com.prison.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CellDTO {
    @NotBlank(message = "监舍编号不能为空")
    private String cellNumber;

    @NotNull(message = "所属监区不能为空")
    private Long areaId;

    @NotBlank(message = "监舍类型不能为空")
    private String cellType;

    @NotNull(message = "容量不能为空")
    private Integer capacity;

    private Integer currentOccupancy;
    private String status;
}