package com.prison.controller;

import com.prison.Result;
import com.prison.dto.DashboardVO;
import com.prison.mapper.*;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final PrisonerMapper prisonerMapper;
    private final GuardMapper guardMapper;
    private final CellMapper cellMapper;
    private final PatrolMapper patrolMapper;
    private final IncidentMapper incidentMapper;
    private final VisitorMapper visitorMapper;

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'GUARD', 'DOCTOR', 'VIEWER')")
    public Result<DashboardVO> getDashboard() {
        Long prisonerCount = prisonerMapper.selectCount(null);
        Long guardCount = guardMapper.selectCount(null);

        Long totalCapacity = cellMapper.selectList(null).stream()
                .mapToLong(c -> c.getCapacity() != null ? c.getCapacity() : 0)
                .sum();
        Long totalOccupancy = cellMapper.selectList(null).stream()
                .mapToLong(c -> c.getCurrentOccupancy() != null ? c.getCurrentOccupancy() : 0)
                .sum();
        double cellUsageRate = totalCapacity > 0
                ? Math.round((double) totalOccupancy / totalCapacity * 10000.0) / 100.0
                : 0.0;

        Long todayPatrolCount = patrolMapper.countTodayPatrols();
        Long pendingIncidentCount = incidentMapper.countPendingIncidents();
        Long todayVisitorCount = visitorMapper.countTodayVisitors();

        DashboardVO vo = DashboardVO.of(
                prisonerCount,
                guardCount,
                cellUsageRate,
                todayPatrolCount != null ? todayPatrolCount : 0,
                pendingIncidentCount != null ? pendingIncidentCount : 0,
                todayVisitorCount != null ? todayVisitorCount : 0
        );

        return Result.success(vo);
    }
}