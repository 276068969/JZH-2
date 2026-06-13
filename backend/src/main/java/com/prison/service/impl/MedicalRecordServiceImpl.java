package com.prison.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.prison.entity.Cell;
import com.prison.entity.MedicalRecord;
import com.prison.entity.PrisonArea;
import com.prison.entity.Prisoner;
import com.prison.mapper.CellMapper;
import com.prison.mapper.MedicalRecordMapper;
import com.prison.mapper.PrisonAreaMapper;
import com.prison.mapper.PrisonerMapper;
import com.prison.service.MedicalRecordService;
import com.prison.vo.MedicalTimelineNodeVO;
import com.prison.vo.MedicalTimelineVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.util.*;

@Service
@RequiredArgsConstructor
public class MedicalRecordServiceImpl extends ServiceImpl<MedicalRecordMapper, MedicalRecord> implements MedicalRecordService {

    private final PrisonerMapper prisonerMapper;
    private final PrisonAreaMapper prisonAreaMapper;
    private final CellMapper cellMapper;

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
            "TRANSFERRED", "已转院",
            "DECEASED", "已故"
    );

    private static final Map<String, String> RESULT_TAG_TYPES = Map.of(
            "RECOVERED", "success",
            "TREATING", "warning",
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
            throw new IllegalArgumentException("服刑人员不存在");
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

        nodes.sort(Comparator.comparing(MedicalTimelineNodeVO::getEventDate).reversed());

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
        node.setMedicalTypeLabel(MEDICAL_TYPE_LABELS.getOrDefault(medicalType, medicalType));
        node.setColor(MEDICAL_TYPE_COLORS.getOrDefault(medicalType, "#909399"));
        node.setIcon(MEDICAL_TYPE_ICONS.getOrDefault(medicalType, "FirstAidKit"));

        node.setDiagnosis(record.getDiagnosis());
        node.setTreatment(record.getTreatment());
        node.setHospital(record.getHospital());
        node.setDoctorName(record.getDoctorName());

        String result = record.getResult();
        node.setResult(result);
        node.setResultLabel(RESULT_LABELS.getOrDefault(result, result));
        node.setResultTagType(RESULT_TAG_TYPES.getOrDefault(result, "info"));

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
}