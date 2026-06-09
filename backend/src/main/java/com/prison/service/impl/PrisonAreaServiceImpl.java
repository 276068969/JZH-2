package com.prison.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.prison.entity.PrisonArea;
import com.prison.mapper.PrisonAreaMapper;
import com.prison.service.PrisonAreaService;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
public class PrisonAreaServiceImpl extends ServiceImpl<PrisonAreaMapper, PrisonArea> implements PrisonAreaService {

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
}