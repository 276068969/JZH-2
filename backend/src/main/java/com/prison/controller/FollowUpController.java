package com.prison.controller;

import com.prison.Result;
import com.prison.dto.FollowUpMarkDTO;
import com.prison.dto.FollowUpQueryDTO;
import com.prison.service.FollowUpService;
import com.prison.vo.FollowUpStatsVO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/follow-up")
@RequiredArgsConstructor
public class FollowUpController {

    private final FollowUpService followUpService;

    @GetMapping("/workbench")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'DOCTOR', 'VIEWER')")
    public Result<?> workbench(FollowUpQueryDTO queryDTO) {
        return Result.success(followUpService.pageFollowUpWorkbench(queryDTO));
    }

    @GetMapping("/stats")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'DOCTOR', 'VIEWER')")
    public Result<FollowUpStatsVO> stats() {
        return Result.success(followUpService.getFollowUpStats());
    }

    @PostMapping("/mark")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'DOCTOR')")
    public Result<?> mark(@Valid @RequestBody FollowUpMarkDTO dto) {
        followUpService.markFollowUp(dto);
        return Result.success("操作成功");
    }
}
