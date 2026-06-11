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
    @Transactional
    public void createCell(Cell cell) {
        if (cell.getCurrentOccupancy() == null) {
            cell.setCurrentOccupancy(0);
        }
        if (cell.getStatus() == null) {
            cell.setStatus("AVAILABLE");
        }
        if (cell.getCurrentOccupancy() > cell.getCapacity()) {
            throw new BusinessException("当前入住人数(" + cell.getCurrentOccupancy() + ")不能超过容量(" + cell.getCapacity() + ")");
        }
        if (cell.getCurrentOccupancy() >= cell.getCapacity()) {
            cell.setStatus("FULL");
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

        if (cell.getCapacity() != null) {
            if (cell.getCapacity() < 1) {
                throw new BusinessException("监舍容量不能小于1");
            }
            int currentOccupancy = existing.getCurrentOccupancy();
            if (cell.getCapacity() < currentOccupancy) {
                throw new BusinessException("容量(" + cell.getCapacity() + ")不能小于当前入住人数(" + currentOccupancy + ")");
            }
        }

        if (cell.getStatus() != null) {
            if ("MAINTENANCE".equals(cell.getStatus()) && existing.getCurrentOccupancy() > 0) {
                throw new BusinessException("监舍内仍有在押人员(" + existing.getCurrentOccupancy() + "人)，无法设置为维护中状态");
            }
        }

        cell.setId(id);
        updateById(cell);

        Cell updated = getById(id);
        refreshCellStatus(updated);
        updateById(updated);
    }

    @Override
    @Transactional
    public void deleteCell(Long id) {
        Cell cell = getById(id);
        if (cell == null) {
            throw new BusinessException("监舍不存在");
        }
        int prisonerCount = prisonerMapper.countByCellId(id);
        if (prisonerCount > 0) {
            throw new BusinessException("监舍内仍有在押人员(" + prisonerCount + "人)，无法删除");
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
        Cell cell = getById(cellId);
        if (cell == null) {
            throw new BusinessException("监舍不存在");
        }
        if ("MAINTENANCE".equals(cell.getStatus())) {
            throw new BusinessException("监舍[" + cell.getCellNumber() + "]处于维护中，无法分配人员");
        }
        if ("ISOLATION".equals(cell.getCellType()) && cell.getCurrentOccupancy() >= 1) {
            throw new BusinessException("隔离监舍[" + cell.getCellNumber() + "]只能关押1人，当前已满员");
        }
        if (cell.getCurrentOccupancy() >= cell.getCapacity()) {
            throw new BusinessException("监舍[" + cell.getCellNumber() + "]已满员(容量:" + cell.getCapacity() + ")，无法继续分配");
        }
        cell.setCurrentOccupancy(cell.getCurrentOccupancy() + 1);
        refreshCellStatus(cell);
        updateById(cell);
    }

    @Override
    @Transactional
    public void decrementOccupancy(Long cellId) {
        Cell cell = getById(cellId);
        if (cell == null) {
            return;
        }
        if (cell.getCurrentOccupancy() <= 0) {
            return;
        }
        cell.setCurrentOccupancy(cell.getCurrentOccupancy() - 1);
        refreshCellStatus(cell);
        updateById(cell);
    }

    @Override
    public void validateCanAssign(Long cellId) {
        Cell cell = getById(cellId);
        if (cell == null) {
            throw new BusinessException("监舍不存在");
        }
        if ("MAINTENANCE".equals(cell.getStatus())) {
            throw new BusinessException("监舍[" + cell.getCellNumber() + "]处于维护中，无法分配人员");
        }
        if ("ISOLATION".equals(cell.getCellType()) && cell.getCurrentOccupancy() >= 1) {
            throw new BusinessException("隔离监舍[" + cell.getCellNumber() + "]只能关押1人，当前已满员");
        }
        if (cell.getCurrentOccupancy() >= cell.getCapacity()) {
            throw new BusinessException("监舍[" + cell.getCellNumber() + "]已满员(容量:" + cell.getCapacity() + ")，无法继续分配");
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
