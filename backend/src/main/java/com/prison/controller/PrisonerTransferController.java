package com.prison.controller;

import com.prison.Result;
import com.prison.dto.PrisonerTransferDTO;
import com.prison.dto.PrisonerTransferQueryDTO;
import com.prison.entity.PrisonerTransfer;
import com.prison.service.PrisonerTransferService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/prisoner-transfers")
@RequiredArgsConstructor
public class PrisonerTransferController {

    private final PrisonerTransferService prisonerTransferService;

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'GUARD', 'VIEWER')")
    public Result<?> page(PrisonerTransferQueryDTO queryDTO) {
        return Result.success(prisonerTransferService.pageTransfers(queryDTO));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'GUARD', 'VIEWER')")
    public Result<PrisonerTransfer> getById(@PathVariable Long id) {
        return Result.success(prisonerTransferService.getTransferById(id));
    }

    @GetMapping("/prisoner/{prisonerId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'GUARD', 'VIEWER')")
    public Result<List<PrisonerTransfer>> getByPrisonerId(@PathVariable Long prisonerId) {
        return Result.success(prisonerTransferService.getTransfersByPrisonerId(prisonerId));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public Result<?> create(@Valid @RequestBody PrisonerTransferDTO dto) {
        prisonerTransferService.createTransfer(dto);
        return Result.success("调动登记成功");
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public Result<?> update(@PathVariable Long id, @RequestBody PrisonerTransferDTO dto) {
        prisonerTransferService.updateTransfer(id, dto);
        return Result.success("更新成功");
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<?> delete(@PathVariable Long id) {
        prisonerTransferService.deleteTransfer(id);
        return Result.success("删除成功");
    }
}
