package com.prison.controller;

import com.prison.Result;
import com.prison.dto.PrisonAreaDTO;
import com.prison.entity.PrisonArea;
import com.prison.service.PrisonAreaService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/prison-areas")
@RequiredArgsConstructor
public class PrisonAreaController {

    private final PrisonAreaService prisonAreaService;

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'GUARD', 'VIEWER')")
    public Result<?> list(@RequestParam(defaultValue = "1") int page,
                          @RequestParam(defaultValue = "10") int size,
                          @RequestParam(required = false) String keyword) {
        return Result.success(prisonAreaService.pagePrisonAreas(page, size, keyword));
    }

    @GetMapping("/all")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'GUARD', 'DOCTOR', 'VIEWER')")
    public Result<List<PrisonArea>> all() {
        return Result.success(prisonAreaService.list());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'GUARD', 'VIEWER')")
    public Result<PrisonArea> getById(@PathVariable Long id) {
        return Result.success(prisonAreaService.getById(id));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public Result<?> create(@Valid @RequestBody PrisonAreaDTO dto) {
        PrisonArea area = new PrisonArea();
        BeanUtils.copyProperties(dto, area);
        prisonAreaService.save(area);
        return Result.success("创建成功");
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public Result<?> update(@PathVariable Long id, @Valid @RequestBody PrisonAreaDTO dto) {
        PrisonArea area = new PrisonArea();
        BeanUtils.copyProperties(dto, area);
        area.setId(id);
        prisonAreaService.updateById(area);
        return Result.success("更新成功");
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<?> delete(@PathVariable Long id) {
        prisonAreaService.removeById(id);
        return Result.success("删除成功");
    }
}