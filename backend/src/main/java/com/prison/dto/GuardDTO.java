package com.prison.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.time.LocalDate;

@Data
public class GuardDTO {
    @NotBlank(message = "警号不能为空")
    private String guardNumber;

    @NotBlank(message = "姓名不能为空")
    private String name;

    @NotBlank(message = "性别不能为空")
    private String gender;

    @NotBlank(message = "身份证号不能为空")
    private String idCard;

    @NotBlank(message = "电话不能为空")
    private String phone;

    private String email;
    private String position;
    private Long areaId;
    private LocalDate entryDate;
    private String status;
}