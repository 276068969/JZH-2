package com.prison.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.prison.dto.VisitorApprovalDTO;
import com.prison.entity.Visitor;

import java.util.Map;

public interface VisitorService extends IService<Visitor> {
    Page<Visitor> pageVisitors(int page, int size, String keyword, String status, String visitType);

    Page<Visitor> pagePendingVisitors(int page, int size);

    void approve(Long id, VisitorApprovalDTO dto);

    void reject(Long id, VisitorApprovalDTO dto);

    void startVisit(Long id);

    void endVisit(Long id);

    Map<String, Long> getStatusStatistics();

    Map<String, Long> getTypeStatistics();
}
