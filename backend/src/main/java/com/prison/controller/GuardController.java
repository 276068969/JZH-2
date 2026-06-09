package com.prison.controller;

import com.prison.Result;
import com.prison.dto.GuardDTO;
import com.prison.entity.Guard;
import com.prison.service.GuardService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/guards")
@RequiredArgsConstructor
public class GuardController {

    private final GuardService guardService;

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'GUARD', 'VIEWER')")
    public Result<?> list(@RequestParam(defaultValue = "1") int page,
                          @RequestParam(defaultValue = "10") int size,
                          @RequestParam(required = false) String keyword) {
        return Result.success(guardService.pageGuards(page, size, keyword));
    }

    @GetMapping("/all")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'GUARD', 'VIEWER')")
    public Result<List<Guard>> all() {
        return Result.success(guardService.list());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'GUARD', 'VIEWER')")
    public Result<Guard> getById(@PathVariable Long id) {
        return Result.success(guardService.getById(id));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public Result<?> create(@Valid @RequestBody GuardDTO dto) {
        Guard guard = new Guard();
        BeanUtils.copyProperties(dto, guard);
        guardService.save(guard);
        return Result.success("创建成功");
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public Result<?> update(@PathVariable Long id, @Valid @RequestBody GuardDTO dto) {
        Guard guard = new Guard();
        BeanUtils.copyProperties(dto, guard);
        guard.setId(id);
        guardService.updateById(guard);
        return Result.success("更新成功");
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<?> delete(@PathVariable Long id) {
        guardService.removeById(id);
        return Result.success("删除成功");
    }
}