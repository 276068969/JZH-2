package com.prison.dto;

import lombok.Data;

import java.util.List;
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

    private PrisonerTrend prisonerTrend;
    private List<CellDistribution> cellDistribution;
    private List<IncidentCategoryStat> incidentCategoryStats;
    private PatrolWeeklyStats patrolWeeklyStats;

    @Data
    public static class PrisonerTrend {
        private List<String> months;
        private List<Long> prisonerCounts;
        private List<Long> newEntries;
        private List<Long> releases;
    }

    @Data
    public static class CellDistribution {
        private String name;
        private Long value;
        private String color;
    }

    @Data
    public static class IncidentCategoryStat {
        private String type;
        private String label;
        private Long count;
        private String color;
    }

    @Data
    public static class PatrolWeeklyStats {
        private List<String> days;
        private List<Long> planned;
        private List<Long> completed;
    }

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
