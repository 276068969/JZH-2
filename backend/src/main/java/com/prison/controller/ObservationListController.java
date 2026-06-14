package com.prison.controller;

import com.prison.Result;
import com.prison.dto.ObservationListQueryDTO;
import com.prison.service.ObservationListService;
import com.prison.vo.ObservationStatsVO;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/observation-list")
@RequiredArgsConstructor
public class ObservationListController {

    private final ObservationListService observationListService;

    @GetMapping("/page")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'GUARD', 'DOCTOR', 'VIEWER')")
    public Result<?> pageObservationList(ObservationListQueryDTO queryDTO) {
        return Result.success(observationListService.pageObservationList(queryDTO));
    }

    @GetMapping("/stats")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'GUARD', 'DOCTOR', 'VIEWER')")
    public Result<ObservationStatsVO> getObservationStats(
            @RequestParam(required = false) Integer incidentDaysThreshold) {
        return Result.success(observationListService.getObservationStats(incidentDaysThreshold));
    }
}
