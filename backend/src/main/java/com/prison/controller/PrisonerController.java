package com.prison.controller;

import com.prison.Result;
import com.prison.dto.PrisonerDTO;
import com.prison.dto.PrisonerQueryDTO;
import com.prison.dto.ReleaseBoardVO;
import com.prison.dto.ReleaseWarningVO;
import com.prison.entity.Prisoner;
import com.prison.service.PrisonerService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/prisoners")
@RequiredArgsConstructor
public class PrisonerController {

    private final PrisonerService prisonerService;

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'GUARD', 'DOCTOR', 'VIEWER')")
    public Result<?> list(@RequestParam(defaultValue = "1") int page,
                          @RequestParam(defaultValue = "10") int size,
                          @RequestParam(required = false) String keyword) {
        return Result.success(prisonerService.pagePrisoners(page, size, keyword));
    }

    @PostMapping("/search")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'GUARD', 'VIEWER')")
    public Result<?> advancedSearch(@RequestBody PrisonerQueryDTO queryDTO) {
        return Result.success(prisonerService.advancedSearch(queryDTO));
    }

    @GetMapping("/all")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'GUARD', 'VIEWER')")
    public Result<List<Prisoner>> all() {
        return Result.success(prisonerService.list());
    }

    @GetMapping("/release-warnings")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'GUARD', 'VIEWER')")
    public Result<List<ReleaseWarningVO>> releaseWarnings(
            @RequestParam(required = false) Integer days,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String dangerLevel) {
        return Result.success(prisonerService.getReleaseWarnings(days, status, dangerLevel));
    }

    @GetMapping("/release-board")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'GUARD', 'VIEWER')")
    public Result<ReleaseBoardVO.BoardData> releaseBoard(
            @RequestParam(required = false) Integer days,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String dangerLevel,
            @RequestParam(required = false) Long areaId) {
        return Result.success(prisonerService.getReleaseBoard(days, status, dangerLevel, areaId));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'GUARD', 'DOCTOR', 'VIEWER')")
    public Result<Prisoner> getById(@PathVariable Long id) {
        return Result.success(prisonerService.getById(id));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public Result<?> create(@Valid @RequestBody PrisonerDTO dto) {
        Prisoner prisoner = new Prisoner();
        BeanUtils.copyProperties(dto, prisoner);
        prisonerService.createPrisoner(prisoner);
        return Result.success("创建成功");
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public Result<?> update(@PathVariable Long id, @Valid @RequestBody PrisonerDTO dto) {
        Prisoner prisoner = new Prisoner();
        BeanUtils.copyProperties(dto, prisoner);
        prisonerService.updatePrisoner(id, prisoner);
        return Result.success("更新成功");
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<?> delete(@PathVariable Long id) {
        prisonerService.deletePrisoner(id);
        return Result.success("删除成功");
    }
}