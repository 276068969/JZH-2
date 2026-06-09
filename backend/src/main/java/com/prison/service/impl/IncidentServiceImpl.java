package com.prison.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.prison.entity.Incident;
import com.prison.mapper.IncidentMapper;
import com.prison.service.IncidentService;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
public class IncidentServiceImpl extends ServiceImpl<IncidentMapper, Incident> implements IncidentService {

    @Override
    public Page<Incident> pageIncidents(int page, int size, String keyword) {
        LambdaQueryWrapper<Incident> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(keyword)) {
            wrapper.like(Incident::getIncidentTitle, keyword)
                    .or()
                    .like(Incident::getIncidentType, keyword);
        }
        wrapper.orderByDesc(Incident::getOccurTime);
        return page(new Page<>(page, size), wrapper);
    }
}