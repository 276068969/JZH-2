package com.prison.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.prison.entity.PrisonArea;

import java.util.List;

public interface PrisonAreaService extends IService<PrisonArea> {
    Page<PrisonArea> pagePrisonAreas(int page, int size, String keyword);

    List<com.prison.vo.PrisonAreaStatsVO> listAreaStats();

    com.prison.vo.PrisonAreaStatsVO getAreaStatsById(Long id);

    void createPrisonArea(PrisonArea prisonArea);

    void updatePrisonArea(Long id, PrisonArea prisonArea);

    void deletePrisonArea(Long id);

    void syncPopulation(Long areaId);

    void syncAllPopulation();

    void incrementPopulation(Long areaId);

    void decrementPopulation(Long areaId);

    com.prison.vo.CapacityWarningVO getCapacityWarnings();

    com.prison.vo.CapacityWarningVO getCapacityWarningsByAreaId(Long areaId);
}
