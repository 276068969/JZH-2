package com.prison.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.prison.config.BusinessException;
import com.prison.entity.Cell;
import com.prison.entity.PrisonArea;
import com.prison.mapper.PrisonAreaMapper;
import com.prison.mapper.PrisonerMapper;
import com.prison.service.CellService;
import com.prison.service.PrisonAreaService;
import com.prison.vo.CapacityWarningVO;
import com.prison.vo.PrisonAreaStatsVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PrisonAreaServiceImpl extends ServiceImpl<PrisonAreaMapper, PrisonArea> implements PrisonAreaService {

    private final PrisonerMapper prisonerMapper;
    private final CellService cellService;

    @Override
    public Page<PrisonArea> pagePrisonAreas(int page, int size, String keyword) {
        LambdaQueryWrapper<PrisonArea> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(keyword)) {
            wrapper.like(PrisonArea::getAreaName, keyword)
                    .or()
                    .like(PrisonArea::getAreaCode, keyword);
        }
        wrapper.orderByDesc(PrisonArea::getCreateTime);
        return page(new Page<>(page, size), wrapper);
    }

    @Override
    public List<PrisonAreaStatsVO> listAreaStats() {
        List<PrisonArea> areas = list();
        List<PrisonAreaStatsVO> result = new ArrayList<>();
        for (PrisonArea area : areas) {
            result.add(buildAreaStats(area, false));
        }
        return result;
    }

    @Override
    public PrisonAreaStatsVO getAreaStatsById(Long id) {
        PrisonArea area = getById(id);
        if (area == null) {
            throw new BusinessException("监区不存在");
        }
        return buildAreaStats(area, true);
    }

    private PrisonAreaStatsVO buildAreaStats(PrisonArea area, boolean includeCells) {
        PrisonAreaStatsVO vo = new PrisonAreaStatsVO();
        vo.setId(area.getId());
        vo.setAreaName(area.getAreaName());
        vo.setAreaCode(area.getAreaCode());
        vo.setAreaType(area.getAreaType());
        vo.setCapacity(area.getCapacity());
        vo.setCurrentPopulation(area.getCurrentPopulation());
        vo.setStatus(area.getStatus());
        vo.setDescription(area.getDescription());

        List<Cell> cells = cellService.listByAreaId(area.getId());
        int cellCount = cells.size();
        int fullCount = 0;
        int availableCount = 0;
        int maintenanceCount = 0;
        int totalCapacity = 0;
        int totalOccupancy = 0;

        for (Cell cell : cells) {
            int cap = cell.getCapacity() != null ? cell.getCapacity() : 0;
            int occ = cell.getCurrentOccupancy() != null ? cell.getCurrentOccupancy() : 0;
            totalCapacity += cap;
            totalOccupancy += occ;

            String status = cell.getStatus();
            if ("FULL".equals(status)) {
                fullCount++;
            } else if ("MAINTENANCE".equals(status)) {
                maintenanceCount++;
            } else {
                availableCount++;
            }
        }

        vo.setCellCount(cellCount);
        vo.setFullCellCount(fullCount);
        vo.setAvailableCellCount(availableCount);
        vo.setMaintenanceCellCount(maintenanceCount);
        vo.setTotalCellCapacity(totalCapacity);
        vo.setTotalCellOccupancy(totalOccupancy);
        vo.setOccupancyRate(totalCapacity > 0
                ? Math.round((double) totalOccupancy / totalCapacity * 10000.0) / 100.0
                : 0.0);

        if (includeCells) {
            List<PrisonAreaStatsVO.CellDetailVO> cellVOs = cells.stream().map(cell -> {
                PrisonAreaStatsVO.CellDetailVO cellVO = new PrisonAreaStatsVO.CellDetailVO();
                cellVO.setId(cell.getId());
                cellVO.setCellNumber(cell.getCellNumber());
                cellVO.setCellType(cell.getCellType());
                cellVO.setCapacity(cell.getCapacity());
                cellVO.setCurrentOccupancy(cell.getCurrentOccupancy());
                cellVO.setStatus(cell.getStatus());
                int cap = cell.getCapacity() != null ? cell.getCapacity() : 0;
                int occ = cell.getCurrentOccupancy() != null ? cell.getCurrentOccupancy() : 0;
                cellVO.setOccupancyRate(cap > 0
                        ? Math.round((double) occ / cap * 10000.0) / 100.0
                        : 0.0);
                return cellVO;
            }).collect(Collectors.toList());
            vo.setCells(cellVOs);
        }

        return vo;
    }

    @Override
    @Transactional
    public void createPrisonArea(PrisonArea prisonArea) {
        prisonArea.setCurrentPopulation(0);
        if (!StringUtils.hasText(prisonArea.getStatus())) {
            prisonArea.setStatus("ACTIVE");
        }
        if (prisonArea.getCapacity() == null || prisonArea.getCapacity() < 0) {
            prisonArea.setCapacity(0);
        }
        save(prisonArea);
    }

    @Override
    @Transactional
    public void updatePrisonArea(Long id, PrisonArea prisonArea) {
        PrisonArea existing = getById(id);
        if (existing == null) {
            throw new BusinessException("监区不存在");
        }

        int realCount = prisonerMapper.countByAreaId(id);

        if (prisonArea.getCapacity() != null) {
            if (prisonArea.getCapacity() < 0) {
                throw new BusinessException("监区容量不能小于0");
            }
            if (prisonArea.getCapacity() < realCount) {
                throw new BusinessException("容量(" + prisonArea.getCapacity() + ")不能小于实际在押人数(" + realCount + "人)");
            }
            existing.setCapacity(prisonArea.getCapacity());
        }

        if (StringUtils.hasText(prisonArea.getAreaName())) {
            existing.setAreaName(prisonArea.getAreaName());
        }
        if (StringUtils.hasText(prisonArea.getAreaCode())) {
            existing.setAreaCode(prisonArea.getAreaCode());
        }
        if (StringUtils.hasText(prisonArea.getAreaType())) {
            existing.setAreaType(prisonArea.getAreaType());
        }
        if (StringUtils.hasText(prisonArea.getAddress())) {
            existing.setAddress(prisonArea.getAddress());
        }
        if (StringUtils.hasText(prisonArea.getDescription())) {
            existing.setDescription(prisonArea.getDescription());
        }
        if (StringUtils.hasText(prisonArea.getStatus())) {
            if ("INACTIVE".equals(prisonArea.getStatus()) && realCount > 0) {
                throw new BusinessException("监区内仍有实际在押人员(" + realCount + "人)，无法停用");
            }
            existing.setStatus(prisonArea.getStatus());
        }

        updateById(existing);
        syncPopulation(id);
    }

    @Override
    @Transactional
    public void deletePrisonArea(Long id) {
        PrisonArea prisonArea = getById(id);
        if (prisonArea == null) {
            throw new BusinessException("监区不存在");
        }
        int realCount = prisonerMapper.countByAreaId(id);
        if (realCount > 0) {
            throw new BusinessException("监区内仍有实际在押人员(" + realCount + "人)，无法删除");
        }
        removeById(id);
    }

    @Override
    @Transactional
    public void syncPopulation(Long areaId) {
        PrisonArea prisonArea = getById(areaId);
        if (prisonArea == null) {
            throw new BusinessException("监区不存在");
        }
        int actualCount = prisonerMapper.countByAreaId(areaId);
        prisonArea.setCurrentPopulation(actualCount);
        updateById(prisonArea);
    }

    @Override
    @Transactional
    public void syncAllPopulation() {
        List<PrisonArea> areas = list();
        for (PrisonArea area : areas) {
            int actualCount = prisonerMapper.countByAreaId(area.getId());
            area.setCurrentPopulation(actualCount);
        }
        updateBatchById(areas);
    }

    @Override
    @Transactional
    public void incrementPopulation(Long areaId) {
        syncPopulation(areaId);
    }

    @Override
    @Transactional
    public void decrementPopulation(Long areaId) {
        PrisonArea prisonArea = getById(areaId);
        if (prisonArea == null) {
            return;
        }
        syncPopulation(areaId);
    }

    @Override
    public CapacityWarningVO getCapacityWarnings() {
        List<PrisonArea> areas = list();
        return buildCapacityWarningVO(areas);
    }

    @Override
    public CapacityWarningVO getCapacityWarningsByAreaId(Long areaId) {
        PrisonArea area = getById(areaId);
        if (area == null) {
            throw new BusinessException("监区不存在");
        }
        return buildCapacityWarningVO(Arrays.asList(area));
    }

    private CapacityWarningVO buildCapacityWarningVO(List<PrisonArea> areas) {
        CapacityWarningVO vo = new CapacityWarningVO();

        CapacityWarningVO.WarningSummaryVO summary = new CapacityWarningVO.WarningSummaryVO();
        List<CapacityWarningVO.AreaWarningVO> areaWarnings = new ArrayList<>();
        List<CapacityWarningVO.CellWarningVO> cellWarnings = new ArrayList<>();

        int totalAreas = areas.size();
        int overCapacityAreaCount = 0;
        int nearFullAreaCount = 0;
        int maintenanceImpactAreaCount = 0;
        int totalCells = 0;
        int overCapacityCellCount = 0;
        int nearFullCellCount = 0;
        int maintenanceCellCount = 0;
        int totalMaintenanceBedsLost = 0;
        int criticalWarningCount = 0;
        int warningCount = 0;
        int infoCount = 0;

        for (PrisonArea area : areas) {
            List<Cell> cells = cellService.listByAreaId(area.getId());

            int cellCount = cells.size();
            int fullCellCount = 0;
            int maintenanceCellCountInArea = 0;
            int maintenanceBedsLost = 0;
            int totalCellCapacity = 0;
            int totalCellOccupancy = 0;

            for (Cell cell : cells) {
                int cap = cell.getCapacity() != null ? cell.getCapacity() : 0;
                int occ = cell.getCurrentOccupancy() != null ? cell.getCurrentOccupancy() : 0;
                totalCellCapacity += cap;
                totalCellOccupancy += occ;

                String status = cell.getStatus();
                if ("FULL".equals(status)) {
                    fullCellCount++;
                } else if ("MAINTENANCE".equals(status)) {
                    maintenanceCellCountInArea++;
                    maintenanceBedsLost += cap;
                }

                CapacityWarningVO.CellWarningVO cellWarning = buildCellWarning(cell, area);
                if (cellWarning != null) {
                    cellWarnings.add(cellWarning);
                    String riskLevel = cellWarning.getRiskLevel();
                    if ("CRITICAL".equals(riskLevel)) {
                        criticalWarningCount++;
                    } else if ("WARNING".equals(riskLevel)) {
                        warningCount++;
                    } else {
                        infoCount++;
                    }

                    if (cellWarning.getRiskTypes().contains("OVER_CAPACITY")) {
                        overCapacityCellCount++;
                    }
                    if (cellWarning.getRiskTypes().contains("NEAR_FULL")) {
                        nearFullCellCount++;
                    }
                    if (cellWarning.getRiskTypes().contains("MAINTENANCE")) {
                        maintenanceCellCount++;
                    }
                }
            }

            totalCells += cellCount;
            totalMaintenanceBedsLost += maintenanceBedsLost;

            CapacityWarningVO.AreaWarningVO areaWarning = buildAreaWarning(
                    area, cellCount, fullCellCount, maintenanceCellCountInArea,
                    maintenanceBedsLost, totalCellCapacity, totalCellOccupancy, cells
            );

            if (areaWarning != null) {
                areaWarnings.add(areaWarning);
                String riskLevel = areaWarning.getRiskLevel();
                if ("CRITICAL".equals(riskLevel)) {
                    criticalWarningCount++;
                } else if ("WARNING".equals(riskLevel)) {
                    warningCount++;
                } else {
                    infoCount++;
                }

                if (areaWarning.getRiskTypes().contains("OVER_CAPACITY")) {
                    overCapacityAreaCount++;
                }
                if (areaWarning.getRiskTypes().contains("NEAR_FULL")) {
                    nearFullAreaCount++;
                }
                if (areaWarning.getRiskTypes().contains("MAINTENANCE_IMPACT")) {
                    maintenanceImpactAreaCount++;
                }
            }
        }

        summary.setTotalAreas(totalAreas);
        summary.setOverCapacityAreaCount(overCapacityAreaCount);
        summary.setNearFullAreaCount(nearFullAreaCount);
        summary.setMaintenanceImpactAreaCount(maintenanceImpactAreaCount);
        summary.setTotalCells(totalCells);
        summary.setOverCapacityCellCount(overCapacityCellCount);
        summary.setNearFullCellCount(nearFullCellCount);
        summary.setMaintenanceCellCount(maintenanceCellCount);
        summary.setTotalMaintenanceBedsLost(totalMaintenanceBedsLost);
        summary.setCriticalWarningCount(criticalWarningCount);
        summary.setWarningCount(warningCount);
        summary.setInfoCount(infoCount);

        vo.setSummary(summary);
        vo.setAreaWarnings(areaWarnings);
        vo.setCellWarnings(cellWarnings);

        return vo;
    }

    private CapacityWarningVO.AreaWarningVO buildAreaWarning(
            PrisonArea area, int cellCount, int fullCellCount, int maintenanceCellCount,
            int maintenanceBedsLost, int totalCellCapacity, int totalCellOccupancy, List<Cell> cells
    ) {
        List<String> riskTypes = new ArrayList<>();
        String riskLevel = "INFO";
        StringBuilder messageBuilder = new StringBuilder();

        int capacity = area.getCapacity() != null ? area.getCapacity() : 0;
        int currentPopulation = area.getCurrentPopulation() != null ? area.getCurrentPopulation() : 0;

        double occupancyRate = capacity > 0
                ? Math.round((double) currentPopulation / capacity * 10000.0) / 100.0
                : 0.0;

        int availableBeds = capacity - currentPopulation;

        if (currentPopulation > capacity) {
            riskTypes.add("OVER_CAPACITY");
            riskLevel = "CRITICAL";
            messageBuilder.append("监区超容，超出").append(currentPopulation - capacity).append("人；");
        } else if (occupancyRate >= 90) {
            riskTypes.add("NEAR_FULL");
            if (!"CRITICAL".equals(riskLevel)) {
                riskLevel = "WARNING";
            }
            messageBuilder.append("入住率达").append(occupancyRate).append("%，接近满员；");
        } else if (occupancyRate >= 80) {
            riskTypes.add("NEAR_FULL");
            if ("INFO".equals(riskLevel)) {
                riskLevel = "INFO";
            }
            messageBuilder.append("入住率达").append(occupancyRate).append("%，需关注；");
        }

        double maintenanceImpactRate = totalCellCapacity > 0
                ? (double) maintenanceBedsLost / totalCellCapacity * 100
                : 0;

        if (maintenanceCellCount > 0) {
            if (maintenanceImpactRate >= 20) {
                riskTypes.add("MAINTENANCE_IMPACT");
                if ("INFO".equals(riskLevel) || "WARNING".equals(riskLevel)) {
                    riskLevel = "WARNING";
                }
                messageBuilder.append(maintenanceCellCount).append("间监舍维护中，减少")
                        .append(maintenanceBedsLost).append("张床位（占")
                        .append(Math.round(maintenanceImpactRate)).append("%）；");
            } else if (maintenanceImpactRate >= 10) {
                riskTypes.add("MAINTENANCE_IMPACT");
                if ("INFO".equals(riskLevel)) {
                    riskLevel = "INFO";
                }
                messageBuilder.append(maintenanceCellCount).append("间监舍维护中，减少")
                        .append(maintenanceBedsLost).append("张床位；");
            }
        }

        if (riskTypes.isEmpty()) {
            return null;
        }

        CapacityWarningVO.AreaWarningVO vo = new CapacityWarningVO.AreaWarningVO();
        vo.setId(area.getId());
        vo.setAreaName(area.getAreaName());
        vo.setAreaCode(area.getAreaCode());
        vo.setAreaType(area.getAreaType());
        vo.setRiskLevel(riskLevel);
        vo.setRiskTypes(riskTypes);
        vo.setCapacity(capacity);
        vo.setCurrentPopulation(currentPopulation);
        vo.setOccupancyRate(occupancyRate);
        vo.setCellCount(cellCount);
        vo.setFullCellCount(fullCellCount);
        vo.setMaintenanceCellCount(maintenanceCellCount);
        vo.setMaintenanceBedsLost(maintenanceBedsLost);
        vo.setAvailableBeds(availableBeds > 0 ? availableBeds : 0);
        vo.setWarningMessage(messageBuilder.toString());

        return vo;
    }

    private CapacityWarningVO.CellWarningVO buildCellWarning(Cell cell, PrisonArea area) {
        List<String> riskTypes = new ArrayList<>();
        String riskLevel = "INFO";
        StringBuilder messageBuilder = new StringBuilder();

        int capacity = cell.getCapacity() != null ? cell.getCapacity() : 0;
        int currentOccupancy = cell.getCurrentOccupancy() != null ? cell.getCurrentOccupancy() : 0;
        String status = cell.getStatus();

        double occupancyRate = capacity > 0
                ? Math.round((double) currentOccupancy / capacity * 10000.0) / 100.0
                : 0.0;

        if ("MAINTENANCE".equals(status)) {
            riskTypes.add("MAINTENANCE");
            riskLevel = "WARNING";
            messageBuilder.append("监舍维护中，不可用；");
        } else {
            if (currentOccupancy > capacity) {
                riskTypes.add("OVER_CAPACITY");
                riskLevel = "CRITICAL";
                messageBuilder.append("超员").append(currentOccupancy - capacity).append("人；");
            } else if (occupancyRate >= 90) {
                riskTypes.add("NEAR_FULL");
                riskLevel = "WARNING";
                messageBuilder.append("入住率达").append(occupancyRate).append("%，接近满员；");
            } else if (occupancyRate >= 80) {
                riskTypes.add("NEAR_FULL");
                riskLevel = "INFO";
                messageBuilder.append("入住率达").append(occupancyRate).append("%，需关注；");
            }
        }

        if (riskTypes.isEmpty()) {
            return null;
        }

        CapacityWarningVO.CellWarningVO vo = new CapacityWarningVO.CellWarningVO();
        vo.setId(cell.getId());
        vo.setCellNumber(cell.getCellNumber());
        vo.setAreaId(area.getId());
        vo.setAreaName(area.getAreaName());
        vo.setCellType(cell.getCellType());
        vo.setRiskLevel(riskLevel);
        vo.setRiskTypes(riskTypes);
        vo.setCapacity(capacity);
        vo.setCurrentOccupancy(currentOccupancy);
        vo.setOccupancyRate(occupancyRate);
        vo.setStatus(status);
        vo.setWarningMessage(messageBuilder.toString());

        return vo;
    }
}
