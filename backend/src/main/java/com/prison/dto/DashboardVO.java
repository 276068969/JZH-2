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
    private Long pendingVisitorCount;
    private Long inProgressVisitorCount;

    public static DashboardVO of(Long prisonerCount, Long guardCount, Double cellUsageRate,
                                  Long todayPatrolCount, Long pendingIncidentCount, Long todayVisitorCount,
                                  Long pendingVisitorCount, Long inProgressVisitorCount) {
        DashboardVO vo = new DashboardVO();
        vo.setPrisonerCount(prisonerCount);
        vo.setGuardCount(guardCount);
        vo.setCellUsageRate(cellUsageRate);
        vo.setTodayPatrolCount(todayPatrolCount);
        vo.setPendingIncidentCount(pendingIncidentCount);
        vo.setTodayVisitorCount(todayVisitorCount);
        vo.setPendingVisitorCount(pendingVisitorCount);
        vo.setInProgressVisitorCount(inProgressVisitorCount);
        return vo;
    }
}