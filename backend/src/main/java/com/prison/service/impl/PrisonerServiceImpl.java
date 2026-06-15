package com.prison.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.prison.config.BusinessException;
import com.prison.dto.PrisonerQueryDTO;
import com.prison.dto.ReleaseBoardVO;
import com.prison.dto.ReleaseWarningVO;
import com.prison.entity.Prisoner;
import com.prison.enums.SysLogAction;
import com.prison.enums.SysLogModule;
import com.prison.mapper.IncidentMapper;
import com.prison.mapper.PrisonerMapper;
import com.prison.service.CellService;
import com.prison.service.PrisonerService;
import com.prison.service.SysLogService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class PrisonerServiceImpl extends ServiceImpl<PrisonerMapper, Prisoner> implements PrisonerService {

    private final CellService cellService;
    private final SysLogService sysLogService;
    private final IncidentMapper incidentMapper;

    @Override
    public Page<Prisoner> pagePrisoners(int page, int size, String keyword) {
        LambdaQueryWrapper<Prisoner> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(keyword)) {
            wrapper.like(Prisoner::getName, keyword)
                    .or()
                    .like(Prisoner::getPrisonerNumber, keyword)
                    .or()
                    .like(Prisoner::getIdCard, keyword);
        }
        wrapper.orderByDesc(Prisoner::getCreateTime);
        return page(new Page<>(page, size), wrapper);
    }

    @Override
    public Page<Prisoner> advancedSearch(PrisonerQueryDTO queryDTO) {
        LambdaQueryWrapper<Prisoner> wrapper = new LambdaQueryWrapper<>();

        if (StringUtils.hasText(queryDTO.getKeyword())) {
            wrapper.and(w -> w.like(Prisoner::getName, queryDTO.getKeyword())
                    .or()
                    .like(Prisoner::getPrisonerNumber, queryDTO.getKeyword())
                    .or()
                    .like(Prisoner::getIdCard, queryDTO.getKeyword()));
        }

        if (queryDTO.getAreaId() != null) {
            wrapper.eq(Prisoner::getAreaId, queryDTO.getAreaId());
        }

        if (StringUtils.hasText(queryDTO.getDangerLevel())) {
            wrapper.eq(Prisoner::getDangerLevel, queryDTO.getDangerLevel());
        }

        if (StringUtils.hasText(queryDTO.getStatus())) {
            wrapper.eq(Prisoner::getStatus, queryDTO.getStatus());
        }

        if (StringUtils.hasText(queryDTO.getGender())) {
            wrapper.eq(Prisoner::getGender, queryDTO.getGender());
        }

        if (StringUtils.hasText(queryDTO.getCrimeType())) {
            wrapper.like(Prisoner::getCrimeType, queryDTO.getCrimeType());
        }

        if (queryDTO.getMinAge() != null || queryDTO.getMaxAge() != null) {
            LocalDate today = LocalDate.now();
            if (queryDTO.getMaxAge() != null) {
                LocalDate minBirthDate = today.minusYears(queryDTO.getMaxAge());
                wrapper.ge(Prisoner::getBirthDate, minBirthDate);
            }
            if (queryDTO.getMinAge() != null) {
                LocalDate maxBirthDate = today.minusYears(queryDTO.getMinAge());
                wrapper.le(Prisoner::getBirthDate, maxBirthDate);
            }
        }

        wrapper.orderByDesc(Prisoner::getCreateTime);

        int pageNum = queryDTO.getPage() != null ? queryDTO.getPage() : 1;
        int pageSize = queryDTO.getSize() != null ? queryDTO.getSize() : 10;

        return page(new Page<>(pageNum, pageSize), wrapper);
    }

    @Override
    public List<ReleaseWarningVO> getReleaseWarnings(Integer days, String status, String dangerLevel) {
        List<ReleaseWarningVO> result = new ArrayList<>();

        if (days != null && days > 0) {
            ReleaseWarningVO vo = buildWarningVO(days, status, dangerLevel);
            result.add(vo);
        } else {
            result.add(buildWarningVO(30, status, dangerLevel));
            result.add(buildWarningVO(60, status, dangerLevel));
            result.add(buildWarningVO(90, status, dangerLevel));
        }

        return result;
    }

    @Override
    @Transactional
    public void createPrisoner(Prisoner prisoner) {
        if (prisoner.getCellId() != null) {
            cellService.validateCanAssign(prisoner.getCellId());
            save(prisoner);
            cellService.incrementOccupancy(prisoner.getCellId());
        } else {
            save(prisoner);
        }

        sysLogService.logSuccess(
                SysLogModule.PRISONER,
                SysLogAction.CREATE,
                "新增服刑人员：" + prisoner.getName() + "（编号：" + prisoner.getPrisonerNumber() + "）",
                "PRISONER",
                prisoner.getId(),
                prisoner.getName()
        );
    }

    @Override
    @Transactional
    public void updatePrisoner(Long id, Prisoner prisoner) {
        Prisoner existing = getById(id);
        if (existing == null) {
            throw new BusinessException("服刑人员不存在");
        }

        Long oldCellId = existing.getCellId();
        Long newCellId = prisoner.getCellId();

        boolean cellChanged = !cellIdsEqual(oldCellId, newCellId);

        if (cellChanged) {
            if (newCellId != null) {
                cellService.validateCanAssign(newCellId);
            }
            prisoner.setId(id);
            updateById(prisoner);
            if (oldCellId != null) {
                cellService.decrementOccupancy(oldCellId);
            }
            if (newCellId != null) {
                cellService.incrementOccupancy(newCellId);
            }
        } else {
            prisoner.setId(id);
            updateById(prisoner);
        }

        sysLogService.logSuccess(
                SysLogModule.PRISONER,
                SysLogAction.UPDATE,
                "修改服刑人员信息：" + existing.getName() + "（编号：" + existing.getPrisonerNumber() + "）",
                "PRISONER",
                id,
                existing.getName()
        );
    }

    @Override
    @Transactional
    public void deletePrisoner(Long id) {
        Prisoner existing = getById(id);
        if (existing == null) {
            throw new BusinessException("服刑人员不存在");
        }

        Long cellId = existing.getCellId();
        String prisonerName = existing.getName();
        String prisonerNumber = existing.getPrisonerNumber();
        removeById(id);

        if (cellId != null) {
            cellService.decrementOccupancy(cellId);
        }

        sysLogService.logSuccess(
                SysLogModule.PRISONER,
                SysLogAction.DELETE,
                "删除服刑人员：" + prisonerName + "（编号：" + prisonerNumber + "）",
                "PRISONER",
                id,
                prisonerName
        );
    }

    private boolean cellIdsEqual(Long a, Long b) {
        if (a == null && b == null) return true;
        if (a == null || b == null) return false;
        return a.equals(b);
    }

    private ReleaseWarningVO buildWarningVO(int days, String status, String dangerLevel) {
        LocalDate today = LocalDate.now();
        LocalDate endDate = today.plusDays(days);

        LambdaQueryWrapper<Prisoner> wrapper = new LambdaQueryWrapper<>();
        wrapper.isNotNull(Prisoner::getReleaseDate)
                .ge(Prisoner::getReleaseDate, today)
                .le(Prisoner::getReleaseDate, endDate);

        if (StringUtils.hasText(status)) {
            wrapper.eq(Prisoner::getStatus, status);
        } else {
            wrapper.in(Prisoner::getStatus, "INCARCERATED", "TRANSFERRED", "MEDICAL_PAROLE");
        }

        if (StringUtils.hasText(dangerLevel)) {
            wrapper.eq(Prisoner::getDangerLevel, dangerLevel);
        }

        wrapper.orderByAsc(Prisoner::getReleaseDate);

        List<Prisoner> prisoners = list(wrapper);

        log.info("临释预警查询: {}天, today={}, endDate={}, status={}, dangerLevel={}, 匹配人数={}",
                days, today, endDate, status, dangerLevel, prisoners.size());

        String label = days + "天内临释人员";
        return new ReleaseWarningVO(days, label, prisoners.size(), prisoners);
    }

    @Override
    public ReleaseBoardVO.BoardData getReleaseBoard(Integer days, String status, String dangerLevel, Long areaId) {
        LocalDate today = LocalDate.now();
        int maxDays = (days != null && days > 0) ? days : 90;
        LocalDate endDate = today.plusDays(maxDays);

        LambdaQueryWrapper<Prisoner> wrapper = new LambdaQueryWrapper<>();
        wrapper.isNotNull(Prisoner::getReleaseDate)
                .ge(Prisoner::getReleaseDate, today)
                .le(Prisoner::getReleaseDate, endDate);

        if (StringUtils.hasText(status)) {
            wrapper.eq(Prisoner::getStatus, status);
        } else {
            wrapper.in(Prisoner::getStatus, "INCARCERATED", "TRANSFERRED", "MEDICAL_PAROLE");
        }

        if (StringUtils.hasText(dangerLevel)) {
            wrapper.eq(Prisoner::getDangerLevel, dangerLevel);
        }

        if (areaId != null) {
            wrapper.eq(Prisoner::getAreaId, areaId);
        }

        wrapper.orderByAsc(Prisoner::getReleaseDate);

        List<Prisoner> prisoners = list(wrapper);

        Map<Long, String> areaNameCache = new HashMap<>();
        Map<Long, List<ReleaseBoardVO.RecentIncident>> incidentMap = new HashMap<>();

        if (!prisoners.isEmpty()) {
            String prisonerIds = prisoners.stream()
                    .map(p -> String.valueOf(p.getId()))
                    .collect(Collectors.joining(","));

            try {
                List<Map<String, Object>> incidentRows = incidentMapper.findRecentByPrisonerIds(prisonerIds);
                for (Map<String, Object> row : incidentRows) {
                    Long prisonerId = ((Number) row.get("related_prisoner_id")).longValue();
                    ReleaseBoardVO.RecentIncident inc = new ReleaseBoardVO.RecentIncident(
                            ((Number) row.get("id")).longValue(),
                            (String) row.get("incident_title"),
                            (String) row.get("incident_type"),
                            (String) row.get("severity"),
                            (String) row.get("status"),
                            row.get("occur_time") != null ? row.get("occur_time").toString() : null
                    );
                    incidentMap.computeIfAbsent(prisonerId, k -> new ArrayList<>()).add(inc);
                }
            } catch (Exception e) {
                log.warn("批量查询事件失败，改为逐条查询", e);
                for (Prisoner p : prisoners) {
                    try {
                        List<Map<String, Object>> rows = incidentMapper.findRecentByPrisonerId(p.getId());
                        List<ReleaseBoardVO.RecentIncident> incs = rows.stream().map(row -> new ReleaseBoardVO.RecentIncident(
                                ((Number) row.get("id")).longValue(),
                                (String) row.get("incident_title"),
                                (String) row.get("incident_type"),
                                (String) row.get("severity"),
                                (String) row.get("status"),
                                row.get("occur_time") != null ? row.get("occur_time").toString() : null
                        )).collect(Collectors.toList());
                        incidentMap.put(p.getId(), incs);
                    } catch (Exception ex) {
                        log.warn("查询服刑人员{}事件失败", p.getId(), ex);
                    }
                }
            }
        }

        List<ReleaseBoardVO> boardPrisoners = prisoners.stream().map(p -> {
            ReleaseBoardVO vo = new ReleaseBoardVO();
            vo.setId(p.getId());
            vo.setPrisonerNumber(p.getPrisonerNumber());
            vo.setName(p.getName());
            vo.setGender(p.getGender());
            vo.setCrimeType(p.getCrimeType());
            vo.setReleaseDate(p.getReleaseDate());
            vo.setRemainingDays(ChronoUnit.DAYS.between(today, p.getReleaseDate()));
            vo.setWarningLevel(calcWarningLevel(vo.getRemainingDays()));
            vo.setAreaId(p.getAreaId());
            vo.setCellId(p.getCellId());
            vo.setDangerLevel(p.getDangerLevel());
            vo.setStatus(p.getStatus());
            vo.setHealthStatus(p.getHealthStatus());
            vo.setRemark(p.getRemark());

            if (p.getAreaId() != null) {
                vo.setAreaName(areaNameCache.computeIfAbsent(p.getAreaId(),
                        aid -> {
                            String name = baseMapper.getAreaNameById(aid);
                            return name != null ? name : "未知监区";
                        }
                ));
            }

            vo.setRecentIncidents(incidentMap.getOrDefault(p.getId(), Collections.emptyList()));
            return vo;
        }).collect(Collectors.toList());

        int total = boardPrisoners.size();
        int urgent30 = (int) boardPrisoners.stream().filter(p -> p.getRemainingDays() != null && p.getRemainingDays() <= 30).count();
        int warning60 = (int) boardPrisoners.stream().filter(p -> p.getRemainingDays() != null && p.getRemainingDays() > 30 && p.getRemainingDays() <= 60).count();
        int notice90 = (int) boardPrisoners.stream().filter(p -> p.getRemainingDays() != null && p.getRemainingDays() > 60 && p.getRemainingDays() <= 90).count();
        int highDangerCount = (int) boardPrisoners.stream().filter(p -> "HIGH".equals(p.getDangerLevel())).count();
        int extremeDangerCount = (int) boardPrisoners.stream().filter(p -> "EXTREME".equals(p.getDangerLevel())).count();

        ReleaseBoardVO.Stats stats = new ReleaseBoardVO.Stats(total, urgent30, warning60, notice90, highDangerCount, extremeDangerCount);

        Map<String, Integer> areaDistMap = new LinkedHashMap<>();
        for (ReleaseBoardVO p : boardPrisoners) {
            String an = p.getAreaName() != null ? p.getAreaName() : "未分配监区";
            areaDistMap.merge(an, 1, Integer::sum);
        }
        List<ReleaseBoardVO.AreaDistribution> areaDistribution = areaDistMap.entrySet().stream()
                .map(e -> new ReleaseBoardVO.AreaDistribution(e.getKey(), e.getValue()))
                .collect(Collectors.toList());

        Map<String, Integer> dangerDistMap = new LinkedHashMap<>();
        String[] dangerOrder = {"EXTREME", "HIGH", "MEDIUM", "LOW"};
        for (ReleaseBoardVO p : boardPrisoners) {
            String dl = p.getDangerLevel() != null ? p.getDangerLevel() : "UNKNOWN";
            dangerDistMap.merge(dl, 1, Integer::sum);
        }
        List<ReleaseBoardVO.DangerDistribution> dangerDistribution = new ArrayList<>();
        for (String dl : dangerOrder) {
            if (dangerDistMap.containsKey(dl)) {
                dangerDistribution.add(new ReleaseBoardVO.DangerDistribution(dl, dangerDistMap.get(dl)));
            }
        }
        if (dangerDistMap.containsKey("UNKNOWN")) {
            dangerDistribution.add(new ReleaseBoardVO.DangerDistribution("UNKNOWN", dangerDistMap.get("UNKNOWN")));
        }

        ReleaseBoardVO.BoardData boardData = new ReleaseBoardVO.BoardData();
        boardData.setStats(stats);
        boardData.setAreaDistribution(areaDistribution);
        boardData.setDangerDistribution(dangerDistribution);
        boardData.setPrisoners(boardPrisoners);

        log.info("临释看板查询: maxDays={}, status={}, dangerLevel={}, areaId={}, 匹配人数={}",
                maxDays, status, dangerLevel, areaId, total);

        return boardData;
    }

    private String calcWarningLevel(long remainingDays) {
        if (remainingDays <= 7) return "EXTREME";
        if (remainingDays <= 15) return "HIGH";
        if (remainingDays <= 30) return "MEDIUM";
        return "LOW";
    }
}
