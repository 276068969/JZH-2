package com.prison.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.prison.config.BusinessException;
import com.prison.entity.PrisonArea;
import com.prison.mapper.PrisonAreaMapper;
import com.prison.mapper.PrisonerMapper;
import com.prison.service.PrisonAreaService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PrisonAreaServiceImpl extends ServiceImpl<PrisonAreaMapper, PrisonArea> implements PrisonAreaService {

    private final PrisonerMapper prisonerMapper;

    @Override
    public Page<PrisonArea> pagePrisonAreas(int page, int size, String keyword) {
        LambdaQueryWrapper<PrisonArea> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(keyword)) {
            wrapper.like(PrisonArea::getAreaName, keyword)
                    .or()
                    .like(PrisonArea::getAreaCode, keyword);
        }
        wrapper.orderByDesc(PrisonArea::getCreateTime);
        return page(new Page<>(page, size), wrapper);
    }

    @Override
    @Transactional
    public void createPrisonArea(PrisonArea prisonArea) {
        prisonArea.setCurrentPopulation(0);
        if (!StringUtils.hasText(prisonArea.getStatus())) {
            prisonArea.setStatus("ACTIVE");
        }
        if (prisonArea.getCapacity() == null || prisonArea.getCapacity() < 0) {
            prisonArea.setCapacity(0);
        }
        save(prisonArea);
    }

    @Override
    @Transactional
    public void updatePrisonArea(Long id, PrisonArea prisonArea) {
        PrisonArea existing = getById(id);
        if (existing == null) {
            throw new BusinessException("监区不存在");
        }

        int realCount = prisonerMapper.countByAreaId(id);

        if (prisonArea.getCapacity() != null) {
            if (prisonArea.getCapacity() < 0) {
                throw new BusinessException("监区容量不能小于0");
            }
            if (prisonArea.getCapacity() < realCount) {
                throw new BusinessException("容量(" + prisonArea.getCapacity() + ")不能小于实际在押人数(" + realCount + "人)");
            }
            existing.setCapacity(prisonArea.getCapacity());
        }

        if (StringUtils.hasText(prisonArea.getAreaName())) {
            existing.setAreaName(prisonArea.getAreaName());
        }
        if (StringUtils.hasText(prisonArea.getAreaCode())) {
            existing.setAreaCode(prisonArea.getAreaCode());
        }
        if (StringUtils.hasText(prisonArea.getAreaType())) {
            existing.setAreaType(prisonArea.getAreaType());
        }
        if (StringUtils.hasText(prisonArea.getAddress())) {
            existing.setAddress(prisonArea.getAddress());
        }
        if (StringUtils.hasText(prisonArea.getDescription())) {
            existing.setDescription(prisonArea.getDescription());
        }
        if (StringUtils.hasText(prisonArea.getStatus())) {
            if ("INACTIVE".equals(prisonArea.getStatus()) && realCount > 0) {
                throw new BusinessException("监区内仍有实际在押人员(" + realCount + "人)，无法停用");
            }
            existing.setStatus(prisonArea.getStatus());
        }

        updateById(existing);
        syncPopulation(id);
    }

    @Override
    @Transactional
    public void deletePrisonArea(Long id) {
        PrisonArea prisonArea = getById(id);
        if (prisonArea == null) {
            throw new BusinessException("监区不存在");
        }
        int realCount = prisonerMapper.countByAreaId(id);
        if (realCount > 0) {
            throw new BusinessException("监区内仍有实际在押人员(" + realCount + "人)，无法删除");
        }
        removeById(id);
    }

    @Override
    @Transactional
    public void syncPopulation(Long areaId) {
        PrisonArea prisonArea = getById(areaId);
        if (prisonArea == null) {
            throw new BusinessException("监区不存在");
        }
        int actualCount = prisonerMapper.countByAreaId(areaId);
        prisonArea.setCurrentPopulation(actualCount);
        updateById(prisonArea);
    }

    @Override
    @Transactional
    public void syncAllPopulation() {
        List<PrisonArea> areas = list();
        for (PrisonArea area : areas) {
            int actualCount = prisonerMapper.countByAreaId(area.getId());
            area.setCurrentPopulation(actualCount);
        }
        updateBatchById(areas);
    }

    @Override
    @Transactional
    public void incrementPopulation(Long areaId) {
        syncPopulation(areaId);
    }

    @Override
    @Transactional
    public void decrementPopulation(Long areaId) {
        PrisonArea prisonArea = getById(areaId);
        if (prisonArea == null) {
            return;
        }
        syncPopulation(areaId);
    }
}
