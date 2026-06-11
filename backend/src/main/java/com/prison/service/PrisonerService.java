package com.prison.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.prison.dto.PrisonerQueryDTO;
import com.prison.dto.ReleaseWarningVO;
import com.prison.entity.Prisoner;

import java.util.List;

public interface PrisonerService extends IService<Prisoner> {
    Page<Prisoner> pagePrisoners(int page, int size, String keyword);

    Page<Prisoner> advancedSearch(PrisonerQueryDTO queryDTO);

    List<ReleaseWarningVO> getReleaseWarnings(Integer days, String status, String dangerLevel);

    void createPrisoner(Prisoner prisoner);

    void updatePrisoner(Long id, Prisoner prisoner);

    void deletePrisoner(Long id);
}