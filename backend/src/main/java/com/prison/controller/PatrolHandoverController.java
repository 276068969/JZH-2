package com.prison.controller;

import com.prison.Result;
import com.prison.dto.PatrolHandoverConfirmDTO;
import com.prison.dto.PatrolHandoverDTO;
import com.prison.dto.PatrolHandoverQueryDTO;
import com.prison.entity.PatrolHandover;
import com.prison.service.PatrolHandoverService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/patrol-handovers")
@RequiredArgsConstructor
public class PatrolHandoverController {

    private final PatrolHandoverService patrolHandoverService;

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'GUARD', 'VIEWER')")
    public Result<?> list(PatrolHandoverQueryDTO query) {
        return Result.success(patrolHandoverService.pageHandovers(query));
    }

    @GetMapping("/all")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'GUARD', 'VIEWER')")
    public Result<List<PatrolHandover>> all() {
        return Result.success(patrolHandoverService.list());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'GUARD', 'VIEWER')")
    public Result<PatrolHandover> getById(@PathVariable Long id) {
        return Result.success(patrolHandoverService.getById(id));
    }

    @GetMapping("/latest")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'GUARD', 'VIEWER')")
    public Result<PatrolHandover> getLatest(@RequestParam Long areaId) {
        return Result.success(patrolHandoverService.getLatestHandoverByArea(areaId));
    }

    @GetMapping("/recent")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'GUARD', 'VIEWER')")
    public Result<List<PatrolHandover>> getRecent(
            @RequestParam(required = false) Long areaId,
            @RequestParam(defaultValue = "10") Integer limit) {
        return Result.success(patrolHandoverService.getRecentHandovers(areaId, limit));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'GUARD')")
    public Result<PatrolHandover> create(@Valid @RequestBody PatrolHandoverDTO dto) {
        return Result.success(patrolHandoverService.createHandover(dto));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'GUARD')")
    public Result<?> update(@PathVariable Long id, @Valid @RequestBody PatrolHandoverDTO dto) {
        PatrolHandover handover = new PatrolHandover();
        org.springframework.beans.BeanUtils.copyProperties(dto, handover);
        handover.setId(id);
        patrolHandoverService.updateById(handover);
        return Result.success("更新成功");
    }

    @PutMapping("/{id}/confirm")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'GUARD')")
    public Result<PatrolHandover> confirm(@PathVariable Long id,
                                          @Valid @RequestBody PatrolHandoverConfirmDTO dto) {
        return Result.success(patrolHandoverService.confirmHandover(
                id, dto.getIncomingGuardId(), dto.getIncomingGuardName()));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public Result<?> delete(@PathVariable Long id) {
        patrolHandoverService.removeById(id);
        return Result.success("删除成功");
    }
}
