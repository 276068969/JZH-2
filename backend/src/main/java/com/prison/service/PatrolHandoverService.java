package com.prison.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.prison.dto.PatrolHandoverDTO;
import com.prison.dto.PatrolHandoverQueryDTO;
import com.prison.entity.PatrolHandover;

import java.util.List;

public interface PatrolHandoverService extends IService<PatrolHandover> {

    Page<PatrolHandover> pageHandovers(PatrolHandoverQueryDTO query);

    PatrolHandover createHandover(PatrolHandoverDTO dto);

    PatrolHandover confirmHandover(Long id, Long incomingGuardId, String incomingGuardName);

    PatrolHandover getLatestHandoverByArea(Long areaId);

    List<PatrolHandover> getRecentHandovers(Long areaId, Integer limit);
}
