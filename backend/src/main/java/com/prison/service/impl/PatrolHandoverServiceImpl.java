package com.prison.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.prison.config.BusinessException;
import com.prison.dto.PatrolHandoverDTO;
import com.prison.dto.PatrolHandoverQueryDTO;
import com.prison.entity.PatrolHandover;
import com.prison.mapper.PatrolHandoverMapper;
import com.prison.service.PatrolHandoverService;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class PatrolHandoverServiceImpl extends ServiceImpl<PatrolHandoverMapper, PatrolHandover> implements PatrolHandoverService {

    @Override
    public Page<PatrolHandover> pageHandovers(PatrolHandoverQueryDTO query) {
        return baseMapper.pageHandovers(new Page<>(query.getPage(), query.getSize()), query);
    }

    @Override
    public PatrolHandover createHandover(PatrolHandoverDTO dto) {
        PatrolHandover handover = new PatrolHandover();
        BeanUtils.copyProperties(dto, handover);
        handover.setStatus("PENDING");
        handover.setPatrolCount(dto.getPatrolCount() != null ? dto.getPatrolCount() : 0);
        handover.setAbnormalCount(dto.getAbnormalCount() != null ? dto.getAbnormalCount() : 0);
        save(handover);
        return handover;
    }

    @Override
    public PatrolHandover confirmHandover(Long id, Long incomingGuardId, String incomingGuardName) {
        PatrolHandover handover = getById(id);
        if (handover == null) {
            throw new BusinessException("交接班记录不存在");
        }
        if (!"PENDING".equals(handover.getStatus())) {
            throw new BusinessException("该交接班记录已确认，无法重复确认");
        }
        handover.setIncomingGuardId(incomingGuardId);
        handover.setIncomingGuardName(incomingGuardName);
        handover.setStatus("CONFIRMED");
        handover.setHandoverTime(LocalDateTime.now());
        updateById(handover);
        return handover;
    }

    @Override
    public PatrolHandover getLatestHandoverByArea(Long areaId) {
        return baseMapper.getLatestHandoverByArea(areaId);
    }

    @Override
    public List<PatrolHandover> getRecentHandovers(Long areaId, Integer limit) {
        LambdaQueryWrapper<PatrolHandover> wrapper = new LambdaQueryWrapper<>();
        if (areaId != null) {
            wrapper.eq(PatrolHandover::getAreaId, areaId);
        }
        wrapper.orderByDesc(PatrolHandover::getHandoverTime);
        wrapper.last("LIMIT " + (limit != null ? limit : 10));
        return list(wrapper);
    }
}
