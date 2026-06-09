package com.prison.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.prison.dto.VisitorApprovalDTO;
import com.prison.entity.Visitor;
import com.prison.mapper.VisitorMapper;
import com.prison.service.VisitorService;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Service
public class VisitorServiceImpl extends ServiceImpl<VisitorMapper, Visitor> implements VisitorService {

    @Override
    public Page<Visitor> pageVisitors(int page, int size, String keyword, String status, String visitType) {
        LambdaQueryWrapper<Visitor> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(keyword)) {
            wrapper.and(w -> w.like(Visitor::getVisitorName, keyword)
                    .or()
                    .like(Visitor::getIdCard, keyword)
                    .or()
                    .like(Visitor::getRelation, keyword));
        }
        if (StringUtils.hasText(status)) {
            wrapper.eq(Visitor::getStatus, status);
        }
        if (StringUtils.hasText(visitType)) {
            wrapper.eq(Visitor::getVisitType, visitType);
        }
        wrapper.orderByDesc(Visitor::getVisitDate);
        return page(new Page<>(page, size), wrapper);
    }

    @Override
    public Page<Visitor> pagePendingVisitors(int page, int size) {
        LambdaQueryWrapper<Visitor> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Visitor::getStatus, "PENDING")
                .orderByAsc(Visitor::getVisitDate);
        return page(new Page<>(page, size), wrapper);
    }

    @Override
    public void approve(Long id, VisitorApprovalDTO dto) {
        Visitor visitor = getById(id);
        if (visitor == null) {
            throw new RuntimeException("访客记录不存在");
        }
        if (!"PENDING".equals(visitor.getStatus())) {
            throw new RuntimeException("当前状态不允许审批通过");
        }
        visitor.setStatus("APPROVED");
        visitor.setApproveRemark(dto.getApproveRemark());
        visitor.setApproveGuardId(dto.getApproveGuardId());
        visitor.setApproveTime(LocalDateTime.now());
        updateById(visitor);
    }

    @Override
    public void reject(Long id, VisitorApprovalDTO dto) {
        Visitor visitor = getById(id);
        if (visitor == null) {
            throw new RuntimeException("访客记录不存在");
        }
        if (!"PENDING".equals(visitor.getStatus())) {
            throw new RuntimeException("当前状态不允许驳回");
        }
        visitor.setStatus("REJECTED");
        visitor.setApproveRemark(dto.getApproveRemark());
        visitor.setApproveGuardId(dto.getApproveGuardId());
        visitor.setApproveTime(LocalDateTime.now());
        updateById(visitor);
    }

    @Override
    public void startVisit(Long id) {
        Visitor visitor = getById(id);
        if (visitor == null) {
            throw new RuntimeException("访客记录不存在");
        }
        if (!"APPROVED".equals(visitor.getStatus())) {
            throw new RuntimeException("当前状态不允许开始会见");
        }
        visitor.setStatus("IN_PROGRESS");
        visitor.setActualStartTime(LocalDateTime.now());
        updateById(visitor);
    }

    @Override
    public void endVisit(Long id) {
        Visitor visitor = getById(id);
        if (visitor == null) {
            throw new RuntimeException("访客记录不存在");
        }
        if (!"IN_PROGRESS".equals(visitor.getStatus())) {
            throw new RuntimeException("当前状态不允许结束会见");
        }
        visitor.setStatus("COMPLETED");
        visitor.setActualEndTime(LocalDateTime.now());
        updateById(visitor);
    }

    @Override
    public Map<String, Long> getStatusStatistics() {
        Map<String, Long> result = new HashMap<>();
        result.put("pending", lambdaQuery().eq(Visitor::getStatus, "PENDING").count());
        result.put("approved", lambdaQuery().eq(Visitor::getStatus, "APPROVED").count());
        result.put("inProgress", lambdaQuery().eq(Visitor::getStatus, "IN_PROGRESS").count());
        result.put("rejected", lambdaQuery().eq(Visitor::getStatus, "REJECTED").count());
        result.put("completed", lambdaQuery().eq(Visitor::getStatus, "COMPLETED").count());
        result.put("cancelled", lambdaQuery().eq(Visitor::getStatus, "CANCELLED").count());
        return result;
    }

    @Override
    public Map<String, Long> getTypeStatistics() {
        Map<String, Long> result = new HashMap<>();
        result.put("family", lambdaQuery().eq(Visitor::getVisitType, "FAMILY").count());
        result.put("lawyer", lambdaQuery().eq(Visitor::getVisitType, "LAWYER").count());
        result.put("other", lambdaQuery().ne(Visitor::getVisitType, "FAMILY")
                .ne(Visitor::getVisitType, "LAWYER")
                .or(w -> w.isNull(Visitor::getVisitType))
                .count());
        return result;
    }
}
