package com.prison.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.prison.entity.Guard;
import com.prison.mapper.GuardMapper;
import com.prison.service.GuardService;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
public class GuardServiceImpl extends ServiceImpl<GuardMapper, Guard> implements GuardService {

    @Override
    public Page<Guard> pageGuards(int page, int size, String keyword) {
        LambdaQueryWrapper<Guard> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(keyword)) {
            wrapper.like(Guard::getName, keyword)
                    .or()
                    .like(Guard::getGuardNumber, keyword)
                    .or()
                    .like(Guard::getPhone, keyword);
        }
        wrapper.orderByDesc(Guard::getCreateTime);
        return page(new Page<>(page, size), wrapper);
    }
}