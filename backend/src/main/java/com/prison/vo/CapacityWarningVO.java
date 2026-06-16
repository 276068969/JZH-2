package com.prison.vo;

import lombok.Data;

import java.util.List;

@Data
public class CapacityWarningVO {

    private WarningSummaryVO summary;

    private List<AreaWarningVO> areaWarnings;

    private List<CellWarningVO> cellWarnings;

    @Data
    public static class WarningSummaryVO {
        private Integer totalAreas;
        private Integer overCapacityAreaCount;
        private Integer nearFullAreaCount;
        private Integer maintenanceImpactAreaCount;
        private Integer totalCells;
        private Integer overCapacityCellCount;
        private Integer nearFullCellCount;
        private Integer maintenanceCellCount;
        private Integer totalMaintenanceBedsLost;
        private Integer criticalWarningCount;
        private Integer warningCount;
        private Integer infoCount;
    }

    @Data
    public static class AreaWarningVO {
        private Long id;
        private String areaName;
        private String areaCode;
        private String areaType;
        private String riskLevel;
        private List<String> riskTypes;
        private Integer capacity;
        private Integer currentPopulation;
        private Double occupancyRate;
        private Integer cellCount;
        private Integer fullCellCount;
        private Integer maintenanceCellCount;
        private Integer maintenanceBedsLost;
        private Integer availableBeds;
        private String warningMessage;
    }

    @Data
    public static class CellWarningVO {
        private Long id;
        private String cellNumber;
        private Long areaId;
        private String areaName;
        private String cellType;
        private String riskLevel;
        private List<String> riskTypes;
        private Integer capacity;
        private Integer currentOccupancy;
        private Double occupancyRate;
        private String status;
        private String warningMessage;
    }
}
