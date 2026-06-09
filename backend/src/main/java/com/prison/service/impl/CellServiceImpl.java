package com.prison.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.prison.entity.Cell;
import com.prison.mapper.CellMapper;
import com.prison.service.CellService;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
public class CellServiceImpl extends ServiceImpl<CellMapper, Cell> implements CellService {

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
}