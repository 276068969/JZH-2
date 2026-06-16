package com.prison.controller;

import com.prison.Result;
import com.prison.dto.CellDTO;
import com.prison.entity.Cell;
import com.prison.service.CellService;
import com.prison.service.PrisonAreaService;
import com.prison.vo.CapacityWarningVO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/cells")
@RequiredArgsConstructor
public class CellController {

    private final CellService cellService;
    private final PrisonAreaService prisonAreaService;

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'GUARD', 'VIEWER')")
    public Result<?> list(@RequestParam(defaultValue = "1") int page,
                          @RequestParam(defaultValue = "10") int size,
                          @RequestParam(required = false) String keyword,
                          @RequestParam(required = false) Long areaId) {
        if (areaId != null) {
            return Result.success(cellService.pageCellsByAreaId(page, size, areaId, keyword));
        }
        return Result.success(cellService.pageCells(page, size, keyword));
    }

    @GetMapping("/all")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'GUARD', 'VIEWER')")
    public Result<List<Cell>> all(@RequestParam(required = false) Long areaId) {
        if (areaId != null) {
            return Result.success(cellService.listByAreaId(areaId));
        }
        return Result.success(cellService.list());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'GUARD', 'VIEWER')")
    public Result<Cell> getById(@PathVariable Long id) {
        return Result.success(cellService.getById(id));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public Result<?> create(@Valid @RequestBody CellDTO dto) {
        Cell cell = new Cell();
        BeanUtils.copyProperties(dto, cell);
        cellService.createCell(cell);
        return Result.success("创建成功");
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public Result<?> update(@PathVariable Long id, @Valid @RequestBody CellDTO dto) {
        Cell cell = new Cell();
        BeanUtils.copyProperties(dto, cell);
        cellService.updateCell(id, cell);
        return Result.success("更新成功");
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<?> delete(@PathVariable Long id) {
        cellService.deleteCell(id);
        return Result.success("删除成功");
    }

    @PostMapping("/{id}/sync-occupancy")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public Result<?> syncOccupancy(@PathVariable Long id) {
        cellService.syncOccupancy(id);
        return Result.success("同步完成");
    }

    @PostMapping("/sync-all-occupancy")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public Result<?> syncAllOccupancy() {
        cellService.syncAllOccupancy();
        return Result.success("全量同步完成");
    }

    @GetMapping("/warnings")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'GUARD', 'VIEWER')")
    public Result<CapacityWarningVO> warnings() {
        return Result.success(prisonAreaService.getCapacityWarnings());
    }

    @GetMapping("/by-area/{areaId}/warnings")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'GUARD', 'VIEWER')")
    public Result<CapacityWarningVO> warningsByAreaId(@PathVariable Long areaId) {
        return Result.success(prisonAreaService.getCapacityWarningsByAreaId(areaId));
    }
}
