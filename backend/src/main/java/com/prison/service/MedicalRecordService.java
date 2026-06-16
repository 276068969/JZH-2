package com.prison.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.prison.entity.MedicalRecord;
import com.prison.vo.MedicalTimelineVO;

import java.util.List;

public interface MedicalRecordService extends IService<MedicalRecord> {
    Page<MedicalRecord> pageMedicalRecords(int page, int size, String keyword);

    MedicalTimelineVO getTimelineByPrisonerId(Long prisonerId);

    List<MedicalRecord> listByPrisonerId(Long prisonerId);

    void createMedicalRecord(MedicalRecord record);

    void updateMedicalRecord(Long id, MedicalRecord record);

    void deleteMedicalRecord(Long id);

    String calculateHealthStatus(Long prisonerId);

    void updatePrisonerHealthStatus(Long prisonerId);
}