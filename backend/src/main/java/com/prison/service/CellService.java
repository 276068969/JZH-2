package com.prison.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.prison.entity.Cell;

public interface CellService extends IService<Cell> {
    Page<Cell> pageCells(int page, int size, String keyword);

    void createCell(Cell cell);

    void updateCell(Long id, Cell cell);

    void deleteCell(Long id);

    void syncOccupancy(Long cellId);

    void syncAllOccupancy();

    void incrementOccupancy(Long cellId);

    void decrementOccupancy(Long cellId);

    void validateCanAssign(Long cellId);
}