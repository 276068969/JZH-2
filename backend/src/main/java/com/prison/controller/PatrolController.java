package com.prison.controller;

import com.prison.Result;
import com.prison.dto.PatrolAbnormalQueryDTO;
import com.prison.dto.PatrolDTO;
import com.prison.entity.Patrol;
import com.prison.service.PatrolService;
import com.prison.vo.PatrolAbnormalSummaryVO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/patrols")
@RequiredArgsConstructor
public class PatrolController {

    private final PatrolService patrolService;

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'GUARD', 'VIEWER')")
    public Result<?> list(@RequestParam(defaultValue = "1") int page,
                          @RequestParam(defaultValue = "10") int size,
                          @RequestParam(required = false) String keyword) {
        return Result.success(patrolService.pagePatrols(page, size, keyword));
    }

    @GetMapping("/all")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'GUARD', 'VIEWER')")
    public Result<List<Patrol>> all() {
        return Result.success(patrolService.list());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'GUARD', 'VIEWER')")
    public Result<Patrol> getById(@PathVariable Long id) {
        return Result.success(patrolService.getById(id));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'GUARD')")
    public Result<?> create(@Valid @RequestBody PatrolDTO dto) {
        Patrol patrol = new Patrol();
        BeanUtils.copyProperties(dto, patrol);
        patrolService.save(patrol);
        return Result.success("创建成功");
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'GUARD')")
    public Result<?> update(@PathVariable Long id, @Valid @RequestBody PatrolDTO dto) {
        Patrol patrol = new Patrol();
        BeanUtils.copyProperties(dto, patrol);
        patrol.setId(id);
        patrolService.updateById(patrol);
        return Result.success("更新成功");
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public Result<?> delete(@PathVariable Long id) {
        patrolService.removeById(id);
        return Result.success("删除成功");
    }

    @GetMapping("/abnormal-summary")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'VIEWER')")
    public Result<PatrolAbnormalSummaryVO> abnormalSummary(PatrolAbnormalQueryDTO query) {
        return Result.success(patrolService.abnormalSummary(query));
    }
}