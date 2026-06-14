package com.prison.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.prison.dto.VisitorApprovalDTO;
import com.prison.dto.VisitorCalendarQueryDTO;
import com.prison.dto.VisitorDTO;
import com.prison.entity.Visitor;
import com.prison.vo.LawyerMeetingDetailVO;
import com.prison.vo.VisitorCalendarVO;

import java.util.List;
import java.util.Map;

public interface VisitorService extends IService<Visitor> {
    Page<Visitor> pageVisitors(int page, int size, String keyword, String status, String visitType, String relation);

    Page<Visitor> pagePendingVisitors(int page, int size, String visitType);

    Page<Visitor> pageLawyerVisitors(int page, int size, String keyword, String status);

    Page<Visitor> pageFamilyVisitors(int page, int size, String keyword, String status);

    void createVisitor(VisitorDTO dto);

    void updateVisitor(Long id, VisitorDTO dto);

    void approve(Long id, VisitorApprovalDTO dto);

    void reject(Long id, VisitorApprovalDTO dto);

    void startVisit(Long id);

    void endVisit(Long id);

    void cancel(Long id, VisitorApprovalDTO dto);

    Map<String, Long> getStatusStatistics();

    Map<String, Long> getTypeStatistics();

    Map<String, Long> getDetailedStatistics();

    List<VisitorCalendarVO> getCalendarList(VisitorCalendarQueryDTO queryDTO);

    LawyerMeetingDetailVO getLawyerMeetingDetail(Long id);

    Visitor enrichVisitorDisplay(Visitor visitor);
}
