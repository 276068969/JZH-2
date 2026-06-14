package com.prison.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.prison.dto.ObservationListQueryDTO;
import com.prison.entity.Cell;
import com.prison.entity.Incident;
import com.prison.entity.MedicalRecord;
import com.prison.entity.PrisonArea;
import com.prison.entity.Prisoner;
import com.prison.enums.SeverityLevel;
import com.prison.mapper.CellMapper;
import com.prison.mapper.IncidentMapper;
import com.prison.mapper.MedicalRecordMapper;
import com.prison.mapper.PrisonAreaMapper;
import com.prison.mapper.PrisonerMapper;
import com.prison.service.ObservationListService;
import com.prison.vo.ObservationListVO;
import com.prison.vo.ObservationStatsVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Period;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ObservationListServiceImpl extends ServiceImpl<PrisonerMapper, Prisoner> implements ObservationListService {

    private final IncidentMapper incidentMapper;
    private final MedicalRecordMapper medicalRecordMapper;
    private final PrisonAreaMapper prisonAreaMapper;
    private final CellMapper cellMapper;

    private static final Set<String> HIGH_DANGER_LEVELS = Set.of("HIGH", "EXTREME");
    private static final Set<String> ACTIVE_PRISONER_STATUSES = Set.of("ACTIVE", "SERVING", "INCARCERATED");

    @Override
    public Page<ObservationListVO> pageObservationList(ObservationListQueryDTO queryDTO) {
        int incidentDaysThreshold = queryDTO.getIncidentDaysThreshold() != null
                ? queryDTO.getIncidentDaysThreshold() : 30;

        Set<Long> highDangerPrisonerIds = collectHighDangerPrisoners();
        Set<Long> recentIncidentPrisonerIds = collectRecentIncidentPrisoners(incidentDaysThreshold);
        Set<Long> ongoingTreatmentPrisonerIds = collectOngoingTreatmentPrisoners();

        Set<Long> allObservedIds = new HashSet<>();
        allObservedIds.addAll(highDangerPrisonerIds);
        allObservedIds.addAll(recentIncidentPrisonerIds);
        allObservedIds.addAll(ongoingTreatmentPrisonerIds);

        if (allObservedIds.isEmpty()) {
            return new Page<>(queryDTO.getPage(), queryDTO.getSize());
        }

        if (StringUtils.hasText(queryDTO.getRiskCategory())) {
            switch (queryDTO.getRiskCategory()) {
                case "DANGER_LEVEL" -> allObservedIds.retainAll(highDangerPrisonerIds);
                case "RECENT_INCIDENT" -> allObservedIds.retainAll(recentIncidentPrisonerIds);
                case "ONGOING_TREATMENT" -> allObservedIds.retainAll(ongoingTreatmentPrisonerIds);
                default -> {
                }
            }
            if (allObservedIds.isEmpty()) {
                return new Page<>(queryDTO.getPage(), queryDTO.getSize());
            }
        }

        LambdaQueryWrapper<Prisoner> prisonerWrapper = new LambdaQueryWrapper<>();
        prisonerWrapper.in(Prisoner::getId, allObservedIds);

        if (StringUtils.hasText(queryDTO.getKeyword())) {
            String keyword = "%" + queryDTO.getKeyword() + "%";
            prisonerWrapper.and(w -> w
                    .like(Prisoner::getName, keyword)
                    .or()
                    .like(Prisoner::getPrisonerNumber, keyword)
            );
        }
        if (queryDTO.getAreaId() != null) {
            prisonerWrapper.eq(Prisoner::getAreaId, queryDTO.getAreaId());
        }
        if (queryDTO.getCellId() != null) {
            prisonerWrapper.eq(Prisoner::getCellId, queryDTO.getCellId());
        }
        if (StringUtils.hasText(queryDTO.getDangerLevel())) {
            prisonerWrapper.eq(Prisoner::getDangerLevel, queryDTO.getDangerLevel());
        }
        if (StringUtils.hasText(queryDTO.getPrisonerStatus())) {
            prisonerWrapper.eq(Prisoner::getStatus, queryDTO.getPrisonerStatus());
        }

        List<Prisoner> prisoners = list(prisonerWrapper);
        if (prisoners.isEmpty()) {
            return new Page<>(queryDTO.getPage(), queryDTO.getSize());
        }

        Set<Long> prisonerIdSet = prisoners.stream().map(Prisoner::getId).collect(Collectors.toSet());

        Map<Long, List<Incident>> prisonerIncidentsMap = getPrisonerIncidentsMap(prisonerIdSet);
        Map<Long, List<MedicalRecord>> prisonerMedicalMap = getPrisonerMedicalMap(prisonerIdSet);

        Set<Long> areaIdSet = prisoners.stream()
                .map(Prisoner::getAreaId).filter(Objects::nonNull).collect(Collectors.toSet());
        Map<Long, PrisonArea> areaMap = areaIdSet.isEmpty() ? Collections.emptyMap() :
                prisonAreaMapper.selectBatchIds(areaIdSet)
                        .stream().collect(Collectors.toMap(PrisonArea::getId, a -> a));

        Set<Long> cellIdSet = prisoners.stream()
                .map(Prisoner::getCellId).filter(Objects::nonNull).collect(Collectors.toSet());
        Map<Long, Cell> cellMap = cellIdSet.isEmpty() ? Collections.emptyMap() :
                cellMapper.selectBatchIds(cellIdSet)
                        .stream().collect(Collectors.toMap(Cell::getId, c -> c));

        List<ObservationListVO> voList = new ArrayList<>();
        for (Prisoner prisoner : prisoners) {
            ObservationListVO vo = convertToVO(
                    prisoner,
                    highDangerPrisonerIds,
                    recentIncidentPrisonerIds,
                    ongoingTreatmentPrisonerIds,
                    prisonerIncidentsMap.getOrDefault(prisoner.getId(), Collections.emptyList()),
                    prisonerMedicalMap.getOrDefault(prisoner.getId(), Collections.emptyList()),
                    areaMap,
                    cellMap,
                    incidentDaysThreshold
            );
            voList.add(vo);
        }

        sortVOList(voList, queryDTO.getSortField(), queryDTO.getSortOrder());

        int page = Math.max(1, queryDTO.getPage());
        int size = Math.max(1, queryDTO.getSize());
        int fromIndex = (page - 1) * size;
        int toIndex = Math.min(fromIndex + size, voList.size());

        Page<ObservationListVO> resultPage = new Page<>(page, size);
        if (fromIndex >= voList.size()) {
            resultPage.setRecords(Collections.emptyList());
        } else {
            resultPage.setRecords(voList.subList(fromIndex, toIndex));
        }
        resultPage.setTotal(voList.size());
        return resultPage;
    }

    @Override
    public ObservationStatsVO getObservationStats(Integer incidentDaysThreshold) {
        int days = incidentDaysThreshold != null ? incidentDaysThreshold : 30;

        Set<Long> highDangerPrisonerIds = collectHighDangerPrisoners();
        Set<Long> recentIncidentPrisonerIds = collectRecentIncidentPrisoners(days);
        Set<Long> ongoingTreatmentPrisonerIds = collectOngoingTreatmentPrisoners();

        Set<Long> allObservedIds = new HashSet<>();
        allObservedIds.addAll(highDangerPrisonerIds);
        allObservedIds.addAll(recentIncidentPrisonerIds);
        allObservedIds.addAll(ongoingTreatmentPrisonerIds);

        Set<Long> multipleRiskIds = new HashSet<>();
        for (Long id : allObservedIds) {
            int count = 0;
            if (highDangerPrisonerIds.contains(id)) count++;
            if (recentIncidentPrisonerIds.contains(id)) count++;
            if (ongoingTreatmentPrisonerIds.contains(id)) count++;
            if (count >= 2) multipleRiskIds.add(id);
        }

        ObservationStatsVO stats = new ObservationStatsVO();
        stats.setTotalObserved((long) allObservedIds.size());
        stats.setByDangerLevelCount((long) highDangerPrisonerIds.size());
        stats.setByRecentIncidentCount((long) recentIncidentPrisonerIds.size());
        stats.setByOngoingTreatmentCount((long) ongoingTreatmentPrisonerIds.size());
        stats.setMultipleRiskCount((long) multipleRiskIds.size());

        if (!allObservedIds.isEmpty()) {
            List<Prisoner> prisoners = listByIds(allObservedIds);

            long extremeCount = prisoners.stream()
                    .filter(p -> "EXTREME".equalsIgnoreCase(p.getDangerLevel())).count();
            long highCount = prisoners.stream()
                    .filter(p -> "HIGH".equalsIgnoreCase(p.getDangerLevel())).count();
            stats.setExtremeDangerCount(extremeCount);
            stats.setHighDangerCount(highCount);

            LocalDateTime thresholdTime = LocalDateTime.now().minusDays(days);
            long unresolvedHigh = incidentMapper.selectCount(
                    new LambdaQueryWrapper<Incident>()
                            .in(Incident::getRelatedPrisonerId, allObservedIds)
                            .and(w -> w.in(Incident::getSeverity, "HIGH", "CRITICAL")
                                    .or().in(Incident::getSeverity, "高", "严重"))
                            .notIn(Incident::getStatus, "CLOSED", "RESOLVED", "已关闭", "已解决")
            );
            stats.setUnresolvedHighIncidentTotal(unresolvedHigh);

            long ongoingTreat = medicalRecordMapper.selectCount(
                    new LambdaQueryWrapper<MedicalRecord>()
                            .in(MedicalRecord::getPrisonerId, allObservedIds)
                            .eq(MedicalRecord::getResult, "TREATING")
            );
            stats.setOngoingTreatmentTotal(ongoingTreat);

            Map<String, Long> byArea = prisoners.stream()
                    .filter(p -> p.getAreaId() != null)
                    .map(p -> {
                        PrisonArea area = prisonAreaMapper.selectById(p.getAreaId());
                        return area != null ? area.getAreaName() : "未知监区";
                    })
                    .collect(Collectors.groupingBy(n -> n, Collectors.counting()));
            stats.setByAreaDistribution(byArea);

            Map<String, Long> byCrime = prisoners.stream()
                    .filter(p -> StringUtils.hasText(p.getCrimeType()))
                    .collect(Collectors.groupingBy(Prisoner::getCrimeType, Collectors.counting()));
            stats.setByCrimeTypeDistribution(byCrime);
        } else {
            stats.setExtremeDangerCount(0L);
            stats.setHighDangerCount(0L);
            stats.setUnresolvedHighIncidentTotal(0L);
            stats.setOngoingTreatmentTotal(0L);
            stats.setByAreaDistribution(Collections.emptyMap());
            stats.setByCrimeTypeDistribution(Collections.emptyMap());
        }

        return stats;
    }

    private Set<Long> collectHighDangerPrisoners() {
        LambdaQueryWrapper<Prisoner> wrapper = new LambdaQueryWrapper<>();
        wrapper.and(w -> w
                .in(Prisoner::getDangerLevel, HIGH_DANGER_LEVELS)
                .or()
                .in(Prisoner::getDangerLevel, "高", "极高")
        );
        List<Prisoner> list = list(wrapper);
        return list.stream().map(Prisoner::getId).collect(Collectors.toSet());
    }

    private Set<Long> collectRecentIncidentPrisoners(int daysThreshold) {
        LocalDateTime thresholdTime = LocalDateTime.now().minusDays(daysThreshold);
        LambdaQueryWrapper<Incident> wrapper = new LambdaQueryWrapper<>();
        wrapper.ge(Incident::getOccurTime, thresholdTime)
                .isNotNull(Incident::getRelatedPrisonerId);
        List<Incident> list = incidentMapper.selectList(wrapper);
        return list.stream()
                .map(Incident::getRelatedPrisonerId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
    }

    private Set<Long> collectOngoingTreatmentPrisoners() {
        LambdaQueryWrapper<MedicalRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(MedicalRecord::getResult, "TREATING")
                .or()
                .like(MedicalRecord::getTreatment, "持续")
                .or()
                .like(MedicalRecord::getTreatment, "长期")
                .or()
                .and(w -> w.in(MedicalRecord::getFollowUpStatus, "PENDING", "MISSED")
                        .isNotNull(MedicalRecord::getFollowUpDate));
        List<MedicalRecord> list = medicalRecordMapper.selectList(wrapper);
        return list.stream()
                .map(MedicalRecord::getPrisonerId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
    }

    private Map<Long, List<Incident>> getPrisonerIncidentsMap(Set<Long> prisonerIdSet) {
        if (prisonerIdSet.isEmpty()) return Collections.emptyMap();
        List<Incident> incidents = incidentMapper.selectList(
                new LambdaQueryWrapper<Incident>()
                        .in(Incident::getRelatedPrisonerId, prisonerIdSet)
                        .orderByDesc(Incident::getOccurTime)
        );
        return incidents.stream()
                .filter(i -> i.getRelatedPrisonerId() != null)
                .collect(Collectors.groupingBy(Incident::getRelatedPrisonerId));
    }

    private Map<Long, List<MedicalRecord>> getPrisonerMedicalMap(Set<Long> prisonerIdSet) {
        if (prisonerIdSet.isEmpty()) return Collections.emptyMap();
        List<MedicalRecord> records = medicalRecordMapper.selectList(
                new LambdaQueryWrapper<MedicalRecord>()
                        .in(MedicalRecord::getPrisonerId, prisonerIdSet)
                        .orderByDesc(MedicalRecord::getRecordDate)
        );
        return records.stream()
                .filter(r -> r.getPrisonerId() != null)
                .collect(Collectors.groupingBy(MedicalRecord::getPrisonerId));
    }

    private ObservationListVO convertToVO(Prisoner prisoner,
                                          Set<Long> highDangerIds,
                                          Set<Long> recentIncidentIds,
                                          Set<Long> ongoingTreatmentIds,
                                          List<Incident> incidents,
                                          List<MedicalRecord> medicalRecords,
                                          Map<Long, PrisonArea> areaMap,
                                          Map<Long, Cell> cellMap,
                                          int incidentDaysThreshold) {
        ObservationListVO vo = new ObservationListVO();
        vo.setPrisonerId(prisoner.getId());
        vo.setPrisonerNumber(prisoner.getPrisonerNumber());
        vo.setPrisonerName(prisoner.getName());
        vo.setGender(prisoner.getGender());
        vo.setCrimeType(prisoner.getCrimeType());
        vo.setPrisonerStatus(prisoner.getStatus());
        vo.setEntryDate(prisoner.getEntryDate());
        vo.setReleaseDate(prisoner.getReleaseDate());
        vo.setAreaId(prisoner.getAreaId());
        vo.setCellId(prisoner.getCellId());
        vo.setDangerLevel(prisoner.getDangerLevel());

        if (prisoner.getBirthDate() != null) {
            vo.setAge(Period.between(prisoner.getBirthDate(), LocalDate.now()).getYears());
        }

        if (prisoner.getAreaId() != null) {
            PrisonArea area = areaMap.get(prisoner.getAreaId());
            if (area != null) vo.setAreaName(area.getAreaName());
        }
        if (prisoner.getCellId() != null) {
            Cell cell = cellMap.get(prisoner.getCellId());
            if (cell != null) vo.setCellNumber(cell.getCellNumber());
        }

        Set<String> riskCategories = new LinkedHashSet<>();
        List<String> riskReasons = new ArrayList<>();
        int riskScore = 0;

        boolean isHighDanger = highDangerIds.contains(prisoner.getId());
        boolean hasRecentIncident = recentIncidentIds.contains(prisoner.getId());
        boolean hasOngoingTreatment = ongoingTreatmentIds.contains(prisoner.getId());

        if (isHighDanger) {
            riskCategories.add("DANGER_LEVEL");
            String level = prisoner.getDangerLevel();
            if ("EXTREME".equalsIgnoreCase(level) || "极高".equals(level)) {
                riskReasons.add("极高危险等级");
                riskScore += 50;
            } else {
                riskReasons.add("高危险等级");
                riskScore += 30;
            }
        }

        LocalDateTime thresholdTime = LocalDateTime.now().minusDays(incidentDaysThreshold);
        List<Incident> recentIncidents = incidents.stream()
                .filter(i -> i.getOccurTime() != null && i.getOccurTime().isAfter(thresholdTime))
                .collect(Collectors.toList());
        vo.setRecentIncidentCount(recentIncidents.size());

        long unresolvedHighCount = incidents.stream()
                .filter(i -> {
                    SeverityLevel sl = SeverityLevel.fromString(i.getSeverity());
                    boolean isHighOrAbove = sl != null && sl.isHighOrAbove();
                    boolean isHighSeverity = isHighOrAbove
                            || "高".equals(i.getSeverity()) || "严重".equals(i.getSeverity());
                    boolean isUnresolved = !"CLOSED".equalsIgnoreCase(i.getStatus())
                            && !"RESOLVED".equalsIgnoreCase(i.getStatus())
                            && !"已关闭".equals(i.getStatus())
                            && !"已解决".equals(i.getStatus());
                    return isHighSeverity && isUnresolved;
                })
                .count();
        vo.setUnresolvedHighIncidentCount((int) unresolvedHighCount);

        if (hasRecentIncident) {
            riskCategories.add("RECENT_INCIDENT");
            if (unresolvedHighCount > 0) {
                riskReasons.add("近" + incidentDaysThreshold + "天有" + unresolvedHighCount + "起高严重级未解决事件");
                riskScore += (int) unresolvedHighCount * 25;
            } else if (!recentIncidents.isEmpty()) {
                riskReasons.add("近" + incidentDaysThreshold + "天有" + recentIncidents.size() + "起事件记录");
                riskScore += recentIncidents.size() * 10;
            }
        }

        if (!incidents.isEmpty()) {
            Incident last = incidents.get(0);
            vo.setLastIncidentTime(last.getOccurTime());
            vo.setLastIncidentType(last.getIncidentType());
            vo.setLastIncidentSeverity(last.getSeverity());
        }

        long ongoingCount = medicalRecords.stream()
                .filter(r -> "TREATING".equals(r.getResult())
                        || (r.getTreatment() != null
                        && (r.getTreatment().contains("持续") || r.getTreatment().contains("长期"))))
                .count();
        vo.setOngoingTreatmentCount((int) ongoingCount);

        if (!medicalRecords.isEmpty()) {
            MedicalRecord last = medicalRecords.get(0);
            vo.setLastMedicalDate(last.getRecordDate());
            vo.setLastDiagnosis(last.getDiagnosis());
            vo.setLastTreatmentStatus(last.getResult());
        }

        if (hasOngoingTreatment) {
            riskCategories.add("ONGOING_TREATMENT");
            if (ongoingCount > 0) {
                riskReasons.add("存在" + ongoingCount + "项持续治疗状态");
                riskScore += (int) ongoingCount * 15;
            } else {
                riskReasons.add("存在复诊待办或逾期");
                riskScore += 10;
            }
        }

        int categoryCount = riskCategories.size();
        if (categoryCount >= 3) {
            riskScore += 30;
            riskReasons.add("三重风险叠加");
        } else if (categoryCount == 2) {
            riskScore += 15;
            riskReasons.add("双重风险叠加");
        }

        vo.setRiskCategories(riskCategories);
        vo.setRiskReasons(riskReasons);
        vo.setRiskScore(riskScore);

        return vo;
    }

    private void sortVOList(List<ObservationListVO> voList, String sortField, String sortOrder) {
        boolean desc = !"asc".equalsIgnoreCase(sortOrder);
        Comparator<ObservationListVO> comparator;
        switch (sortField == null ? "" : sortField) {
            case "dangerLevel" -> comparator = Comparator.comparingInt(vo -> {
                String dl = vo.getDangerLevel();
                if ("EXTREME".equalsIgnoreCase(dl) || "极高".equals(dl)) return 4;
                if ("HIGH".equalsIgnoreCase(dl) || "高".equals(dl)) return 3;
                if ("MEDIUM".equalsIgnoreCase(dl) || "中".equals(dl)) return 2;
                return 1;
            });
            case "recentIncidentCount" ->
                    comparator = Comparator.comparingInt(v -> v.getRecentIncidentCount() == null ? 0 : v.getRecentIncidentCount());
            case "unresolvedHighIncidentCount" ->
                    comparator = Comparator.comparingInt(v -> v.getUnresolvedHighIncidentCount() == null ? 0 : v.getUnresolvedHighIncidentCount());
            case "ongoingTreatmentCount" ->
                    comparator = Comparator.comparingInt(v -> v.getOngoingTreatmentCount() == null ? 0 : v.getOngoingTreatmentCount());
            case "lastIncidentTime" ->
                    comparator = Comparator.comparing(v -> v.getLastIncidentTime() == null ? LocalDateTime.MIN : v.getLastIncidentTime());
            case "riskScore", default ->
                    comparator = Comparator.comparingInt(v -> v.getRiskScore() == null ? 0 : v.getRiskScore());
        }
        if (desc) {
            comparator = comparator.reversed();
        }
        voList.sort(comparator);
    }
}
