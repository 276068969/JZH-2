package com.prison.vo;

import lombok.Data;

import java.time.LocalDate;

@Data
public class VisitorCalendarVO {
    private Long id;
    private String visitorName;
    private String relation;
    private String visitType;
    private Long prisonerId;
    private String prisonerName;
    private String prisonerNumber;
    private LocalDate visitDate;
    private String visitTimeSlot;
    private String status;
    private Integer visitorCount;
    private String purpose;
}
