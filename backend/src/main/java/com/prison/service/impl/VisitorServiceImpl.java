package com.prison.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.prison.dto.VisitorApprovalDTO;
import com.prison.dto.VisitorCalendarQueryDTO;
import com.prison.dto.VisitorDTO;
import com.prison.entity.Guard;
import com.prison.entity.Prisoner;
import com.prison.entity.Visitor;
import com.prison.mapper.VisitorMapper;
import com.prison.service.GuardService;
import com.prison.service.PrisonerService;
import com.prison.service.VisitorService;
import com.prison.vo.LawyerMeetingDetailVO;
import com.prison.vo.VisitorCalendarVO;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class VisitorServiceImpl extends ServiceImpl<VisitorMapper, Visitor> implements VisitorService {

    private final PrisonerService prisonerService;
    private final GuardService guardService;

    private static final Map<String, String> STATUS_TEXT_MAP = new HashMap<>();
    private static final Map<String, String> LAWYER_STATUS_TEXT_MAP = new HashMap<>();
    private static final Map<String, String> CASE_TYPE_TEXT_MAP = new HashMap<>();
    private static final Map<String, String> MEETING_STAGE_TEXT_MAP = new HashMap<>();
    private static final Map<String, String> SECURITY_LEVEL_TEXT_MAP = new HashMap<>();
    private static final Map<String, String> ROOM_TYPE_TEXT_MAP = new HashMap<>();

    static {
        STATUS_TEXT_MAP.put("PENDING", "待审核");
        STATUS_TEXT_MAP.put("APPROVED", "已通过");
        STATUS_TEXT_MAP.put("REJECTED", "已驳回");
        STATUS_TEXT_MAP.put("IN_PROGRESS", "会见中");
        STATUS_TEXT_MAP.put("COMPLETED", "已完成");
        STATUS_TEXT_MAP.put("CANCELLED", "已取消");

        LAWYER_STATUS_TEXT_MAP.put("PENDING", "律师会见待审核");
        LAWYER_STATUS_TEXT_MAP.put("APPROVED", "律师会见已批准");
        LAWYER_STATUS_TEXT_MAP.put("REJECTED", "律师会见已驳回");
        LAWYER_STATUS_TEXT_MAP.put("IN_PROGRESS", "律师会见进行中");
        LAWYER_STATUS_TEXT_MAP.put("COMPLETED", "律师会见已完成");
        LAWYER_STATUS_TEXT_MAP.put("CANCELLED", "律师会见已取消");

        CASE_TYPE_TEXT_MAP.put("CRIMINAL", "刑事");
        CASE_TYPE_TEXT_MAP.put("CIVIL", "民事");
        CASE_TYPE_TEXT_MAP.put("ADMINISTRATIVE", "行政");
        CASE_TYPE_TEXT_MAP.put("OTHER", "其他");

        MEETING_STAGE_TEXT_MAP.put("INVESTIGATION", "侦查阶段");
        MEETING_STAGE_TEXT_MAP.put("PROSECUTION", "审查起诉阶段");
        MEETING_STAGE_TEXT_MAP.put("TRIAL", "审判阶段");
        MEETING_STAGE_TEXT_MAP.put("EXECUTION", "执行阶段");

        SECURITY_LEVEL_TEXT_MAP.put("STANDARD", "标准级");
        SECURITY_LEVEL_TEXT_MAP.put("ELEVATED", "加强级");
        SECURITY_LEVEL_TEXT_MAP.put("STRICT", "严格级");

        ROOM_TYPE_TEXT_MAP.put("NORMAL", "普通会见室");
        ROOM_TYPE_TEXT_MAP.put("ISOLATION", "隔离会见室");
        ROOM_TYPE_TEXT_MAP.put("REMOTE", "远程会见室");
    }

    private volatile Boolean visitTypeColumnExists;

    private boolean isVisitTypeColumnExists() {
        if (visitTypeColumnExists != null) {
            return visitTypeColumnExists;
        }
        synchronized (this) {
            if (visitTypeColumnExists != null) {
                return visitTypeColumnExists;
            }
            try {
                lambdaQuery().eq(Visitor::getVisitType, "__probe__").last("LIMIT 0").count();
                visitTypeColumnExists = true;
            } catch (Exception e) {
                visitTypeColumnExists = false;
            }
            return visitTypeColumnExists;
        }
    }

    public static String getStatusText(String status, boolean isLawyer) {
        return isLawyer
                ? LAWYER_STATUS_TEXT_MAP.getOrDefault(status, STATUS_TEXT_MAP.getOrDefault(status, status))
                : STATUS_TEXT_MAP.getOrDefault(status, status);
    }

    public static String getCaseTypeText(String caseType) {
        return CASE_TYPE_TEXT_MAP.getOrDefault(caseType, "-");
    }

    public static String getMeetingStageText(String stage) {
        return MEETING_STAGE_TEXT_MAP.getOrDefault(stage, "-");
    }

    public static String getSecurityLevelText(String level) {
        return SECURITY_LEVEL_TEXT_MAP.getOrDefault(level, "-");
    }

    public static String getRoomTypeText(String roomType) {
        return ROOM_TYPE_TEXT_MAP.getOrDefault(roomType, "-");
    }

    @Override
    public Page<Visitor> pageVisitors(int page, int size, String keyword, String status, String visitType, String relation) {
        LambdaQueryWrapper<Visitor> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(keyword)) {
            wrapper.and(w -> w.like(Visitor::getVisitorName, keyword)
                    .or()
                    .like(Visitor::getIdCard, keyword)
                    .or()
                    .like(Visitor::getRelation, keyword)
                    .or()
                    .like(Visitor::getLawyerLicenseNo, keyword)
                    .or()
                    .like(Visitor::getLawFirmName, keyword));
        }
        if (StringUtils.hasText(status)) {
            wrapper.eq(Visitor::getStatus, status);
        }
        if (StringUtils.hasText(visitType) && isVisitTypeColumnExists()) {
            wrapper.eq(Visitor::getVisitType, visitType);
        }
        if (StringUtils.hasText(relation)) {
            wrapper.eq(Visitor::getRelation, relation);
        }
        wrapper.orderByDesc(Visitor::getVisitDate);
        Page<Visitor> resultPage = page(new Page<>(page, size), wrapper);
        resultPage.getRecords().forEach(this::enrichVisitorDisplay);
        return resultPage;
    }

    @Override
    public Page<Visitor> pagePendingVisitors(int page, int size, String visitType) {
        LambdaQueryWrapper<Visitor> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Visitor::getStatus, "PENDING");
        if (StringUtils.hasText(visitType) && isVisitTypeColumnExists()) {
            wrapper.eq(Visitor::getVisitType, visitType);
        }
        wrapper.orderByAsc(Visitor::getVisitDate);
        Page<Visitor> resultPage = page(new Page<>(page, size), wrapper);
        resultPage.getRecords().forEach(this::enrichVisitorDisplay);
        return resultPage;
    }

    @Override
    public Page<Visitor> pageLawyerVisitors(int page, int size, String keyword, String status) {
        LambdaQueryWrapper<Visitor> wrapper = new LambdaQueryWrapper<>();
        wrapper.and(w -> w.eq(Visitor::getRelation, "LAWYER")
                .or()
                .eq(Visitor::getVisitType, "LAWYER"));
        if (StringUtils.hasText(keyword)) {
            wrapper.and(w -> w.like(Visitor::getVisitorName, keyword)
                    .or()
                    .like(Visitor::getIdCard, keyword)
                    .or()
                    .like(Visitor::getLawyerLicenseNo, keyword)
                    .or()
                    .like(Visitor::getLawFirmName, keyword)
                    .or()
                    .like(Visitor::getCaseType, keyword));
        }
        if (StringUtils.hasText(status)) {
            wrapper.eq(Visitor::getStatus, status);
        }
        wrapper.orderByDesc(Visitor::getVisitDate);
        Page<Visitor> resultPage = page(new Page<>(page, size), wrapper);
        resultPage.getRecords().forEach(this::enrichVisitorDisplay);
        return resultPage;
    }

    @Override
    public Page<Visitor> pageFamilyVisitors(int page, int size, String keyword, String status) {
        LambdaQueryWrapper<Visitor> wrapper = new LambdaQueryWrapper<>();
        wrapper.and(w -> w.ne(Visitor::getRelation, "LAWYER")
                .and(w2 -> w2.ne(Visitor::getVisitType, "LAWYER").or().isNull(Visitor::getVisitType)));
        if (StringUtils.hasText(keyword)) {
            wrapper.and(w -> w.like(Visitor::getVisitorName, keyword)
                    .or()
                    .like(Visitor::getIdCard, keyword)
                    .or()
                    .like(Visitor::getRelation, keyword));
        }
        if (StringUtils.hasText(status)) {
            wrapper.eq(Visitor::getStatus, status);
        }
        wrapper.orderByDesc(Visitor::getVisitDate);
        Page<Visitor> resultPage = page(new Page<>(page, size), wrapper);
        resultPage.getRecords().forEach(this::enrichVisitorDisplay);
        return resultPage;
    }

    @Override
    public void createVisitor(VisitorDTO dto) {
        Visitor visitor = new Visitor();
        BeanUtils.copyProperties(dto, visitor);
        syncRelationAndVisitType(visitor);
        validateVisitorData(visitor);
        autoFillLawyerDefaults(visitor);
        if (visitor.getStatus() == null) {
            visitor.setStatus("PENDING");
        }
        if (isLawyerVisit(visitor) && visitor.getRecordingRequired() == null) {
            visitor.setRecordingRequired(true);
        }
        save(visitor);
    }

    @Override
    public void updateVisitor(Long id, VisitorDTO dto) {
        Visitor existing = getById(id);
        if (existing == null) {
            throw new RuntimeException("访客记录不存在");
        }
        Visitor visitor = new Visitor();
        BeanUtils.copyProperties(dto, visitor);
        visitor.setId(id);
        syncRelationAndVisitType(visitor);
        validateVisitorData(visitor);
        autoFillLawyerDefaults(visitor);
        updateById(visitor);
    }

    private void autoFillLawyerDefaults(Visitor visitor) {
        if (!isLawyerVisit(visitor)) {
            return;
        }
        if (visitor.getMeetingSecurityLevel() == null) {
            String dangerLevel = getPrisonerDangerLevel(visitor.getPrisonerId());
            if ("HIGH".equals(dangerLevel) || "EXTREME".equals(dangerLevel)) {
                visitor.setMeetingSecurityLevel("STRICT");
            } else if ("MEDIUM".equals(dangerLevel)) {
                visitor.setMeetingSecurityLevel("ELEVATED");
            } else {
                visitor.setMeetingSecurityLevel("STANDARD");
            }
        }
        if (visitor.getRecordingRequired() == null) {
            visitor.setRecordingRequired(true);
        }
        if (visitor.getMeetingStage() == null) {
            visitor.setMeetingStage("EXECUTION");
        }
        if (visitor.getRoomTypeRequired() == null) {
            if ("STRICT".equals(visitor.getMeetingSecurityLevel())) {
                visitor.setRoomTypeRequired("ISOLATION");
            } else {
                visitor.setRoomTypeRequired("NORMAL");
            }
        }
    }

    private String getPrisonerDangerLevel(Long prisonerId) {
        if (prisonerId == null) {
            return "LOW";
        }
        try {
            Prisoner prisoner = prisonerService.getById(prisonerId);
            return prisoner != null ? prisoner.getDangerLevel() : "LOW";
        } catch (Exception e) {
            return "LOW";
        }
    }

    private boolean isLawyerVisit(Visitor visitor) {
        return "LAWYER".equals(visitor.getRelation()) || "LAWYER".equals(visitor.getVisitType());
    }

    private void syncRelationAndVisitType(Visitor visitor) {
        if ("LAWYER".equals(visitor.getRelation())) {
            visitor.setVisitType("LAWYER");
        } else if ("LAWYER".equals(visitor.getVisitType())) {
            visitor.setRelation("LAWYER");
        }
    }

    private void validateVisitorData(Visitor visitor) {
        if (isLawyerVisit(visitor)) {
            validateLawyerFields(visitor);
        } else {
            clearLawyerFieldsForNonLawyer(visitor);
            validateFamilyFields(visitor);
        }
    }

    private void clearLawyerFieldsForNonLawyer(Visitor visitor) {
        visitor.setLawyerLicenseNo(null);
        visitor.setLawFirmName(null);
        visitor.setPowerOfAttorneyNo(null);
        visitor.setCaseType(null);
        visitor.setLawyerLicenseValidDate(null);
        visitor.setIsLegalAid(null);
        visitor.setAssistantLawyerName(null);
        visitor.setAssistantLawyerLicenseNo(null);
        visitor.setMeetingSecurityLevel(null);
        visitor.setIsUrgentLawyerMeeting(null);
        visitor.setLawyerEmail(null);
        visitor.setMeetingStage(null);
        visitor.setRoomTypeRequired(null);
        visitor.setHasAssistant(null);
        visitor.setNeedsTranslator(null);
        visitor.setRecordingRequired(null);
    }

    private void validateFamilyFields(Visitor visitor) {
        if (!StringUtils.hasText(visitor.getRelation())) {
            throw new RuntimeException("【家属会见资料不完整】请选择与服刑人员关系");
        }
        if ("LAWYER".equals(visitor.getRelation())) {
            return;
        }
        if ("OTHER".equals(visitor.getRelation()) && !StringUtils.hasText(visitor.getPurpose())) {
            throw new RuntimeException("【其他关系会见】请填写会见目的，说明探视理由");
        }
    }

    @Override
    public Visitor enrichVisitorDisplay(Visitor visitor) {
        if (visitor == null) {
            return null;
        }
        boolean isLawyer = isLawyerVisit(visitor);
        visitor.setIsLawyerVisit(isLawyer);
        visitor.setStatusText(getStatusText(visitor.getStatus(), isLawyer));
        if (isLawyer) {
            visitor.setCaseTypeText(getCaseTypeText(visitor.getCaseType()));
            visitor.setMeetingStageText(getMeetingStageText(visitor.getMeetingStage()));
            visitor.setMeetingSecurityLevelText(getSecurityLevelText(visitor.getMeetingSecurityLevel()));
            visitor.setRoomTypeText(getRoomTypeText(visitor.getRoomTypeRequired()));
            if (visitor.getLawyerLicenseValidDate() != null) {
                visitor.setLawyerLicenseExpired(visitor.getLawyerLicenseValidDate().isBefore(LocalDate.now()));
            }
        }
        return visitor;
    }

    private void validateLawyerFields(Visitor visitor) {
        List<String> missingItems = new ArrayList<>();
        if (!StringUtils.hasText(visitor.getLawyerLicenseNo())) {
            missingItems.add("律师执业证号");
        }
        if (!StringUtils.hasText(visitor.getLawFirmName())) {
            missingItems.add("律师事务所名称");
        }
        if (!StringUtils.hasText(visitor.getPowerOfAttorneyNo())) {
            missingItems.add("委托书/法律援助公函编号");
        }
        if (!StringUtils.hasText(visitor.getCaseType())) {
            missingItems.add("案件类型");
        }
        if (!missingItems.isEmpty()) {
            throw new RuntimeException("【律师会见资料不完整】请补充：" + String.join("、", missingItems));
        }
        if (visitor.getLawyerLicenseValidDate() != null
                && visitor.getLawyerLicenseValidDate().isBefore(LocalDate.now())) {
            throw new RuntimeException("【律师执业证已过期】有效期至 " + visitor.getLawyerLicenseValidDate() + "，请核验证件有效性");
        }
        if (Boolean.TRUE.equals(visitor.getHasAssistant())
                && !StringUtils.hasText(visitor.getAssistantLawyerName())) {
            throw new RuntimeException("【携带助理会见】必须填写协办律师姓名");
        }
        if (Boolean.TRUE.equals(visitor.getHasAssistant())
                && !StringUtils.hasText(visitor.getAssistantLawyerLicenseNo())) {
            throw new RuntimeException("【携带助理会见】必须填写协办律师执业证号");
        }
        if (Boolean.TRUE.equals(visitor.getIsLegalAid())
                && !StringUtils.hasText(visitor.getPowerOfAttorneyNo())) {
            throw new RuntimeException("【法律援助案件】必须提供法律援助公函编号");
        }
        if (Boolean.TRUE.equals(visitor.getIsUrgentLawyerMeeting())
                && !StringUtils.hasText(visitor.getPurpose())) {
            throw new RuntimeException("【紧急律师会见】必须说明紧急事由（填写会见目的）");
        }
    }

    @Override
    public void approve(Long id, VisitorApprovalDTO dto) {
        Visitor visitor = getById(id);
        if (visitor == null) {
            throw new RuntimeException("访客记录不存在");
        }
        if (!"PENDING".equals(visitor.getStatus())) {
            throw new RuntimeException("当前状态不允许审批通过");
        }
        if (isLawyerVisit(visitor)) {
            validateLawyerApproval(visitor);
            if (!StringUtils.hasText(dto.getApproveRemark())) {
                throw new RuntimeException("【律师会见审批】必须填写审批意见，说明核验情况");
            }
        }
        visitor.setStatus("APPROVED");
        visitor.setApproveRemark(dto.getApproveRemark());
        visitor.setApproveGuardId(dto.getApproveGuardId());
        visitor.setApproveTime(LocalDateTime.now());
        updateById(visitor);
    }

    private void validateLawyerApproval(Visitor visitor) {
        List<String> missingItems = new ArrayList<>();
        if (!StringUtils.hasText(visitor.getLawyerLicenseNo())) {
            missingItems.add("律师执业证");
        }
        if (!StringUtils.hasText(visitor.getLawFirmName())) {
            missingItems.add("律师事务所证明（律所函）");
        }
        if (!StringUtils.hasText(visitor.getPowerOfAttorneyNo())) {
            missingItems.add("委托书或法律援助公函");
        }
        if (visitor.getLawyerLicenseValidDate() == null) {
            missingItems.add("律师执业证有效期");
        } else if (visitor.getLawyerLicenseValidDate().isBefore(LocalDate.now())) {
            missingItems.add("执业证已过期（有效期至 " + visitor.getLawyerLicenseValidDate() + "）");
        }
        if (Boolean.TRUE.equals(visitor.getHasAssistant())) {
            if (!StringUtils.hasText(visitor.getAssistantLawyerName())) {
                missingItems.add("协办律师姓名");
            }
            if (!StringUtils.hasText(visitor.getAssistantLawyerLicenseNo())) {
                missingItems.add("协办律师执业证号");
            }
        }
        if (!missingItems.isEmpty()) {
            throw new RuntimeException("【律师会见审批未通过】请核验以下材料：" + String.join("、", missingItems));
        }
    }

    @Override
    public void reject(Long id, VisitorApprovalDTO dto) {
        Visitor visitor = getById(id);
        if (visitor == null) {
            throw new RuntimeException("访客记录不存在");
        }
        if (!"PENDING".equals(visitor.getStatus())) {
            throw new RuntimeException("当前状态不允许驳回");
        }
        if (isLawyerVisit(visitor)) {
            if (!StringUtils.hasText(dto.getApproveRemark())) {
                throw new RuntimeException("【驳回律师会见】必须说明具体驳回理由，如材料缺失、证件无效等");
            }
            if (dto.getApproveRemark().length() < 5) {
                throw new RuntimeException("【驳回律师会见】驳回理由过于简略，请详细说明原因（至少5个字）");
            }
        }
        visitor.setStatus("REJECTED");
        visitor.setApproveRemark(dto.getApproveRemark());
        visitor.setApproveGuardId(dto.getApproveGuardId());
        visitor.setApproveTime(LocalDateTime.now());
        updateById(visitor);
    }

    @Override
    public void startVisit(Long id) {
        Visitor visitor = getById(id);
        if (visitor == null) {
            throw new RuntimeException("访客记录不存在");
        }
        if (!"APPROVED".equals(visitor.getStatus())) {
            throw new RuntimeException("当前状态不允许开始会见");
        }
        if (isLawyerVisit(visitor)) {
            List<String> missingItems = buildLawyerVerificationList(visitor).stream()
                    .filter(item -> !Boolean.TRUE.equals(item.getPassed()))
                    .map(LawyerMeetingDetailVO.VerificationItem::getName)
                    .collect(Collectors.toList());
            if (!missingItems.isEmpty()) {
                throw new RuntimeException("【律师会见开始前核验未通过】请确认：" + String.join("、", missingItems));
            }
        }
        visitor.setStatus("IN_PROGRESS");
        visitor.setActualStartTime(LocalDateTime.now());
        updateById(visitor);
    }

    @Override
    public void endVisit(Long id) {
        Visitor visitor = getById(id);
        if (visitor == null) {
            throw new RuntimeException("访客记录不存在");
        }
        if (!"IN_PROGRESS".equals(visitor.getStatus())) {
            throw new RuntimeException("当前状态不允许结束会见");
        }
        visitor.setStatus("COMPLETED");
        visitor.setActualEndTime(LocalDateTime.now());
        updateById(visitor);
    }

    @Override
    public void cancel(Long id, VisitorApprovalDTO dto) {
        Visitor visitor = getById(id);
        if (visitor == null) {
            throw new RuntimeException("访客记录不存在");
        }
        boolean isLawyer = isLawyerVisit(visitor);
        if (!"PENDING".equals(visitor.getStatus()) && !"APPROVED".equals(visitor.getStatus())) {
            throw new RuntimeException(isLawyer ? "【律师会见】当前状态不允许取消" : "当前状态不允许取消");
        }
        if (isLawyer) {
            if (!StringUtils.hasText(dto.getApproveRemark())) {
                throw new RuntimeException("【取消律师会见】必须说明取消原因，如时间冲突、材料补充等");
            }
            if (dto.getApproveRemark().length() < 5) {
                throw new RuntimeException("【取消律师会见】取消原因过于简略，请详细说明（至少5个字）");
            }
        }
        visitor.setStatus("CANCELLED");
        visitor.setApproveRemark(dto.getApproveRemark());
        visitor.setApproveGuardId(dto.getApproveGuardId());
        visitor.setApproveTime(LocalDateTime.now());
        updateById(visitor);
    }

    @Override
    public Map<String, Long> getStatusStatistics() {
        Map<String, Long> result = new HashMap<>();
        result.put("pending", lambdaQuery().eq(Visitor::getStatus, "PENDING").count());
        result.put("approved", lambdaQuery().eq(Visitor::getStatus, "APPROVED").count());
        result.put("inProgress", lambdaQuery().eq(Visitor::getStatus, "IN_PROGRESS").count());
        result.put("rejected", lambdaQuery().eq(Visitor::getStatus, "REJECTED").count());
        result.put("completed", lambdaQuery().eq(Visitor::getStatus, "COMPLETED").count());
        result.put("cancelled", lambdaQuery().eq(Visitor::getStatus, "CANCELLED").count());

        result.put("lawyerPending", lambdaQuery()
                .eq(Visitor::getStatus, "PENDING")
                .and(w -> w.eq(Visitor::getRelation, "LAWYER").or().eq(Visitor::getVisitType, "LAWYER"))
                .count());
        result.put("familyPending", lambdaQuery()
                .eq(Visitor::getStatus, "PENDING")
                .and(w -> w.ne(Visitor::getRelation, "LAWYER").and(w2 -> w2.ne(Visitor::getVisitType, "LAWYER").or().isNull(Visitor::getVisitType)))
                .count());
        return result;
    }

    @Override
    public Map<String, Long> getTypeStatistics() {
        Map<String, Long> result = new HashMap<>();
        if (!isVisitTypeColumnExists()) {
            result.put("family", 0L);
            result.put("lawyer", 0L);
            result.put("other", 0L);
            return result;
        }
        result.put("family", lambdaQuery().eq(Visitor::getVisitType, "FAMILY").count());
        result.put("lawyer", lambdaQuery().eq(Visitor::getVisitType, "LAWYER").count());
        result.put("other", lambdaQuery().ne(Visitor::getVisitType, "FAMILY")
                .ne(Visitor::getVisitType, "LAWYER")
                .or(w -> w.isNull(Visitor::getVisitType))
                .count());
        return result;
    }

    @Override
    public Map<String, Long> getDetailedStatistics() {
        Map<String, Long> result = new HashMap<>();
        result.putAll(getStatusStatistics());
        result.putAll(getTypeStatistics());

        result.put("lawyerApproved", lambdaQuery()
                .eq(Visitor::getStatus, "APPROVED")
                .and(w -> w.eq(Visitor::getRelation, "LAWYER").or().eq(Visitor::getVisitType, "LAWYER"))
                .count());
        result.put("lawyerInProgress", lambdaQuery()
                .eq(Visitor::getStatus, "IN_PROGRESS")
                .and(w -> w.eq(Visitor::getRelation, "LAWYER").or().eq(Visitor::getVisitType, "LAWYER"))
                .count());
        result.put("lawyerCompleted", lambdaQuery()
                .eq(Visitor::getStatus, "COMPLETED")
                .and(w -> w.eq(Visitor::getRelation, "LAWYER").or().eq(Visitor::getVisitType, "LAWYER"))
                .count());
        result.put("lawyerRejected", lambdaQuery()
                .eq(Visitor::getStatus, "REJECTED")
                .and(w -> w.eq(Visitor::getRelation, "LAWYER").or().eq(Visitor::getVisitType, "LAWYER"))
                .count());
        result.put("lawyerCancelled", lambdaQuery()
                .eq(Visitor::getStatus, "CANCELLED")
                .and(w -> w.eq(Visitor::getRelation, "LAWYER").or().eq(Visitor::getVisitType, "LAWYER"))
                .count());
        result.put("lawyerUrgentPending", lambdaQuery()
                .eq(Visitor::getStatus, "PENDING")
                .and(w -> w.eq(Visitor::getRelation, "LAWYER").or().eq(Visitor::getVisitType, "LAWYER"))
                .eq(Visitor::getIsUrgentLawyerMeeting, Boolean.TRUE)
                .count());
        result.put("lawyerLicenseExpiredPending", lambdaQuery()
                .eq(Visitor::getStatus, "PENDING")
                .and(w -> w.eq(Visitor::getRelation, "LAWYER").or().eq(Visitor::getVisitType, "LAWYER"))
                .lt(Visitor::getLawyerLicenseValidDate, LocalDate.now())
                .count());
        result.put("familyPending", lambdaQuery()
                .eq(Visitor::getStatus, "PENDING")
                .and(w -> w.ne(Visitor::getRelation, "LAWYER").and(w2 -> w2.ne(Visitor::getVisitType, "LAWYER").or().isNull(Visitor::getVisitType)))
                .count());
        result.put("familyApproved", lambdaQuery()
                .eq(Visitor::getStatus, "APPROVED")
                .and(w -> w.ne(Visitor::getRelation, "LAWYER").and(w2 -> w2.ne(Visitor::getVisitType, "LAWYER").or().isNull(Visitor::getVisitType)))
                .count());
        result.put("familyInProgress", lambdaQuery()
                .eq(Visitor::getStatus, "IN_PROGRESS")
                .and(w -> w.ne(Visitor::getRelation, "LAWYER").and(w2 -> w2.ne(Visitor::getVisitType, "LAWYER").or().isNull(Visitor::getVisitType)))
                .count());
        result.put("familyCompleted", lambdaQuery()
                .eq(Visitor::getStatus, "COMPLETED")
                .and(w -> w.ne(Visitor::getRelation, "LAWYER").and(w2 -> w2.ne(Visitor::getVisitType, "LAWYER").or().isNull(Visitor::getVisitType)))
                .count());
        return result;
    }

    @Override
    public List<VisitorCalendarVO> getCalendarList(VisitorCalendarQueryDTO queryDTO) {
        LambdaQueryWrapper<Visitor> wrapper = new LambdaQueryWrapper<>();
        wrapper.select(
                Visitor::getId,
                Visitor::getVisitorName,
                Visitor::getRelation,
                Visitor::getVisitType,
                Visitor::getPrisonerId,
                Visitor::getVisitDate,
                Visitor::getVisitTimeSlot,
                Visitor::getStatus
        );
        if (queryDTO.getStartDate() != null) {
            wrapper.ge(Visitor::getVisitDate, queryDTO.getStartDate());
        }
        if (queryDTO.getEndDate() != null) {
            wrapper.le(Visitor::getVisitDate, queryDTO.getEndDate());
        }
        if (StringUtils.hasText(queryDTO.getStatus())) {
            wrapper.eq(Visitor::getStatus, queryDTO.getStatus());
        }
        if (StringUtils.hasText(queryDTO.getVisitType()) && isVisitTypeColumnExists()) {
            wrapper.eq(Visitor::getVisitType, queryDTO.getVisitType());
        }
        if (StringUtils.hasText(queryDTO.getRelation())) {
            wrapper.eq(Visitor::getRelation, queryDTO.getRelation());
        }
        wrapper.orderByAsc(Visitor::getVisitDate, Visitor::getVisitTimeSlot);
        List<Visitor> visitors = list(wrapper);

        if (visitors.isEmpty()) {
            return new ArrayList<>();
        }

        List<Long> prisonerIds = visitors.stream()
                .map(Visitor::getPrisonerId)
                .filter(id -> id != null)
                .distinct()
                .collect(Collectors.toList());

        Map<Long, Prisoner> prisonerMap = new HashMap<>();
        if (!prisonerIds.isEmpty()) {
            List<Prisoner> prisoners = prisonerService.listByIds(prisonerIds);
            prisonerMap = prisoners.stream()
                    .collect(Collectors.toMap(Prisoner::getId, p -> p));
        }

        List<VisitorCalendarVO> result = new ArrayList<>();
        for (Visitor visitor : visitors) {
            VisitorCalendarVO vo = new VisitorCalendarVO();
            BeanUtils.copyProperties(visitor, vo);
            Prisoner prisoner = prisonerMap.get(visitor.getPrisonerId());
            if (prisoner != null) {
                vo.setPrisonerName(prisoner.getName());
                vo.setPrisonerNumber(prisoner.getPrisonerNumber());
            }
            result.add(vo);
        }
        return result;
    }

    @Override
    public LawyerMeetingDetailVO getLawyerMeetingDetail(Long id) {
        Visitor visitor = getById(id);
        if (visitor == null) {
            throw new RuntimeException("访客记录不存在");
        }
        if (!isLawyerVisit(visitor)) {
            throw new RuntimeException("该记录不是律师会见，无法查看律师会见详情");
        }

        LawyerMeetingDetailVO vo = new LawyerMeetingDetailVO();
        BeanUtils.copyProperties(visitor, vo);

        vo.setStatusText(getStatusText(visitor.getStatus(), true));
        vo.setCaseTypeText(getCaseTypeText(visitor.getCaseType()));
        vo.setMeetingStageText(getMeetingStageText(visitor.getMeetingStage()));
        vo.setMeetingSecurityLevelText(getSecurityLevelText(visitor.getMeetingSecurityLevel()));
        vo.setRoomTypeRequiredText(getRoomTypeText(visitor.getRoomTypeRequired()));

        if (visitor.getLawyerLicenseValidDate() != null) {
            vo.setLawyerLicenseExpired(visitor.getLawyerLicenseValidDate().isBefore(LocalDate.now()));
        }

        Prisoner prisoner = null;
        if (visitor.getPrisonerId() != null) {
            try {
                prisoner = prisonerService.getById(visitor.getPrisonerId());
            } catch (Exception ignored) {
            }
        }
        if (prisoner != null) {
            vo.setPrisonerName(prisoner.getName());
            vo.setPrisonerNumber(prisoner.getPrisonerNumber());
        }

        if (visitor.getApproveGuardId() != null) {
            try {
                Guard guard = guardService.getById(visitor.getApproveGuardId());
                if (guard != null) {
                    vo.setApproveGuardName(guard.getName());
                }
            } catch (Exception ignored) {
            }
        }

        vo.setVerificationChecklist(buildLawyerVerificationList(visitor));

        return vo;
    }

    private List<LawyerMeetingDetailVO.VerificationItem> buildLawyerVerificationList(Visitor visitor) {
        List<LawyerMeetingDetailVO.VerificationItem> list = new ArrayList<>();

        boolean hasLicense = StringUtils.hasText(visitor.getLawyerLicenseNo());
        list.add(new LawyerMeetingDetailVO.VerificationItem(
                "LAWYER_LICENSE",
                "律师执业证",
                hasLicense,
                hasLicense ? null : "未提供律师执业证号"
        ));

        boolean licenseValid = true;
        String licenseRemark = null;
        if (visitor.getLawyerLicenseValidDate() == null) {
            licenseValid = false;
            licenseRemark = "未填写执业证有效期";
        } else if (visitor.getLawyerLicenseValidDate().isBefore(LocalDate.now())) {
            licenseValid = false;
            licenseRemark = "执业证已于 " + visitor.getLawyerLicenseValidDate() + " 过期";
        }
        list.add(new LawyerMeetingDetailVO.VerificationItem(
                "LICENSE_VALIDITY",
                "执业证有效期核验",
                licenseValid,
                licenseRemark
        ));

        boolean hasFirm = StringUtils.hasText(visitor.getLawFirmName());
        list.add(new LawyerMeetingDetailVO.VerificationItem(
                "LAW_FIRM_LETTER",
                "律师事务所证明（律所函）",
                hasFirm,
                hasFirm ? null : "未提供律师事务所名称"
        ));

        boolean hasPOA = StringUtils.hasText(visitor.getPowerOfAttorneyNo());
        list.add(new LawyerMeetingDetailVO.VerificationItem(
                "POWER_OF_ATTORNEY",
                "委托书或法律援助公函",
                hasPOA,
                hasPOA ? null : "未提供委托书/公函编号"
        ));

        if (Boolean.TRUE.equals(visitor.getIsLegalAid())) {
            boolean aidOk = StringUtils.hasText(visitor.getPowerOfAttorneyNo());
            list.add(new LawyerMeetingDetailVO.VerificationItem(
                    "LEGAL_AID_CERT",
                    "法律援助公函",
                    aidOk,
                    aidOk ? null : "法律援助案件必须提供法律援助公函编号"
            ));
        }

        if (Boolean.TRUE.equals(visitor.getHasAssistant())) {
            boolean assistantNameOk = StringUtils.hasText(visitor.getAssistantLawyerName());
            list.add(new LawyerMeetingDetailVO.VerificationItem(
                    "ASSISTANT_NAME",
                    "协办律师姓名",
                    assistantNameOk,
                    assistantNameOk ? null : "携带助理时必须提供协办律师姓名"
            ));
            boolean assistantLicenseOk = StringUtils.hasText(visitor.getAssistantLawyerLicenseNo());
            list.add(new LawyerMeetingDetailVO.VerificationItem(
                    "ASSISTANT_LICENSE",
                    "协办律师执业证号",
                    assistantLicenseOk,
                    assistantLicenseOk ? null : "携带助理时必须提供协办律师执业证号"
            ));
        }

        if (Boolean.TRUE.equals(visitor.getIsUrgentLawyerMeeting())) {
            boolean urgentOk = StringUtils.hasText(visitor.getPurpose());
            list.add(new LawyerMeetingDetailVO.VerificationItem(
                    "URGENT_REASON",
                    "紧急事由说明",
                    urgentOk,
                    urgentOk ? null : "紧急会见必须说明紧急事由"
            ));
        }

        return list;
    }
}
