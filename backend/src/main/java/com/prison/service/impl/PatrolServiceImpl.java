package com.prison.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.prison.entity.Patrol;
import com.prison.mapper.PatrolMapper;
import com.prison.service.PatrolService;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
public class PatrolServiceImpl extends ServiceImpl<PatrolMapper, Patrol> implements PatrolService {

    @Override
    public Page<Patrol> pagePatrols(int page, int size, String keyword) {
        LambdaQueryWrapper<Patrol> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(keyword)) {
            wrapper.like(Patrol::getPatrolType, keyword)
                    .or()
                    .like(Patrol::getResult, keyword);
        }
        wrapper.orderByDesc(Patrol::getPatrolTime);
        return page(new Page<>(page, size), wrapper);
    }
}