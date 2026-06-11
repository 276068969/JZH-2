package com.prison.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.prison.dto.FollowUpMarkDTO;
import com.prison.dto.FollowUpQueryDTO;
import com.prison.entity.Cell;
import com.prison.entity.MedicalRecord;
import com.prison.entity.PrisonArea;
import com.prison.entity.Prisoner;
import com.prison.mapper.CellMapper;
import com.prison.mapper.MedicalRecordMapper;
import com.prison.mapper.PrisonAreaMapper;
import com.prison.mapper.PrisonerMapper;
import com.prison.service.FollowUpService;
import com.prison.vo.FollowUpStatsVO;
import com.prison.vo.FollowUpWorkbenchVO;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class FollowUpServiceImpl extends ServiceImpl<MedicalRecordMapper, MedicalRecord> implements FollowUpService {

    private final PrisonerMapper prisonerMapper;
    private final PrisonAreaMapper prisonAreaMapper;
    private final CellMapper cellMapper;

    private static final Set<String> ACTIVE_FOLLOW_UP_STATUSES = Set.of("PENDING", "MISSED");

    @Override
    public Page<FollowUpWorkbenchVO> pageFollowUpWorkbench(FollowUpQueryDTO queryDTO) {
        LocalDate today = LocalDate.now();

        LambdaQueryWrapper<MedicalRecord> recordWrapper = new LambdaQueryWrapper<>();
        recordWrapper.isNotNull(MedicalRecord::getFollowUpDate);

        if (StringUtils.hasText(queryDTO.getKeyword())) {
            String keyword = "%" + queryDTO.getKeyword() + "%";
            List<Long> matchedPrisonerIds = prisonerMapper.selectList(
                    new LambdaQueryWrapper<Prisoner>()
                            .like(Prisoner::getName, keyword)
                            .or()
                            .like(Prisoner::getPrisonerNumber, keyword)
            ).stream().map(Prisoner::getId).collect(Collectors.toList());

            recordWrapper.and(w -> w
                    .like(MedicalRecord::getDiagnosis, keyword)
                    .or()
                    .like(MedicalRecord::getDoctorName, keyword)
                    .or(!matchedPrisonerIds.isEmpty(), w2 -> w2.in(MedicalRecord::getPrisonerId, matchedPrisonerIds))
            );
        }

        if (StringUtils.hasText(queryDTO.getFollowUpStatus())) {
            if ("OVERDUE".equals(queryDTO.getFollowUpStatus())) {
                recordWrapper.and(w -> w
                        .in(MedicalRecord::getFollowUpStatus, "PENDING", "MISSED")
                        .lt(MedicalRecord::getFollowUpDate, today)
                );
            } else {
                recordWrapper.eq(MedicalRecord::getFollowUpStatus, queryDTO.getFollowUpStatus());
            }
        }

        if (queryDTO.getFollowUpStartDate() != null) {
            recordWrapper.ge(MedicalRecord::getFollowUpDate, queryDTO.getFollowUpStartDate());
        }
        if (queryDTO.getFollowUpEndDate() != null) {
            recordWrapper.le(MedicalRecord::getFollowUpDate, queryDTO.getFollowUpEndDate());
        }

        List<Long> filterPrisonerIds = null;
        if (queryDTO.getAreaId() != null || StringUtils.hasText(queryDTO.getDangerLevel())
                || StringUtils.hasText(queryDTO.getPrisonerStatus())) {
            LambdaQueryWrapper<Prisoner> pWrapper = new LambdaQueryWrapper<>();
            if (queryDTO.getAreaId() != null) {
                pWrapper.eq(Prisoner::getAreaId, queryDTO.getAreaId());
            }
            if (StringUtils.hasText(queryDTO.getDangerLevel())) {
                pWrapper.eq(Prisoner::getDangerLevel, queryDTO.getDangerLevel());
            }
            if (StringUtils.hasText(queryDTO.getPrisonerStatus())) {
                pWrapper.eq(Prisoner::getStatus, queryDTO.getPrisonerStatus());
            }
            filterPrisonerIds = prisonerMapper.selectList(pWrapper)
                    .stream().map(Prisoner::getId).collect(Collectors.toList());
            if (filterPrisonerIds.isEmpty()) {
                return new Page<>(queryDTO.getPage(), queryDTO.getSize());
            }
            recordWrapper.in(MedicalRecord::getPrisonerId, filterPrisonerIds);
        }

        recordWrapper.orderByAsc(MedicalRecord::getFollowUpDate);

        Page<MedicalRecord> recordPage = page(
                new Page<>(1, Integer.MAX_VALUE),
                recordWrapper
        );

        List<MedicalRecord> records = recordPage.getRecords();
        if (records.isEmpty()) {
            return new Page<>(queryDTO.getPage(), queryDTO.getSize());
        }

        Set<Long> prisonerIdSet = records.stream().map(MedicalRecord::getPrisonerId).collect(Collectors.toSet());
        Map<Long, Prisoner> prisonerMap = prisonerMapper.selectBatchIds(prisonerIdSet)
                .stream().collect(Collectors.toMap(Prisoner::getId, p -> p));

        Set<Long> areaIdSet = prisonerMap.values().stream()
                .map(Prisoner::getAreaId).filter(Objects::nonNull).collect(Collectors.toSet());
        Map<Long, PrisonArea> areaMap = areaIdSet.isEmpty() ? Collections.emptyMap() :
                prisonAreaMapper.selectBatchIds(areaIdSet)
                        .stream().collect(Collectors.toMap(PrisonArea::getId, a -> a));

        Set<Long> cellIdSet = prisonerMap.values().stream()
                .map(Prisoner::getCellId).filter(Objects::nonNull).collect(Collectors.toSet());
        Map<Long, Cell> cellMap = cellIdSet.isEmpty() ? Collections.emptyMap() :
                cellMapper.selectBatchIds(cellIdSet)
                        .stream().collect(Collectors.toMap(Cell::getId, c -> c));

        Map<Long, Long> missedCountMap = calculateMissedCountPerPrisoner(prisonerIdSet);

        List<FollowUpWorkbenchVO> voList = new ArrayList<>();
        for (MedicalRecord record : records) {
            FollowUpWorkbenchVO vo = convertToVO(record, prisonerMap, areaMap, cellMap, missedCountMap, today);
            if (Boolean.TRUE.equals(queryDTO.getOnlyKeyAttention()) && !Boolean.TRUE.equals(vo.getIsKeyAttention())) {
                continue;
            }
            if (StringUtils.hasText(queryDTO.getActiveFilter()) && !matchesActiveFilter(vo, queryDTO.getActiveFilter(), today)) {
                continue;
            }
            voList.add(vo);
        }

        int page = Math.max(1, queryDTO.getPage());
        int size = Math.max(1, queryDTO.getSize());
        int fromIndex = (page - 1) * size;
        int toIndex = Math.min(fromIndex + size, voList.size());

        Page<FollowUpWorkbenchVO> resultPage = new Page<>(page, size);
        if (fromIndex >= voList.size()) {
            resultPage.setRecords(Collections.emptyList());
        } else {
            resultPage.setRecords(voList.subList(fromIndex, toIndex));
        }
        resultPage.setTotal(voList.size());
        return resultPage;
    }

    @Override
    public FollowUpStatsVO getFollowUpStats() {
        LocalDate today = LocalDate.now();
        LocalDate weekEnd = today.plusDays(7);
        LocalDate monthEnd = today.plusDays(30);

        List<MedicalRecord> allFollowUps = list(
                new LambdaQueryWrapper<MedicalRecord>()
                        .isNotNull(MedicalRecord::getFollowUpDate)
        );

        FollowUpStatsVO stats = new FollowUpStatsVO();

        long todayPending = 0;
        long weekPending = 0;
        long monthPending = 0;
        long overdue = 0;
        long completed = 0;
        Set<Long> consecutiveMissedPrisoners = new HashSet<>();
        Set<Long> stillTreatingPrisoners = new HashSet<>();
        Map<Long, Integer> prisonerMissedCount = new HashMap<>();

        for (MedicalRecord r : allFollowUps) {
            LocalDate fuDate = r.getFollowUpDate();
            String status = r.getFollowUpStatus();
            boolean isPending = !StringUtils.hasText(status) || "PENDING".equals(status);

            if ("COMPLETED".equals(status)) {
                completed++;
            }

            long daysUntil = ChronoUnit.DAYS.between(today, fuDate);

            if (isPending || "MISSED".equals(status)) {
                if (daysUntil < 0) {
                    overdue++;
                } else if (daysUntil == 0) {
                    todayPending++;
                } else if (daysUntil <= 7) {
                    weekPending++;
                } else if (daysUntil <= 30) {
                    monthPending++;
                }
            }

            if ("MISSED".equals(status)) {
                prisonerMissedCount.merge(r.getPrisonerId(), 1, Integer::sum);
            }

            if ("TREATING".equals(r.getResult())) {
                stillTreatingPrisoners.add(r.getPrisonerId());
            }
        }

        for (Map.Entry<Long, Integer> entry : prisonerMissedCount.entrySet()) {
            if (entry.getValue() >= 2) {
                consecutiveMissedPrisoners.add(entry.getKey());
            }
        }

        Set<Long> keyAttentionSet = new HashSet<>();
        keyAttentionSet.addAll(consecutiveMissedPrisoners);

        for (MedicalRecord r : allFollowUps) {
            if ("TREATING".equals(r.getResult()) && r.getFollowUpDate() != null) {
                LocalDate fuDate = r.getFollowUpDate();
                String status = r.getFollowUpStatus();
                if ((!StringUtils.hasText(status) || "PENDING".equals(status) || "MISSED".equals(status))
                        && ChronoUnit.DAYS.between(today, fuDate) < 0) {
                    keyAttentionSet.add(r.getPrisonerId());
                }
            }
        }

        stats.setTodayPending(todayPending);
        stats.setWeekPending(weekPending);
        stats.setMonthPending(monthPending);
        stats.setOverdue(overdue);
        stats.setCompleted(completed);
        stats.setTotalWithFollowUp((long) allFollowUps.size());
        stats.setConsecutiveMissed((long) consecutiveMissedPrisoners.size());
        stats.setStillTreating((long) stillTreatingPrisoners.size());
        stats.setKeyAttention((long) keyAttentionSet.size());

        return stats;
    }

    @Override
    @Transactional
    public void markFollowUp(FollowUpMarkDTO dto) {
        MedicalRecord record = getById(dto.getMedicalRecordId());
        if (record == null) {
            throw new IllegalArgumentException("医疗记录不存在");
        }

        record.setFollowUpStatus(dto.getFollowUpStatus());

        if ("COMPLETED".equals(dto.getFollowUpStatus())) {
            if (dto.getActualFollowUpDate() != null) {
                record.setActualFollowUpDate(dto.getActualFollowUpDate());
            } else {
                record.setActualFollowUpDate(LocalDate.now());
            }
            if (StringUtils.hasText(dto.getFollowUpResult())) {
                record.setFollowUpResult(dto.getFollowUpResult());
            }
            if (StringUtils.hasText(dto.getFollowUpRemark())) {
                record.setFollowUpRemark(dto.getFollowUpRemark());
            }
        } else if ("MISSED".equals(dto.getFollowUpStatus())) {
            if (StringUtils.hasText(dto.getFollowUpRemark())) {
                record.setFollowUpRemark(dto.getFollowUpRemark());
            }
        } else if ("CANCELLED".equals(dto.getFollowUpStatus())) {
            if (StringUtils.hasText(dto.getFollowUpRemark())) {
                record.setFollowUpRemark(dto.getFollowUpRemark());
            }
        }

        updateById(record);

        if ("COMPLETED".equals(dto.getFollowUpStatus()) && dto.getNextFollowUpDate() != null) {
            MedicalRecord nextRecord = new MedicalRecord();
            BeanUtils.copyProperties(record, nextRecord);
            nextRecord.setId(null);
            nextRecord.setRecordDate(record.getActualFollowUpDate());
            nextRecord.setFollowUpDate(dto.getNextFollowUpDate());
            nextRecord.setFollowUpStatus("PENDING");
            nextRecord.setActualFollowUpDate(null);
            nextRecord.setFollowUpResult(null);
            nextRecord.setFollowUpRemark(null);
            if (StringUtils.hasText(dto.getFollowUpResult())) {
                nextRecord.setTreatment(dto.getFollowUpResult());
            }
            save(nextRecord);
        }
    }

    private Map<Long, Long> calculateMissedCountPerPrisoner(Set<Long> prisonerIdSet) {
        if (prisonerIdSet.isEmpty()) return Collections.emptyMap();
        List<MedicalRecord> missedRecords = list(
                new LambdaQueryWrapper<MedicalRecord>()
                        .in(MedicalRecord::getPrisonerId, prisonerIdSet)
                        .eq(MedicalRecord::getFollowUpStatus, "MISSED")
        );
        return missedRecords.stream()
                .collect(Collectors.groupingBy(MedicalRecord::getPrisonerId, Collectors.counting()));
    }

    private boolean matchesActiveFilter(FollowUpWorkbenchVO vo, String filter, LocalDate today) {
        if (vo.getFollowUpDate() == null) return false;
        long days = ChronoUnit.DAYS.between(today, vo.getFollowUpDate());
        String status = vo.getFollowUpStatus();
        boolean isPending = !StringUtils.hasText(status) || "PENDING".equals(status);

        return switch (filter) {
            case "TODAY" -> isPending && days == 0;
            case "WEEK" -> isPending && days > 0 && days <= 7;
            case "MONTH" -> isPending && days > 7 && days <= 30;
            case "OVERDUE" -> "OVERDUE".equals(status) || "MISSED".equals(status)
                    || (isPending && days < 0);
            case "MISSED" -> Boolean.TRUE.equals(vo.getIsKeyAttention())
                    && vo.getMissedFollowUpCount() != null && vo.getMissedFollowUpCount() >= 2;
            case "TREATING" -> "TREATING".equals(vo.getResult());
            case "KEY" -> Boolean.TRUE.equals(vo.getIsKeyAttention());
            case "DONE" -> "COMPLETED".equals(status);
            default -> true;
        };
    }

    private FollowUpWorkbenchVO convertToVO(MedicalRecord record,
                                            Map<Long, Prisoner> prisonerMap,
                                            Map<Long, PrisonArea> areaMap,
                                            Map<Long, Cell> cellMap,
                                            Map<Long, Long> missedCountMap,
                                            LocalDate today) {
        FollowUpWorkbenchVO vo = new FollowUpWorkbenchVO();
        vo.setId(record.getId());

        BeanUtils.copyProperties(record, vo);

        Prisoner prisoner = prisonerMap.get(record.getPrisonerId());
        if (prisoner != null) {
            vo.setPrisonerId(prisoner.getId());
            vo.setPrisonerNumber(prisoner.getPrisonerNumber());
            vo.setPrisonerName(prisoner.getName());
            vo.setGender(prisoner.getGender());
            vo.setAreaId(prisoner.getAreaId());
            vo.setCellId(prisoner.getCellId());
            vo.setDangerLevel(prisoner.getDangerLevel());
            vo.setPrisonerStatus(prisoner.getStatus());

            if (prisoner.getAreaId() != null) {
                PrisonArea area = areaMap.get(prisoner.getAreaId());
                if (area != null) {
                    vo.setAreaName(area.getAreaName());
                }
            }
            if (prisoner.getCellId() != null) {
                Cell cell = cellMap.get(prisoner.getCellId());
                if (cell != null) {
                    vo.setCellNumber(cell.getCellNumber());
                }
            }
        }

        long missed = missedCountMap.getOrDefault(record.getPrisonerId(), 0L);
        vo.setMissedFollowUpCount((int) missed);

        String status = record.getFollowUpStatus();
        boolean isPending = !StringUtils.hasText(status) || "PENDING".equals(status);
        LocalDate fuDate = record.getFollowUpDate();

        if (fuDate != null) {
            long days = ChronoUnit.DAYS.between(today, fuDate);
            if (days >= 0) {
                vo.setDaysUntilFollowUp(days);
                vo.setDaysOverdue(0L);
            } else {
                vo.setDaysUntilFollowUp(0L);
                vo.setDaysOverdue(Math.abs(days));
                if (isPending) {
                    vo.setFollowUpStatus("OVERDUE");
                }
            }
        }

        List<String> reasons = new ArrayList<>();
        if (missed >= 2) {
            reasons.add("连续" + missed + "次未复诊");
        }
        if ("TREATING".equals(record.getResult())) {
            reasons.add("治疗未结束");
            if (fuDate != null && (isPending || "MISSED".equals(status))
                    && ChronoUnit.DAYS.between(today, fuDate) < 0) {
                reasons.add("治疗中且逾期未复诊");
            }
        }
        if ("HIGH".equals(vo.getDangerLevel()) || "EXTREME".equals(vo.getDangerLevel())) {
            if (!reasons.isEmpty()) {
                reasons.add("高危人员");
            }
        }

        if (!reasons.isEmpty()) {
            vo.setIsKeyAttention(true);
            vo.setKeyAttentionReason(String.join("、", reasons));
        } else {
            vo.setIsKeyAttention(false);
        }

        return vo;
    }
}
