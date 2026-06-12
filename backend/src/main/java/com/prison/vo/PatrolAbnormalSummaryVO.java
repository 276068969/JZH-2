package com.prison.vo;

import lombok.Data;

import java.util.List;

@Data
public class PatrolAbnormalSummaryVO {

    private Long total;

    private List<TypeStat> byType;
    private List<AreaStat> byArea;
    private List<GuardStat> byGuard;
    private List<HourStat> byHour;

    private PageData<AbnormalRecord> records;

    @Data
    public static class TypeStat {
        private String patrolType;
        private Long count;
    }

    @Data
    public static class AreaStat {
        private Long areaId;
        private String areaName;
        private Long count;
    }

    @Data
    public static class GuardStat {
        private Long guardId;
        private String guardName;
        private Long count;
    }

    @Data
    public static class HourStat {
        private Integer hour;
        private Long count;
    }

    @Data
    public static class AbnormalRecord {
        private Long id;
        private String patrolTime;
        private String patrolType;
        private String result;
        private String description;
        private Long guardId;
        private String guardName;
        private Long areaId;
        private String areaName;
    }

    @Data
    public static class PageData<T> {
        private List<T> list;
        private Long total;
        private Integer page;
        private Integer size;
    }
}
