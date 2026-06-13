package com.prison.dto;

import lombok.Data;

import java.time.LocalDate;

@Data
public class VisitorCalendarQueryDTO {
    private LocalDate startDate;
    private LocalDate endDate;
    private String status;
    private String visitType;
}
