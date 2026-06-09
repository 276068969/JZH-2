package com.prison.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;

@Data
public class PrisonerDTO {
    @NotBlank(message = "编号不能为空")
    private String prisonerNumber;

    @NotBlank(message = "姓名不能为空")
    private String name;

    @NotBlank(message = "性别不能为空")
    private String gender;

    @NotBlank(message = "身份证号不能为空")
    private String idCard;

    private LocalDate birthDate;
    private String nativePlace;
    private String crimeType;
    private Integer sentenceTerm;
    private LocalDate entryDate;
    private LocalDate releaseDate;
    private Long areaId;
    private Long cellId;
    private String educationLevel;
    private String maritalStatus;
    private String occupation;
    private String healthStatus;
    private String dangerLevel;
    private String status;
    private String photoUrl;
    private String remark;
}