package com.prison.vo;

import lombok.Data;

import java.util.Map;

@Data
public class ObservationStatsVO {

    private Long totalObserved;

    private Long byDangerLevelCount;

    private Long byRecentIncidentCount;

    private Long byOngoingTreatmentCount;

    private Long multipleRiskCount;

    private Long extremeDangerCount;

    private Long highDangerCount;

    private Long unresolvedHighIncidentTotal;

    private Long ongoingTreatmentTotal;

    private Map<String, Long> byAreaDistribution;

    private Map<String, Long> byCrimeTypeDistribution;
}
