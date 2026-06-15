package com.prison.dto;

import lombok.Data;

import java.time.LocalDate;
import java.util.List;

@Data
public class ReleaseBoardVO {
    private Long id;
    private String prisonerNumber;
    private String name;
    private String gender;
    private String crimeType;
    private LocalDate releaseDate;
    private Long remainingDays;
    private String warningLevel;
    private Long areaId;
    private String areaName;
    private Long cellId;
    private String dangerLevel;
    private String status;
    private String healthStatus;
    private String remark;
    private List<RecentIncident> recentIncidents;

    @Data
    public static class RecentIncident {
        private Long id;
        private String incidentTitle;
        private String incidentType;
        private String severity;
        private String status;
        private String occurTime;

        public RecentIncident(Long id, String incidentTitle, String incidentType, String severity, String status, String occurTime) {
            this.id = id;
            this.incidentTitle = incidentTitle;
            this.incidentType = incidentType;
            this.severity = severity;
            this.status = status;
            this.occurTime = occurTime;
        }
    }

    @Data
    public static class Stats {
        private int total;
        private int urgent30;
        private int warning60;
        private int notice90;
        private int highDangerCount;
        private int extremeDangerCount;

        public Stats(int total, int urgent30, int warning60, int notice90, int highDangerCount, int extremeDangerCount) {
            this.total = total;
            this.urgent30 = urgent30;
            this.warning60 = warning60;
            this.notice90 = notice90;
            this.highDangerCount = highDangerCount;
            this.extremeDangerCount = extremeDangerCount;
        }
    }

    @Data
    public static class AreaDistribution {
        private String areaName;
        private int count;

        public AreaDistribution(String areaName, int count) {
            this.areaName = areaName;
            this.count = count;
        }
    }

    @Data
    public static class DangerDistribution {
        private String dangerLevel;
        private int count;

        public DangerDistribution(String dangerLevel, int count) {
            this.dangerLevel = dangerLevel;
            this.count = count;
        }
    }

    @Data
    public static class BoardData {
        private Stats stats;
        private List<AreaDistribution> areaDistribution;
        private List<DangerDistribution> dangerDistribution;
        private List<ReleaseBoardVO> prisoners;
    }
}
