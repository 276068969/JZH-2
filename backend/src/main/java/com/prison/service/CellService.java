package com.prison.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.prison.entity.Cell;

public interface CellService extends IService<Cell> {
    Page<Cell> pageCells(int page, int size, String keyword);
}