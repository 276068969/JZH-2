package com.prison.controller;

import com.prison.Result;
import com.prison.dto.MedicalRecordDTO;
import com.prison.entity.MedicalRecord;
import com.prison.service.MedicalRecordService;
import com.prison.vo.MedicalTimelineVO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/medical-records")
@RequiredArgsConstructor
public class MedicalRecordController {

    private final MedicalRecordService medicalRecordService;

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'DOCTOR', 'VIEWER')")
    public Result<?> list(@RequestParam(defaultValue = "1") int page,
                          @RequestParam(defaultValue = "10") int size,
                          @RequestParam(required = false) String keyword) {
        return Result.success(medicalRecordService.pageMedicalRecords(page, size, keyword));
    }

    @GetMapping("/all")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'DOCTOR', 'VIEWER')")
    public Result<List<MedicalRecord>> all() {
        return Result.success(medicalRecordService.list());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'DOCTOR', 'VIEWER')")
    public Result<MedicalRecord> getById(@PathVariable Long id) {
        return Result.success(medicalRecordService.getById(id));
    }

    @GetMapping("/prisoner/{prisonerId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'DOCTOR', 'VIEWER')")
    public Result<List<MedicalRecord>> listByPrisonerId(@PathVariable Long prisonerId) {
        return Result.success(medicalRecordService.listByPrisonerId(prisonerId));
    }

    @GetMapping("/timeline/{prisonerId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'DOCTOR', 'VIEWER')")
    public Result<MedicalTimelineVO> timeline(@PathVariable Long prisonerId) {
        return Result.success(medicalRecordService.getTimelineByPrisonerId(prisonerId));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'DOCTOR')")
    public Result<?> create(@Valid @RequestBody MedicalRecordDTO dto) {
        MedicalRecord record = new MedicalRecord();
        BeanUtils.copyProperties(dto, record);
        medicalRecordService.createMedicalRecord(record);
        return Result.success("创建成功");
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'DOCTOR')")
    public Result<?> update(@PathVariable Long id, @Valid @RequestBody MedicalRecordDTO dto) {
        MedicalRecord record = new MedicalRecord();
        BeanUtils.copyProperties(dto, record);
        medicalRecordService.updateMedicalRecord(id, record);
        return Result.success("更新成功");
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public Result<?> delete(@PathVariable Long id) {
        medicalRecordService.deleteMedicalRecord(id);
        return Result.success("删除成功");
    }
}