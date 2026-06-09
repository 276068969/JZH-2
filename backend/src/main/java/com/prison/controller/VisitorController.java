package com.prison.controller;

import com.prison.Result;
import com.prison.dto.VisitorApprovalDTO;
import com.prison.dto.VisitorDTO;
import com.prison.entity.Visitor;
import com.prison.service.VisitorService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

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
                          @RequestParam(required = false) String visitType) {
        return Result.success(visitorService.pageVisitors(page, size, keyword, status, visitType));
    }

    @GetMapping("/pending")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'GUARD')")
    public Result<?> pendingList(@RequestParam(defaultValue = "1") int page,
                                 @RequestParam(defaultValue = "10") int size) {
        return Result.success(visitorService.pagePendingVisitors(page, size));
    }

    @GetMapping("/all")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'GUARD', 'VIEWER')")
    public Result<List<Visitor>> all() {
        return Result.success(visitorService.list());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'GUARD', 'VIEWER')")
    public Result<Visitor> getById(@PathVariable Long id) {
        return Result.success(visitorService.getById(id));
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

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'GUARD')")
    public Result<?> create(@Valid @RequestBody VisitorDTO dto) {
        Visitor visitor = new Visitor();
        BeanUtils.copyProperties(dto, visitor);
        if (visitor.getStatus() == null) {
            visitor.setStatus("PENDING");
        }
        visitorService.save(visitor);
        return Result.success("创建成功");
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'GUARD')")
    public Result<?> update(@PathVariable Long id, @Valid @RequestBody VisitorDTO dto) {
        Visitor visitor = new Visitor();
        BeanUtils.copyProperties(dto, visitor);
        visitor.setId(id);
        visitorService.updateById(visitor);
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

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public Result<?> delete(@PathVariable Long id) {
        visitorService.removeById(id);
        return Result.success("删除成功");
    }
}
