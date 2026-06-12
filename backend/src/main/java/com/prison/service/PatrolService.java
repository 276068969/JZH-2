package com.prison.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.prison.dto.PatrolAbnormalQueryDTO;
import com.prison.entity.Patrol;
import com.prison.vo.PatrolAbnormalSummaryVO;

public interface PatrolService extends IService<Patrol> {
    Page<Patrol> pagePatrols(int page, int size, String keyword);

    PatrolAbnormalSummaryVO abnormalSummary(PatrolAbnormalQueryDTO query);
}