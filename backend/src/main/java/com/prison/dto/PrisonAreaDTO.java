package com.prison.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class PrisonAreaDTO {
    @NotBlank(message = "监区名称不能为空")
    private String areaName;

    @NotBlank(message = "监区编码不能为空")
    private String areaCode;

    @NotBlank(message = "监区类型不能为空")
    private String areaType;

    @NotNull(message = "容量不能为空")
    private Integer capacity;

    private Integer currentPopulation;
    private String address;
    private String description;
    private String status;
}