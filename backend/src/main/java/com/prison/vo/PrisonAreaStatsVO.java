package com.prison.vo;

import lombok.Data;

import java.util.List;

@Data
public class PrisonAreaStatsVO {

    private Long id;

    private String areaName;

    private String areaCode;

    private String areaType;

    private Integer capacity;

    private Integer currentPopulation;

    private String status;

    private String description;

    private Integer cellCount;

    private Integer fullCellCount;

    private Integer availableCellCount;

    private Integer maintenanceCellCount;

    private Integer totalCellCapacity;

    private Integer totalCellOccupancy;

    private Double occupancyRate;

    private List<CellDetailVO> cells;

    @Data
    public static class CellDetailVO {
        private Long id;
        private String cellNumber;
        private String cellType;
        private Integer capacity;
        private Integer currentOccupancy;
        private String status;
        private Double occupancyRate;
    }
}
