package com.prison.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.prison.entity.Prisoner;
import com.prison.mapper.PrisonerMapper;
import com.prison.service.PrisonerService;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
public class PrisonerServiceImpl extends ServiceImpl<PrisonerMapper, Prisoner> implements PrisonerService {

    @Override
    public Page<Prisoner> pagePrisoners(int page, int size, String keyword) {
        LambdaQueryWrapper<Prisoner> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(keyword)) {
            wrapper.like(Prisoner::getName, keyword)
                    .or()
                    .like(Prisoner::getPrisonerNumber, keyword)
                    .or()
                    .like(Prisoner::getIdCard, keyword);
        }
        wrapper.orderByDesc(Prisoner::getCreateTime);
        return page(new Page<>(page, size), wrapper);
    }
}