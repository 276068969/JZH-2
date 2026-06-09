package com.prison.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.prison.entity.Visitor;
import com.prison.mapper.VisitorMapper;
import com.prison.service.VisitorService;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
public class VisitorServiceImpl extends ServiceImpl<VisitorMapper, Visitor> implements VisitorService {

    @Override
    public Page<Visitor> pageVisitors(int page, int size, String keyword) {
        LambdaQueryWrapper<Visitor> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(keyword)) {
            wrapper.like(Visitor::getVisitorName, keyword)
                    .or()
                    .like(Visitor::getIdCard, keyword)
                    .or()
                    .like(Visitor::getRelation, keyword);
        }
        wrapper.orderByDesc(Visitor::getVisitDate);
        return page(new Page<>(page, size), wrapper);
    }
}