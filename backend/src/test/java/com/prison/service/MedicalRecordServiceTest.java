package com.prison.service;

import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.prison.entity.MedicalRecord;
import com.prison.entity.Prisoner;
import com.prison.mapper.CellMapper;
import com.prison.mapper.MedicalRecordMapper;
import com.prison.mapper.PrisonAreaMapper;
import com.prison.mapper.PrisonerMapper;
import com.prison.service.impl.MedicalRecordServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MedicalRecordServiceTest {

    @Mock
    private MedicalRecordMapper medicalRecordMapper;

    @Mock
    private PrisonerMapper prisonerMapper;

    @Mock
    private PrisonAreaMapper prisonAreaMapper;

    @Mock
    private CellMapper cellMapper;

    @Mock
    private PrisonerService prisonerService;

    @Mock
    private SysLogService sysLogService;

    @Spy
    @InjectMocks
    private MedicalRecordServiceImpl medicalRecordService;

    private Prisoner testPrisoner;

    @BeforeEach
    void setUp() {
        testPrisoner = new Prisoner();
        testPrisoner.setId(1L);
        testPrisoner.setName("测试人员");
        testPrisoner.setPrisonerNumber("P20240001");
        testPrisoner.setHealthStatus("持续治疗中");
    }

    @Test
    @DisplayName("健康状态计算 - 无医疗记录返回null")
    void testCalculateHealthStatus_NoRecords_ReturnsNull() {
        doReturn(Collections.emptyList()).when(medicalRecordService).listByPrisonerId(1L);

        String result = medicalRecordService.calculateHealthStatus(1L);

        assertNull(result);
    }

    @Test
    @DisplayName("健康状态计算 - 已故优先级最高")
    void testCalculateHealthStatus_Deceased_HighestPriority() {
        List<MedicalRecord> records = new ArrayList<>();

        MedicalRecord r1 = new MedicalRecord();
        r1.setResult("DECEASED");
        r1.setMedicalType("OUTPATIENT");
        records.add(r1);

        MedicalRecord r2 = new MedicalRecord();
        r2.setResult("TREATING");
        r2.setMedicalType("HOSPITALIZATION");
        records.add(r2);

        doReturn(records).when(medicalRecordService).listByPrisonerId(1L);

        String result = medicalRecordService.calculateHealthStatus(1L);

        assertEquals("已故", result);
    }

    @Test
    @DisplayName("健康状态计算 - 急诊治疗中优先级高于住院治疗中")
    void testCalculateHealthStatus_EmergencyTreating_HigherThanHospitalization() {
        List<MedicalRecord> records = new ArrayList<>();

        MedicalRecord r1 = new MedicalRecord();
        r1.setResult("TREATING");
        r1.setMedicalType("EMERGENCY");
        records.add(r1);

        MedicalRecord r2 = new MedicalRecord();
        r2.setResult("TREATING");
        r2.setMedicalType("HOSPITALIZATION");
        records.add(r2);

        doReturn(records).when(medicalRecordService).listByPrisonerId(1L);

        String result = medicalRecordService.calculateHealthStatus(1L);

        assertEquals("急诊治疗中", result);
    }

    @Test
    @DisplayName("健康状态计算 - 住院治疗中优先级高于持续治疗中")
    void testCalculateHealthStatus_HospitalizationTreating_HigherThanTreating() {
        List<MedicalRecord> records = new ArrayList<>();

        MedicalRecord r1 = new MedicalRecord();
        r1.setResult("TREATING");
        r1.setMedicalType("OUTPATIENT");
        records.add(r1);

        MedicalRecord r2 = new MedicalRecord();
        r2.setResult("TREATING");
        r2.setMedicalType("HOSPITALIZATION");
        records.add(r2);

        doReturn(records).when(medicalRecordService).listByPrisonerId(1L);

        String result = medicalRecordService.calculateHealthStatus(1L);

        assertEquals("住院治疗中", result);
    }

    @Test
    @DisplayName("健康状态计算 - 仅治疗中记录返回持续治疗中")
    void testCalculateHealthStatus_OnlyTreating_ReturnsChronicTreating() {
        List<MedicalRecord> records = new ArrayList<>();

        MedicalRecord r = new MedicalRecord();
        r.setResult("TREATING");
        r.setMedicalType("OUTPATIENT");
        records.add(r);

        doReturn(records).when(medicalRecordService).listByPrisonerId(1L);

        String result = medicalRecordService.calculateHealthStatus(1L);

        assertEquals("持续治疗中", result);
    }

    @Test
    @DisplayName("健康状态计算 - 曾急诊")
    void testCalculateHealthStatus_OnlyRecoveredEmergency_ReturnsHadEmergency() {
        List<MedicalRecord> records = new ArrayList<>();

        MedicalRecord r = new MedicalRecord();
        r.setResult("RECOVERED");
        r.setMedicalType("EMERGENCY");
        records.add(r);

        doReturn(records).when(medicalRecordService).listByPrisonerId(1L);

        String result = medicalRecordService.calculateHealthStatus(1L);

        assertEquals("曾急诊", result);
    }

    @Test
    @DisplayName("健康状态计算 - 曾住院")
    void testCalculateHealthStatus_OnlyRecoveredHospitalization_ReturnsHadHospitalization() {
        List<MedicalRecord> records = new ArrayList<>();

        MedicalRecord r = new MedicalRecord();
        r.setResult("RECOVERED");
        r.setMedicalType("HOSPITALIZATION");
        records.add(r);

        doReturn(records).when(medicalRecordService).listByPrisonerId(1L);

        String result = medicalRecordService.calculateHealthStatus(1L);

        assertEquals("曾住院", result);
    }

    @Test
    @DisplayName("健康状态计算 - 病情稳定")
    void testCalculateHealthStatus_OnlyStable_ReturnsStable() {
        List<MedicalRecord> records = new ArrayList<>();

        MedicalRecord r = new MedicalRecord();
        r.setResult("STABLE");
        r.setMedicalType("OUTPATIENT");
        records.add(r);

        doReturn(records).when(medicalRecordService).listByPrisonerId(1L);

        String result = medicalRecordService.calculateHealthStatus(1L);

        assertEquals("病情稳定", result);
    }

    @Test
    @DisplayName("健康状态计算 - 已治愈返回健康")
    void testCalculateHealthStatus_OnlyRecovered_ReturnsHealthy() {
        List<MedicalRecord> records = new ArrayList<>();

        MedicalRecord r1 = new MedicalRecord();
        r1.setResult("RECOVERED");
        r1.setMedicalType("OUTPATIENT");
        records.add(r1);

        MedicalRecord r2 = new MedicalRecord();
        r2.setResult("CURED");
        r2.setMedicalType("OUTPATIENT");
        records.add(r2);

        doReturn(records).when(medicalRecordService).listByPrisonerId(1L);

        String result = medicalRecordService.calculateHealthStatus(1L);

        assertEquals("健康", result);
    }

    @Test
    @DisplayName("健康状态计算 - 已转院")
    void testCalculateHealthStatus_OnlyTransferred_ReturnsTransferred() {
        List<MedicalRecord> records = new ArrayList<>();

        MedicalRecord r = new MedicalRecord();
        r.setResult("TRANSFERRED");
        r.setMedicalType("OUTPATIENT");
        records.add(r);

        doReturn(records).when(medicalRecordService).listByPrisonerId(1L);

        String result = medicalRecordService.calculateHealthStatus(1L);

        assertEquals("已转院", result);
    }

    @Test
    @DisplayName("删除最后一条记录 - 健康状态变为null，使用UpdateWrapper持久化")
    void testDeleteLastRecord_HealthStatusBecomesNull_UsesUpdateWrapper() {
        MedicalRecord existingRecord = new MedicalRecord();
        existingRecord.setId(1L);
        existingRecord.setPrisonerId(1L);
        existingRecord.setRecordDate(LocalDate.now().minusDays(5));
        existingRecord.setMedicalType("OUTPATIENT");
        existingRecord.setResult("TREATING");

        when(prisonerService.getById(1L)).thenReturn(testPrisoner);
        doReturn(existingRecord).when(medicalRecordService).getById(1L);
        doReturn(true).when(medicalRecordService).removeById(1L);
        doReturn(Collections.emptyList()).when(medicalRecordService).listByPrisonerId(1L);

        medicalRecordService.deleteMedicalRecord(1L);

        ArgumentCaptor<LambdaUpdateWrapper<Prisoner>> wrapperCaptor = ArgumentCaptor.forClass(LambdaUpdateWrapper.class);
        verify(prisonerService, times(1)).update(wrapperCaptor.capture());
        verify(prisonerService, never()).updateById(any(Prisoner.class));

        LambdaUpdateWrapper<Prisoner> wrapper = wrapperCaptor.getValue();
        assertNotNull(wrapper, "必须使用UpdateWrapper来设置null值");

        verify(sysLogService, times(2)).logSuccess(any(), any(), anyString(), anyString(), any(), anyString());
    }

    @Test
    @DisplayName("删除非最后一条记录 - 健康状态更新使用updateById")
    void testDeleteNonLastRecord_HealthStatusUpdate_UsesUpdateById() {
        MedicalRecord existingRecord = new MedicalRecord();
        existingRecord.setId(1L);
        existingRecord.setPrisonerId(1L);
        existingRecord.setRecordDate(LocalDate.now().minusDays(10));
        existingRecord.setMedicalType("EMERGENCY");
        existingRecord.setResult("RECOVERED");

        List<MedicalRecord> remainingRecords = new ArrayList<>();
        MedicalRecord remaining = new MedicalRecord();
        remaining.setId(2L);
        remaining.setPrisonerId(1L);
        remaining.setResult("TREATING");
        remaining.setMedicalType("OUTPATIENT");
        remainingRecords.add(remaining);

        when(prisonerService.getById(1L)).thenReturn(testPrisoner);
        doReturn(existingRecord).when(medicalRecordService).getById(1L);
        doReturn(true).when(medicalRecordService).removeById(1L);
        doReturn(remainingRecords).when(medicalRecordService).listByPrisonerId(1L);
        when(prisonerService.updateById(any(Prisoner.class))).thenReturn(true);

        medicalRecordService.deleteMedicalRecord(1L);

        verify(prisonerService, times(1)).updateById(any(Prisoner.class));
        verify(prisonerService, never()).update(any(LambdaUpdateWrapper.class));
    }

    @Test
    @DisplayName("新增第一条记录 - 健康状态从null变为治疗中")
    void testCreateFirstRecord_HealthStatusChangesFromNull() {
        Prisoner prisonerWithNullHealth = new Prisoner();
        prisonerWithNullHealth.setId(1L);
        prisonerWithNullHealth.setName("测试人员");
        prisonerWithNullHealth.setPrisonerNumber("P20240001");
        prisonerWithNullHealth.setHealthStatus(null);

        MedicalRecord newRecord = new MedicalRecord();
        newRecord.setPrisonerId(1L);
        newRecord.setRecordDate(LocalDate.now());
        newRecord.setMedicalType("OUTPATIENT");
        newRecord.setResult("TREATING");
        newRecord.setDiagnosis("感冒");

        List<MedicalRecord> recordsAfterCreate = new ArrayList<>();
        MedicalRecord saved = new MedicalRecord();
        saved.setId(1L);
        saved.setPrisonerId(1L);
        saved.setResult("TREATING");
        saved.setMedicalType("OUTPATIENT");
        recordsAfterCreate.add(saved);

        when(prisonerService.getById(1L)).thenReturn(prisonerWithNullHealth);
        doReturn(true).when(medicalRecordService).save(any(MedicalRecord.class));
        doReturn(recordsAfterCreate).when(medicalRecordService).listByPrisonerId(1L);
        when(prisonerService.updateById(any(Prisoner.class))).thenReturn(true);

        medicalRecordService.createMedicalRecord(newRecord);

        ArgumentCaptor<Prisoner> prisonerCaptor = ArgumentCaptor.forClass(Prisoner.class);
        verify(prisonerService).updateById(prisonerCaptor.capture());

        Prisoner updatedPrisoner = prisonerCaptor.getValue();
        assertEquals("持续治疗中", updatedPrisoner.getHealthStatus());
    }

    @Test
    @DisplayName("新增医疗记录 - 服刑人员不存在抛异常")
    void testCreateRecord_PrisonerNotFound_ThrowsException() {
        MedicalRecord newRecord = new MedicalRecord();
        newRecord.setPrisonerId(999L);

        when(prisonerService.getById(999L)).thenReturn(null);

        assertThrows(com.prison.config.BusinessException.class,
                () -> medicalRecordService.createMedicalRecord(newRecord));

        verify(medicalRecordService, never()).save(any());
    }

    @Test
    @DisplayName("更新医疗记录 - 服刑人员变更时更新双方健康状态")
    void testUpdateRecord_PrisonerChanged_UpdatesBothHealthStatus() {
        MedicalRecord existingRecord = new MedicalRecord();
        existingRecord.setId(1L);
        existingRecord.setPrisonerId(1L);
        existingRecord.setResult("TREATING");

        MedicalRecord updateRecord = new MedicalRecord();
        updateRecord.setId(1L);
        updateRecord.setPrisonerId(2L);
        updateRecord.setResult("RECOVERED");

        Prisoner oldPrisoner = new Prisoner();
        oldPrisoner.setId(1L);
        oldPrisoner.setName("旧服刑人员");
        oldPrisoner.setPrisonerNumber("P001");
        oldPrisoner.setHealthStatus("持续治疗中");

        Prisoner newPrisoner = new Prisoner();
        newPrisoner.setId(2L);
        newPrisoner.setName("新服刑人员");
        newPrisoner.setPrisonerNumber("P002");
        newPrisoner.setHealthStatus(null);

        List<MedicalRecord> oldPrisonerRecords = Collections.emptyList();
        List<MedicalRecord> newPrisonerRecords = new ArrayList<>();
        MedicalRecord r = new MedicalRecord();
        r.setResult("RECOVERED");
        r.setMedicalType("OUTPATIENT");
        newPrisonerRecords.add(r);

        doReturn(existingRecord).when(medicalRecordService).getById(1L);
        when(prisonerService.getById(2L)).thenReturn(newPrisoner);
        when(prisonerService.getById(1L)).thenReturn(oldPrisoner);
        doReturn(true).when(medicalRecordService).updateById(any(MedicalRecord.class));
        doReturn(oldPrisonerRecords).when(medicalRecordService).listByPrisonerId(1L);
        doReturn(newPrisonerRecords).when(medicalRecordService).listByPrisonerId(2L);
        when(prisonerService.updateById(any(Prisoner.class))).thenReturn(true);

        medicalRecordService.updateMedicalRecord(1L, updateRecord);

        verify(prisonerService, times(2)).updateById(any(Prisoner.class));
        verify(sysLogService, times(3)).logSuccess(any(), any(), anyString(), anyString(), any(), anyString());
    }
}
