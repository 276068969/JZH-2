package com.prison.controller;

import com.prison.Result;
import com.prison.dto.DashboardVO;
import com.prison.mapper.*;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.format.TextStyle;
import java.util.*;
import java.util.stream.Collectors;

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
        Long pendingVisitorCount = visitorMapper.countPendingVisitors();
        Long inProgressVisitorCount = visitorMapper.countInProgressVisitors();

        DashboardVO vo = DashboardVO.of(
                prisonerCount,
                guardCount,
                cellUsageRate,
                todayPatrolCount != null ? todayPatrolCount : 0,
                pendingIncidentCount != null ? pendingIncidentCount : 0,
                todayVisitorCount != null ? todayVisitorCount : 0,
                pendingVisitorCount != null ? pendingVisitorCount : 0,
                inProgressVisitorCount != null ? inProgressVisitorCount : 0
        );

        vo.setPrisonerTrend(buildPrisonerTrend());
        vo.setCellDistribution(buildCellDistribution());
        vo.setIncidentCategoryStats(buildIncidentCategoryStats());
        vo.setPatrolWeeklyStats(buildPatrolWeeklyStats());

        return Result.success(vo);
    }

    private DashboardVO.PrisonerTrend buildPrisonerTrend() {
        int year = LocalDate.now().getYear();
        DashboardVO.PrisonerTrend trend = new DashboardVO.PrisonerTrend();

        List<String> months = new ArrayList<>();
        List<Long> newEntries = new ArrayList<>();
        List<Long> releases = new ArrayList<>();
        List<Long> prisonerCounts = new ArrayList<>();

        for (int m = 1; m <= 12; m++) {
            months.add(m + "月");
        }

        Map<Integer, Long> entryMap = prisonerMapper.countNewEntriesByMonth(year).stream()
                .collect(Collectors.toMap(
                        row -> ((Number) row.get("m")).intValue(),
                        row -> ((Number) row.get("cnt")).longValue()
                ));

        Map<Integer, Long> releaseMap = prisonerMapper.countReleasesByMonth(year).stream()
                .collect(Collectors.toMap(
                        row -> ((Number) row.get("m")).intValue(),
                        row -> ((Number) row.get("cnt")).longValue()
                ));

        long cumulative = 0;
        for (int m = 1; m <= 12; m++) {
            long entry = entryMap.getOrDefault(m, 0L);
            long release = releaseMap.getOrDefault(m, 0L);
            cumulative += entry - release;
            newEntries.add(entry);
            releases.add(release);
            prisonerCounts.add(Math.max(cumulative, 0L));
        }

        trend.setMonths(months);
        trend.setNewEntries(newEntries);
        trend.setReleases(releases);
        trend.setPrisonerCounts(prisonerCounts);
        return trend;
    }

    private List<DashboardVO.CellDistribution> buildCellDistribution() {
        List<Map<String, Object>> statusCounts = cellMapper.countByStatus();
        Map<String, Long> statusMap = new HashMap<>();
        for (Map<String, Object> row : statusCounts) {
            String status = (String) row.get("status");
            Long cnt = ((Number) row.get("cnt")).longValue();
            statusMap.put(status, cnt);
        }

        List<DashboardVO.CellDistribution> distribution = new ArrayList<>();
        distribution.add(createCellDist("已满", statusMap.getOrDefault("FULL", 0L), "#f56c6c"));
        distribution.add(createCellDist("空闲", statusMap.getOrDefault("AVAILABLE", 0L), "#67c23a"));
        distribution.add(createCellDist("维护中", statusMap.getOrDefault("MAINTENANCE", 0L), "#e6a23c"));
        return distribution;
    }

    private DashboardVO.CellDistribution createCellDist(String name, Long value, String color) {
        DashboardVO.CellDistribution d = new DashboardVO.CellDistribution();
        d.setName(name);
        d.setValue(value);
        d.setColor(color);
        return d;
    }

    private List<DashboardVO.IncidentCategoryStat> buildIncidentCategoryStats() {
        List<Map<String, Object>> typeCounts = incidentMapper.countByTypeCurrentMonth();
        Map<String, Long> typeMap = new HashMap<>();
        for (Map<String, Object> row : typeCounts) {
            String type = (String) row.get("incident_type");
            Long cnt = ((Number) row.get("cnt")).longValue();
            typeMap.put(type, cnt);
        }

        List<DashboardVO.IncidentCategoryStat> stats = new ArrayList<>();
        stats.add(createIncidentStat("FIGHT", "打架", typeMap.getOrDefault("FIGHT", 0L), "#f56c6c"));
        stats.add(createIncidentStat("MEDICAL", "医疗", typeMap.getOrDefault("MEDICAL", 0L), "#409eff"));
        stats.add(createIncidentStat("ESCAPE_ATTEMPT", "逃跑", typeMap.getOrDefault("ESCAPE_ATTEMPT", 0L), "#e6a23c"));
        stats.add(createIncidentStat("DISCIPLINE", "违纪", typeMap.getOrDefault("DISCIPLINE", 0L), "#f56c6c"));
        stats.add(createIncidentStat("CONTRABAND", "违规物品", typeMap.getOrDefault("CONTRABAND", 0L), "#909399"));
        stats.add(createIncidentStat("OTHER", "其他", typeMap.getOrDefault("OTHER", 0L), "#67c23a"));
        return stats;
    }

    private DashboardVO.IncidentCategoryStat createIncidentStat(String type, String label, Long count, String color) {
        DashboardVO.IncidentCategoryStat s = new DashboardVO.IncidentCategoryStat();
        s.setType(type);
        s.setLabel(label);
        s.setCount(count);
        s.setColor(color);
        return s;
    }

    private DashboardVO.PatrolWeeklyStats buildPatrolWeeklyStats() {
        LocalDate today = LocalDate.now();
        LocalDate monday = today.with(DayOfWeek.MONDAY);

        List<String> days = new ArrayList<>();
        List<Long> planned = new ArrayList<>();
        List<Long> completed = new ArrayList<>();

        List<Map<String, Object>> weekData = patrolMapper.countByDayThisWeek();
        Map<String, Long> dayCountMap = new HashMap<>();
        for (Map<String, Object> row : weekData) {
            String dt = row.get("dt").toString();
            Long cnt = ((Number) row.get("cnt")).longValue();
            dayCountMap.put(dt, cnt);
        }

        for (int i = 0; i < 7; i++) {
            LocalDate d = monday.plusDays(i);
            DayOfWeek dow = d.getDayOfWeek();
            String dayLabel = dow.getDisplayName(TextStyle.SHORT, Locale.CHINESE);
            days.add(dayLabel);

            long basePlanned = (dow == DayOfWeek.SATURDAY || dow == DayOfWeek.SUNDAY) ? 20L : 24L;
            planned.add(basePlanned);

            String dateKey = d.toString();
            completed.add(dayCountMap.getOrDefault(dateKey, 0L));
        }

        DashboardVO.PatrolWeeklyStats stats = new DashboardVO.PatrolWeeklyStats();
        stats.setDays(days);
        stats.setPlanned(planned);
        stats.setCompleted(completed);
        return stats;
    }
}
