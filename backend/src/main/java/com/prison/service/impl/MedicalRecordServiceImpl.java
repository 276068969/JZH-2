package com.prison.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.prison.config.BusinessException;
import com.prison.entity.Cell;
import com.prison.entity.MedicalRecord;
import com.prison.entity.PrisonArea;
import com.prison.entity.Prisoner;
import com.prison.enums.SysLogAction;
import com.prison.enums.SysLogModule;
import com.prison.mapper.CellMapper;
import com.prison.mapper.MedicalRecordMapper;
import com.prison.mapper.PrisonAreaMapper;
import com.prison.mapper.PrisonerMapper;
import com.prison.service.MedicalRecordService;
import com.prison.service.PrisonerService;
import com.prison.service.SysLogService;
import com.prison.vo.MedicalTimelineNodeVO;
import com.prison.vo.MedicalTimelineVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.util.*;

@Service
@RequiredArgsConstructor
public class MedicalRecordServiceImpl extends ServiceImpl<MedicalRecordMapper, MedicalRecord> implements MedicalRecordService {

    private final PrisonerMapper prisonerMapper;
    private final PrisonAreaMapper prisonAreaMapper;
    private final CellMapper cellMapper;
    private final PrisonerService prisonerService;
    private final SysLogService sysLogService;

    private static final Map<String, String> MEDICAL_TYPE_LABELS = Map.of(
            "PHYSICAL", "体检",
            "OUTPATIENT", "门诊",
            "EMERGENCY", "急诊",
            "HOSPITALIZATION", "住院",
            "PSYCHOLOGICAL", "心理咨询"
    );

    private static final Map<String, String> MEDICAL_TYPE_COLORS = Map.of(
            "PHYSICAL", "#409EFF",
            "OUTPATIENT", "#67C23A",
            "EMERGENCY", "#F56C6C",
            "HOSPITALIZATION", "#E6A23C",
            "PSYCHOLOGICAL", "#909399"
    );

    private static final Map<String, String> MEDICAL_TYPE_ICONS = Map.of(
            "PHYSICAL", "Stethoscope",
            "OUTPATIENT", "FirstAidKit",
            "EMERGENCY", "WarningFilled",
            "HOSPITALIZATION", "OfficeBuilding",
            "PSYCHOLOGICAL", "ChatDotRound"
    );

    private static final Map<String, String> RESULT_LABELS = Map.of(
            "RECOVERED", "已治愈",
            "TREATING", "治疗中",
            "STABLE", "病情稳定",
            "CURED", "已痊愈",
            "TRANSFERRED", "已转院",
            "DECEASED", "已故"
    );

    private static final Map<String, String> RESULT_TAG_TYPES = Map.of(
            "RECOVERED", "success",
            "TREATING", "warning",
            "STABLE", "",
            "CURED", "success",
            "TRANSFERRED", "info",
            "DECEASED", "danger"
    );

    private static final Map<String, String> FOLLOW_UP_STATUS_LABELS = Map.of(
            "PENDING", "待复诊",
            "COMPLETED", "已复诊",
            "MISSED", "未复诊",
            "CANCELLED", "已取消",
            "OVERDUE", "已逾期"
    );

    private static final Map<String, String> FOLLOW_UP_TAG_TYPES = Map.of(
            "PENDING", "warning",
            "COMPLETED", "success",
            "MISSED", "danger",
            "CANCELLED", "info",
            "OVERDUE", "danger"
    );

    private static String safeGet(Map<String, String> map, String key, String defaultValue) {
        return key != null ? map.getOrDefault(key, defaultValue) : defaultValue;
    }

    @Override
    public Page<MedicalRecord> pageMedicalRecords(int page, int size, String keyword) {
        LambdaQueryWrapper<MedicalRecord> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(keyword)) {
            wrapper.like(MedicalRecord::getDiagnosis, keyword)
                    .or()
                    .like(MedicalRecord::getDoctorName, keyword)
                    .or()
                    .like(MedicalRecord::getMedicalType, keyword);
        }
        wrapper.orderByDesc(MedicalRecord::getRecordDate);
        return page(new Page<>(page, size), wrapper);
    }

    @Override
    public MedicalTimelineVO getTimelineByPrisonerId(Long prisonerId) {
        Prisoner prisoner = prisonerMapper.selectById(prisonerId);
        if (prisoner == null) {
            MedicalTimelineVO empty = new MedicalTimelineVO();
            empty.setPrisonerId(prisonerId);
            empty.setNodes(Collections.emptyList());
            empty.setTotalRecords(0L);
            empty.setTreatingCount(0L);
            empty.setRecoveredCount(0L);
            empty.setFollowUpPendingCount(0L);
            empty.setFollowUpMissedCount(0L);
            return empty;
        }

        List<MedicalRecord> records = listByPrisonerId(prisonerId);

        MedicalTimelineVO timeline = new MedicalTimelineVO();
        timeline.setPrisonerId(prisoner.getId());
        timeline.setPrisonerNumber(prisoner.getPrisonerNumber());
        timeline.setPrisonerName(prisoner.getName());
        timeline.setGender(prisoner.getGender());
        timeline.setBirthDate(prisoner.getBirthDate());
        timeline.setIdCard(prisoner.getIdCard());
        timeline.setNativePlace(prisoner.getNativePlace());
        timeline.setAreaId(prisoner.getAreaId());
        timeline.setCellId(prisoner.getCellId());
        timeline.setDangerLevel(prisoner.getDangerLevel());
        timeline.setHealthStatus(prisoner.getHealthStatus());
        timeline.setPrisonerStatus(prisoner.getStatus());

        if (prisoner.getAreaId() != null) {
            PrisonArea area = prisonAreaMapper.selectById(prisoner.getAreaId());
            if (area != null) {
                timeline.setAreaName(area.getAreaName());
            }
        }
        if (prisoner.getCellId() != null) {
            Cell cell = cellMapper.selectById(prisoner.getCellId());
            if (cell != null) {
                timeline.setCellNumber(cell.getCellNumber());
            }
        }

        timeline.setTotalRecords((long) records.size());
        timeline.setTreatingCount(records.stream().filter(r -> "TREATING".equals(r.getResult())).count());
        timeline.setRecoveredCount(records.stream().filter(r -> "RECOVERED".equals(r.getResult())).count());

        LocalDate today = LocalDate.now();
        long pendingCount = 0;
        long missedCount = 0;
        for (MedicalRecord r : records) {
            if (r.getFollowUpDate() != null) {
                String status = r.getFollowUpStatus();
                boolean isPending = !StringUtils.hasText(status) || "PENDING".equals(status);
                if ("MISSED".equals(status)) {
                    missedCount++;
                } else if (isPending && r.getFollowUpDate().isBefore(today)) {
                    missedCount++;
                } else if (isPending) {
                    pendingCount++;
                }
            }
        }
        timeline.setFollowUpPendingCount(pendingCount);
        timeline.setFollowUpMissedCount(missedCount);

        List<MedicalTimelineNodeVO> nodes = new ArrayList<>();
        for (MedicalRecord record : records) {
            MedicalTimelineNodeVO visitNode = convertRecordToNode(record, today);
            nodes.add(visitNode);

            if (record.getActualFollowUpDate() != null && "COMPLETED".equals(record.getFollowUpStatus())) {
                MedicalTimelineNodeVO followUpNode = convertFollowUpToNode(record);
                nodes.add(followUpNode);
            }
        }

        nodes.sort((a, b) -> {
            if (a.getEventDate() == null && b.getEventDate() == null) return 0;
            if (a.getEventDate() == null) return 1;
            if (b.getEventDate() == null) return -1;
            return b.getEventDate().compareTo(a.getEventDate());
        });

        timeline.setNodes(nodes);
        return timeline;
    }

    @Override
    public List<MedicalRecord> listByPrisonerId(Long prisonerId) {
        return list(new LambdaQueryWrapper<MedicalRecord>()
                .eq(MedicalRecord::getPrisonerId, prisonerId)
                .orderByDesc(MedicalRecord::getRecordDate));
    }

    private MedicalTimelineNodeVO convertRecordToNode(MedicalRecord record, LocalDate today) {
        MedicalTimelineNodeVO node = new MedicalTimelineNodeVO();
        node.setRecordId(record.getId());
        node.setEventDate(record.getRecordDate());
        node.setNodeType("VISIT");

        String medicalType = record.getMedicalType();
        node.setMedicalType(medicalType);
        node.setMedicalTypeLabel(safeGet(MEDICAL_TYPE_LABELS, medicalType, medicalType));
        node.setColor(safeGet(MEDICAL_TYPE_COLORS, medicalType, "#909399"));
        node.setIcon(safeGet(MEDICAL_TYPE_ICONS, medicalType, "FirstAidKit"));

        node.setDiagnosis(record.getDiagnosis());
        node.setTreatment(record.getTreatment());
        node.setHospital(record.getHospital());
        node.setDoctorName(record.getDoctorName());

        String result = record.getResult();
        node.setResult(result);
        node.setResultLabel(safeGet(RESULT_LABELS, result, result));
        node.setResultTagType(safeGet(RESULT_TAG_TYPES, result, "info"));

        node.setMedicine(record.getMedicine());

        if (record.getFollowUpDate() != null) {
            node.setFollowUpDate(record.getFollowUpDate());
            String status = record.getFollowUpStatus();
            boolean isPending = !StringUtils.hasText(status) || "PENDING".equals(status);
            if (isPending && record.getFollowUpDate().isBefore(today)) {
                node.setFollowUpStatus("OVERDUE");
            } else {
                node.setFollowUpStatus(StringUtils.hasText(status) ? status : "PENDING");
            }
            node.setFollowUpStatusLabel(FOLLOW_UP_STATUS_LABELS.getOrDefault(node.getFollowUpStatus(), node.getFollowUpStatus()));
            node.setFollowUpTagType(FOLLOW_UP_TAG_TYPES.getOrDefault(node.getFollowUpStatus(), "warning"));
        }

        return node;
    }

    private MedicalTimelineNodeVO convertFollowUpToNode(MedicalRecord record) {
        MedicalTimelineNodeVO node = new MedicalTimelineNodeVO();
        node.setRecordId(record.getId());
        node.setEventDate(record.getActualFollowUpDate());
        node.setNodeType("FOLLOW_UP");

        node.setMedicalType("FOLLOW_UP");
        node.setMedicalTypeLabel("复诊");
        node.setColor("#8e44ad");
        node.setIcon("Calendar");

        node.setDiagnosis("复诊记录 - " + (record.getDiagnosis() != null ? record.getDiagnosis() : ""));
        node.setTreatment(record.getFollowUpResult());
        node.setHospital(record.getHospital());
        node.setDoctorName(record.getDoctorName());

        node.setResult("COMPLETED");
        node.setResultLabel("已完成复诊");
        node.setResultTagType("success");

        node.setMedicine(record.getMedicine());
        node.setFollowUpRemark(record.getFollowUpRemark());

        node.setFollowUpDate(record.getFollowUpDate());
        node.setActualFollowUpDate(record.getActualFollowUpDate());
        node.setFollowUpStatus("COMPLETED");
        node.setFollowUpStatusLabel(FOLLOW_UP_STATUS_LABELS.get("COMPLETED"));
        node.setFollowUpTagType(FOLLOW_UP_TAG_TYPES.get("COMPLETED"));

        return node;
    }

    @Override
    @Transactional
    public void createMedicalRecord(MedicalRecord record) {
        Prisoner prisoner = prisonerService.getById(record.getPrisonerId());
        if (prisoner == null) {
            throw new BusinessException("服刑人员不存在");
        }
        save(record);
        updatePrisonerHealthStatus(record.getPrisonerId());
        sysLogService.logSuccess(
                SysLogModule.MEDICAL,
                SysLogAction.CREATE,
                "新增医疗记录：服刑人员" + prisoner.getName() + "（编号：" + prisoner.getPrisonerNumber() + "），就诊日期：" + record.getRecordDate(),
                "MEDICAL_RECORD",
                record.getId(),
                prisoner.getName()
        );
    }

    @Override
    @Transactional
    public void updateMedicalRecord(Long id, MedicalRecord record) {
        MedicalRecord existing = getById(id);
        if (existing == null) {
            throw new BusinessException("医疗记录不存在");
        }
        Prisoner prisoner = prisonerService.getById(record.getPrisonerId());
        if (prisoner == null) {
            throw new BusinessException("服刑人员不存在");
        }
        record.setId(id);
        updateById(record);
        if (!existing.getPrisonerId().equals(record.getPrisonerId())) {
            updatePrisonerHealthStatus(existing.getPrisonerId());
        }
        updatePrisonerHealthStatus(record.getPrisonerId());
        sysLogService.logSuccess(
                SysLogModule.MEDICAL,
                SysLogAction.UPDATE,
                "更新医疗记录：服刑人员" + prisoner.getName() + "（编号：" + prisoner.getPrisonerNumber() + "），就诊日期：" + record.getRecordDate(),
                "MEDICAL_RECORD",
                id,
                prisoner.getName()
        );
    }

    @Override
    @Transactional
    public void deleteMedicalRecord(Long id) {
        MedicalRecord existing = getById(id);
        if (existing == null) {
            throw new BusinessException("医疗记录不存在");
        }
        Prisoner prisoner = prisonerService.getById(existing.getPrisonerId());
        removeById(id);
        if (prisoner != null) {
            updatePrisonerHealthStatus(existing.getPrisonerId());
            sysLogService.logSuccess(
                    SysLogModule.MEDICAL,
                    SysLogAction.DELETE,
                    "删除医疗记录：服刑人员" + prisoner.getName() + "（编号：" + prisoner.getPrisonerNumber() + "），就诊日期：" + existing.getRecordDate(),
                    "MEDICAL_RECORD",
                    id,
                    prisoner.getName()
            );
        }
    }

    @Override
    public String calculateHealthStatus(Long prisonerId) {
        List<MedicalRecord> records = listByPrisonerId(prisonerId);
        if (records.isEmpty()) {
            return null;
        }

        boolean hasDeceased = false;
        boolean hasEmergencyTreating = false;
        boolean hasHospitalizationTreating = false;
        boolean hasTreating = false;
        boolean hasEmergency = false;
        boolean hasHospitalization = false;
        boolean hasStable = false;
        boolean hasRecovered = false;
        boolean hasTransferred = false;

        for (MedicalRecord record : records) {
            String result = record.getResult();
            String medicalType = record.getMedicalType();

            if ("DECEASED".equals(result)) {
                hasDeceased = true;
            }
            if ("EMERGENCY".equals(medicalType) && "TREATING".equals(result)) {
                hasEmergencyTreating = true;
            }
            if ("HOSPITALIZATION".equals(medicalType) && "TREATING".equals(result)) {
                hasHospitalizationTreating = true;
            }
            if ("TREATING".equals(result)) {
                hasTreating = true;
            }
            if ("EMERGENCY".equals(medicalType)) {
                hasEmergency = true;
            }
            if ("HOSPITALIZATION".equals(medicalType)) {
                hasHospitalization = true;
            }
            if ("STABLE".equals(result)) {
                hasStable = true;
            }
            if ("RECOVERED".equals(result) || "CURED".equals(result)) {
                hasRecovered = true;
            }
            if ("TRANSFERRED".equals(result)) {
                hasTransferred = true;
            }
        }

        if (hasDeceased) return "已故";
        if (hasEmergencyTreating) return "急诊治疗中";
        if (hasHospitalizationTreating) return "住院治疗中";
        if (hasTreating) return "持续治疗中";
        if (hasEmergency) return "曾急诊";
        if (hasHospitalization) return "曾住院";
        if (hasStable) return "病情稳定";
        if (hasRecovered) return "健康";
        if (hasTransferred) return "已转院";

        return null;
    }

    @Override
    public void updatePrisonerHealthStatus(Long prisonerId) {
        Prisoner prisoner = prisonerService.getById(prisonerId);
        if (prisoner == null) {
            return;
        }
        String newHealthStatus = calculateHealthStatus(prisonerId);
        String oldHealthStatus = prisoner.getHealthStatus();
        if (!Objects.equals(oldHealthStatus, newHealthStatus)) {
            if (newHealthStatus == null) {
                LambdaUpdateWrapper<Prisoner> wrapper = new LambdaUpdateWrapper<>();
                wrapper.eq(Prisoner::getId, prisonerId)
                        .set(Prisoner::getHealthStatus, null);
                prisonerService.update(wrapper);
            } else {
                prisoner.setHealthStatus(newHealthStatus);
                prisonerService.updateById(prisoner);
            }
            sysLogService.logSuccess(
                    SysLogModule.PRISONER,
                    SysLogAction.UPDATE,
                    "更新服刑人员健康状态：" + prisoner.getName() + "（编号：" + prisoner.getPrisonerNumber() + "），" +
                            (oldHealthStatus != null ? oldHealthStatus : "无") + " → " + (newHealthStatus != null ? newHealthStatus : "无"),
                    "PRISONER",
                    prisonerId,
                    prisoner.getName()
            );
        }
    }
}