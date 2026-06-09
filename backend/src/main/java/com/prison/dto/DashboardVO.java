package com.prison.dto;

import lombok.Data;

import java.util.Map;

@Data
public class DashboardVO {
    private Long prisonerCount;
    private Long guardCount;
    private Double cellUsageRate;
    private Long todayPatrolCount;
    private Long pendingIncidentCount;
    private Long todayVisitorCount;

    public static DashboardVO of(Long prisonerCount, Long guardCount, Double cellUsageRate,
                                  Long todayPatrolCount, Long pendingIncidentCount, Long todayVisitorCount) {
        DashboardVO vo = new DashboardVO();
        vo.setPrisonerCount(prisonerCount);
        vo.setGuardCount(guardCount);
        vo.setCellUsageRate(cellUsageRate);
        vo.setTodayPatrolCount(todayPatrolCount);
        vo.setPendingIncidentCount(pendingIncidentCount);
        vo.setTodayVisitorCount(todayVisitorCount);
        return vo;
    }
}