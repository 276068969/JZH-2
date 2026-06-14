package com.prison.controller;

import com.prison.Result;
import com.prison.dto.VisitorApprovalDTO;
import com.prison.dto.VisitorCalendarQueryDTO;
import com.prison.dto.VisitorDTO;
import com.prison.entity.Visitor;
import com.prison.service.VisitorService;
import com.prison.vo.LawyerMeetingDetailVO;
import com.prison.vo.VisitorCalendarVO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/visitors")
@RequiredArgsConstructor
public class VisitorController {

    private final VisitorService visitorService;

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'GUARD', 'VIEWER')")
    public Result<?> list(@RequestParam(defaultValue = "1") int page,
                          @RequestParam(defaultValue = "10") int size,
                          @RequestParam(required = false) String keyword,
                          @RequestParam(required = false) String status,
                          @RequestParam(required = false) String visitType,
                          @RequestParam(required = false) String relation) {
        return Result.success(visitorService.pageVisitors(page, size, keyword, status, visitType, relation));
    }

    @GetMapping("/lawyer")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'GUARD', 'VIEWER')")
    public Result<?> lawyerList(@RequestParam(defaultValue = "1") int page,
                                @RequestParam(defaultValue = "10") int size,
                                @RequestParam(required = false) String keyword,
                                @RequestParam(required = false) String status) {
        return Result.success(visitorService.pageLawyerVisitors(page, size, keyword, status));
    }

    @GetMapping("/family")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'GUARD', 'VIEWER')")
    public Result<?> familyList(@RequestParam(defaultValue = "1") int page,
                                @RequestParam(defaultValue = "10") int size,
                                @RequestParam(required = false) String keyword,
                                @RequestParam(required = false) String status) {
        return Result.success(visitorService.pageFamilyVisitors(page, size, keyword, status));
    }

    @GetMapping("/pending")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'GUARD')")
    public Result<?> pendingList(@RequestParam(defaultValue = "1") int page,
                                 @RequestParam(defaultValue = "10") int size,
                                 @RequestParam(required = false) String visitType) {
        return Result.success(visitorService.pagePendingVisitors(page, size, visitType));
    }

    @GetMapping("/all")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'GUARD', 'VIEWER')")
    public Result<List<Visitor>> all() {
        List<Visitor> visitors = visitorService.list();
        visitors.forEach(visitorService::enrichVisitorDisplay);
        return Result.success(visitors);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'GUARD', 'VIEWER')")
    public Result<Visitor> getById(@PathVariable Long id) {
        Visitor visitor = visitorService.getById(id);
        visitorService.enrichVisitorDisplay(visitor);
        return Result.success(visitor);
    }

    @GetMapping("/{id}/lawyer-detail")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'GUARD', 'VIEWER')")
    public Result<LawyerMeetingDetailVO> getLawyerDetail(@PathVariable Long id) {
        return Result.success(visitorService.getLawyerMeetingDetail(id));
    }

    @GetMapping("/statistics/status")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'GUARD', 'VIEWER')")
    public Result<Map<String, Long>> statusStatistics() {
        return Result.success(visitorService.getStatusStatistics());
    }

    @GetMapping("/statistics/type")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'GUARD', 'VIEWER')")
    public Result<Map<String, Long>> typeStatistics() {
        return Result.success(visitorService.getTypeStatistics());
    }

    @GetMapping("/statistics/detailed")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'GUARD', 'VIEWER')")
    public Result<Map<String, Long>> detailedStatistics() {
        return Result.success(visitorService.getDetailedStatistics());
    }

    @GetMapping("/calendar")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'GUARD', 'VIEWER')")
    public Result<List<VisitorCalendarVO>> calendar(
            @RequestParam(required = false) LocalDate startDate,
            @RequestParam(required = false) LocalDate endDate,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String visitType,
            @RequestParam(required = false) String relation) {
        VisitorCalendarQueryDTO queryDTO = new VisitorCalendarQueryDTO();
        queryDTO.setStartDate(startDate);
        queryDTO.setEndDate(endDate);
        queryDTO.setStatus(status);
        queryDTO.setVisitType(visitType);
        queryDTO.setRelation(relation);
        return Result.success(visitorService.getCalendarList(queryDTO));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'GUARD')")
    public Result<?> create(@Valid @RequestBody VisitorDTO dto) {
        visitorService.createVisitor(dto);
        return Result.success("创建成功");
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'GUARD')")
    public Result<?> update(@PathVariable Long id, @Valid @RequestBody VisitorDTO dto) {
        visitorService.updateVisitor(id, dto);
        return Result.success("更新成功");
    }

    @PostMapping("/{id}/approve")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'GUARD')")
    public Result<?> approve(@PathVariable Long id, @Valid @RequestBody VisitorApprovalDTO dto) {
        visitorService.approve(id, dto);
        return Result.success("审批通过");
    }

    @PostMapping("/{id}/reject")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'GUARD')")
    public Result<?> reject(@PathVariable Long id, @Valid @RequestBody VisitorApprovalDTO dto) {
        visitorService.reject(id, dto);
        return Result.success("已驳回");
    }

    @PostMapping("/{id}/start")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'GUARD')")
    public Result<?> startVisit(@PathVariable Long id) {
        visitorService.startVisit(id);
        return Result.success("会见已开始");
    }

    @PostMapping("/{id}/end")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'GUARD')")
    public Result<?> endVisit(@PathVariable Long id) {
        visitorService.endVisit(id);
        return Result.success("会见已结束");
    }

    @PostMapping("/{id}/cancel")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'GUARD')")
    public Result<?> cancel(@PathVariable Long id, @Valid @RequestBody VisitorApprovalDTO dto) {
        visitorService.cancel(id, dto);
        return Result.success("已取消");
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public Result<?> delete(@PathVariable Long id) {
        visitorService.removeById(id);
        return Result.success("删除成功");
    }
}
