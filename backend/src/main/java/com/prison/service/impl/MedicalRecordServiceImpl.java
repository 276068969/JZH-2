package com.prison.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.prison.entity.MedicalRecord;
import com.prison.mapper.MedicalRecordMapper;
import com.prison.service.MedicalRecordService;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
public class MedicalRecordServiceImpl extends ServiceImpl<MedicalRecordMapper, MedicalRecord> implements MedicalRecordService {

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
}