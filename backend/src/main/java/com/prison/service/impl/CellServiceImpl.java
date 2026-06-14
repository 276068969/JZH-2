package com.prison.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.prison.config.BusinessException;
import com.prison.entity.Cell;
import com.prison.mapper.CellMapper;
import com.prison.mapper.PrisonerMapper;
import com.prison.service.CellService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CellServiceImpl extends ServiceImpl<CellMapper, Cell> implements CellService {

    private final PrisonerMapper prisonerMapper;

    @Override
    public Page<Cell> pageCells(int page, int size, String keyword) {
        LambdaQueryWrapper<Cell> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(keyword)) {
            wrapper.like(Cell::getCellNumber, keyword)
                    .or()
                    .like(Cell::getCellType, keyword);
        }
        wrapper.orderByDesc(Cell::getCreateTime);
        return page(new Page<>(page, size), wrapper);
    }

    @Override
    public Page<Cell> pageCellsByAreaId(int page, int size, Long areaId, String keyword) {
        LambdaQueryWrapper<Cell> wrapper = new LambdaQueryWrapper<>();
        if (areaId != null) {
            wrapper.eq(Cell::getAreaId, areaId);
        }
        if (StringUtils.hasText(keyword)) {
            wrapper.and(w -> w.like(Cell::getCellNumber, keyword)
                    .or()
                    .like(Cell::getCellType, keyword));
        }
        wrapper.orderByAsc(Cell::getCellNumber);
        return page(new Page<>(page, size), wrapper);
    }

    @Override
    public List<Cell> listByAreaId(Long areaId) {
        LambdaQueryWrapper<Cell> wrapper = new LambdaQueryWrapper<>();
        if (areaId != null) {
            wrapper.eq(Cell::getAreaId, areaId);
        }
        wrapper.orderByAsc(Cell::getCellNumber);
        return list(wrapper);
    }

    @Override
    @Transactional
    public void createCell(Cell cell) {
        cell.setCurrentOccupancy(0);
        if (!StringUtils.hasText(cell.getStatus())) {
            cell.setStatus("AVAILABLE");
        }
        if (cell.getCapacity() == null || cell.getCapacity() < 1) {
            throw new BusinessException("监舍容量不能小于1");
        }
        if ("ISOLATION".equals(cell.getCellType()) && cell.getCapacity() != 1) {
            cell.setCapacity(1);
        }
        save(cell);
    }

    @Override
    @Transactional
    public void updateCell(Long id, Cell cell) {
        Cell existing = getById(id);
        if (existing == null) {
            throw new BusinessException("监舍不存在");
        }

        int realCount = prisonerMapper.countByCellId(id);

        if (cell.getCapacity() != null) {
            if (cell.getCapacity() < 1) {
                throw new BusinessException("监舍容量不能小于1");
            }
            if (cell.getCapacity() < realCount) {
                throw new BusinessException("容量(" + cell.getCapacity() + ")不能小于实际在押人数(" + realCount + "人)");
            }
            existing.setCapacity(cell.getCapacity());
        }

        if (StringUtils.hasText(cell.getStatus())) {
            if ("MAINTENANCE".equals(cell.getStatus()) && realCount > 0) {
                throw new BusinessException("监舍内仍有实际在押人员(" + realCount + "人)，无法设置为维护中状态");
            }
            existing.setStatus(cell.getStatus());
        }

        if (StringUtils.hasText(cell.getCellNumber())) {
            existing.setCellNumber(cell.getCellNumber());
        }
        if (cell.getAreaId() != null) {
            existing.setAreaId(cell.getAreaId());
        }
        if (StringUtils.hasText(cell.getCellType())) {
            existing.setCellType(cell.getCellType());
            if ("ISOLATION".equals(cell.getCellType()) && existing.getCapacity() > 1) {
                if (realCount > 1) {
                    throw new BusinessException("该监舍实际在押(" + realCount + "人)，无法改为隔离监舍(隔离监舍仅容纳1人)");
                }
                existing.setCapacity(1);
            }
        }

        updateById(existing);

        syncOccupancy(id);
    }

    @Override
    @Transactional
    public void deleteCell(Long id) {
        Cell cell = getById(id);
        if (cell == null) {
            throw new BusinessException("监舍不存在");
        }
        int realCount = prisonerMapper.countByCellId(id);
        if (realCount > 0) {
            throw new BusinessException("监舍内仍有实际在押人员(" + realCount + "人)，无法删除");
        }
        removeById(id);
    }

    @Override
    @Transactional
    public void syncOccupancy(Long cellId) {
        Cell cell = getById(cellId);
        if (cell == null) {
            throw new BusinessException("监舍不存在");
        }
        int actualCount = prisonerMapper.countByCellId(cellId);
        cell.setCurrentOccupancy(actualCount);
        refreshCellStatus(cell);
        updateById(cell);
    }

    @Override
    @Transactional
    public void syncAllOccupancy() {
        List<Cell> cells = list();
        for (Cell cell : cells) {
            int actualCount = prisonerMapper.countByCellId(cell.getId());
            cell.setCurrentOccupancy(actualCount);
            refreshCellStatus(cell);
        }
        updateBatchById(cells);
    }

    @Override
    @Transactional
    public void incrementOccupancy(Long cellId) {
        syncOccupancy(cellId);
    }

    @Override
    @Transactional
    public void decrementOccupancy(Long cellId) {
        Cell cell = getById(cellId);
        if (cell == null) {
            return;
        }
        syncOccupancy(cellId);
    }

    @Override
    public void validateCanAssign(Long cellId) {
        Cell cell = getById(cellId);
        if (cell == null) {
            throw new BusinessException("监舍不存在");
        }
        int realCount = prisonerMapper.countByCellId(cellId);

        if ("MAINTENANCE".equals(cell.getStatus())) {
            throw new BusinessException("监舍[" + cell.getCellNumber() + "]处于维护中，无法分配人员");
        }
        if ("ISOLATION".equals(cell.getCellType()) && realCount >= 1) {
            throw new BusinessException("隔离监舍[" + cell.getCellNumber() + "]只能关押1人，当前实际在押" + realCount + "人，已满员");
        }
        if (realCount >= cell.getCapacity()) {
            throw new BusinessException("监舍[" + cell.getCellNumber() + "]已满员(容量:" + cell.getCapacity() + "，实际在押:" + realCount + "人)，无法继续分配");
        }
    }

    private void refreshCellStatus(Cell cell) {
        if ("MAINTENANCE".equals(cell.getStatus())) {
            return;
        }
        if (cell.getCurrentOccupancy() >= cell.getCapacity()) {
            cell.setStatus("FULL");
        } else {
            cell.setStatus("AVAILABLE");
        }
    }
}
