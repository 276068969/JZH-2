package com.prison.dto;

import lombok.Data;

@Data
public class SysLogQueryDTO {

    private Integer page;

    private Integer size;

    private String keyword;

    private String module;

    private String action;

    private String status;

    private String operatorUsername;

    private String targetType;
}
