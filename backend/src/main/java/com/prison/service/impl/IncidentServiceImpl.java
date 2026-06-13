package com.prison.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.prison.config.BusinessException;
import com.prison.dto.IncidentDTO;
import com.prison.entity.Incident;
import com.prison.enums.IncidentStatus;
import com.prison.enums.SeverityLevel;
import com.prison.mapper.IncidentMapper;
import com.prison.service.IncidentService;
import org.springframework.beans.BeanUtils;
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

    @Override
    public void validateStatusTransition(Incident incident, String targetStatusStr) {
        if (incident == null) {
            throw new BusinessException("事件不存在");
        }

        IncidentStatus currentStatus = IncidentStatus.fromString(incident.getStatus());
        if (currentStatus == null) {
            throw new BusinessException("当前事件状态无效: " + incident.getStatus());
        }

        IncidentStatus targetStatus = IncidentStatus.fromString(targetStatusStr);
        if (targetStatus == null) {
            throw new BusinessException("目标状态无效: " + targetStatusStr);
        }

        if (currentStatus == targetStatus) {
            return;
        }

        if (!currentStatus.canTransitionTo(targetStatus)) {
            throw new BusinessException(
                "无法从 " + currentStatus.getDescription() + " 流转到 " + targetStatus.getDescription()
            );
        }

        if (targetStatus == IncidentStatus.PROCESSING || targetStatus == IncidentStatus.RESOLVED
                || targetStatus == IncidentStatus.CLOSED) {
            SeverityLevel severity = SeverityLevel.fromString(incident.getSeverity());
            if (severity != null && severity.isHighOrAbove()) {
                if (incident.getRelatedPrisonerId() == null) {
                    throw new BusinessException("高等级事件必须关联服刑人员后才能进入" + targetStatus.getDescription() + "状态");
                }
            }
        }

        if (targetStatus == IncidentStatus.RESOLVED || targetStatus == IncidentStatus.CLOSED) {
            if (!StringUtils.hasText(incident.getHandlerResult())) {
                throw new BusinessException("必须填写处置结论后才能" + targetStatus.getDescription());
            }
        }
    }

    @Override
    public Incident startProcessing(Long id) {
        Incident incident = getById(id);
        if (incident == null) {
            throw new BusinessException("事件不存在");
        }

        validateStatusTransition(incident, IncidentStatus.PROCESSING.name());

        incident.setStatus(IncidentStatus.PROCESSING.name());
        updateById(incident);
        return incident;
    }

    @Override
    public Incident resolve(Long id, String handlerResult) {
        Incident incident = getById(id);
        if (incident == null) {
            throw new BusinessException("事件不存在");
        }

        if (!StringUtils.hasText(handlerResult)) {
            throw new BusinessException("处置结论不能为空");
        }

        incident.setHandlerResult(handlerResult);
        validateStatusTransition(incident, IncidentStatus.RESOLVED.name());

        incident.setStatus(IncidentStatus.RESOLVED.name());
        updateById(incident);
        return incident;
    }

    @Override
    public Incident close(Long id) {
        Incident incident = getById(id);
        if (incident == null) {
            throw new BusinessException("事件不存在");
        }

        validateStatusTransition(incident, IncidentStatus.CLOSED.name());

        incident.setStatus(IncidentStatus.CLOSED.name());
        updateById(incident);
        return incident;
    }

    @Override
    public void updateIncident(Long id, IncidentDTO dto) {
        Incident existing = getById(id);
        if (existing == null) {
            throw new BusinessException("事件不存在");
        }

        if (StringUtils.hasText(dto.getStatus()) && !dto.getStatus().equals(existing.getStatus())) {
            Incident validateContext = new Incident();
            BeanUtils.copyProperties(existing, validateContext);
            if (StringUtils.hasText(dto.getHandlerResult())) {
                validateContext.setHandlerResult(dto.getHandlerResult());
            }
            if (dto.getRelatedPrisonerId() != null) {
                validateContext.setRelatedPrisonerId(dto.getRelatedPrisonerId());
            }
            if (StringUtils.hasText(dto.getSeverity())) {
                validateContext.setSeverity(dto.getSeverity());
            }
            validateStatusTransition(validateContext, dto.getStatus());
        }

        Incident incident = new Incident();
        BeanUtils.copyProperties(dto, incident);
        incident.setId(id);
        updateById(incident);
    }

    @Override
    public void createIncident(IncidentDTO dto) {
        Incident incident = new Incident();
        BeanUtils.copyProperties(dto, incident);
        if (!StringUtils.hasText(incident.getStatus())) {
            incident.setStatus(IncidentStatus.PENDING.name());
        } else {
            IncidentStatus status = IncidentStatus.fromString(dto.getStatus());
            if (status == null) {
                throw new BusinessException("无效的事件状态: " + dto.getStatus());
            }
            if (status != IncidentStatus.PENDING) {
                throw new BusinessException("新建事件只能为待处理状态");
            }
        }
        save(incident);
    }
}