package com.prison.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.prison.dto.PatrolAbnormalQueryDTO;
import com.prison.entity.Patrol;
import com.prison.mapper.PatrolMapper;
import com.prison.service.PatrolService;
import com.prison.vo.PatrolAbnormalSummaryVO;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
public class PatrolServiceImpl extends ServiceImpl<PatrolMapper, Patrol> implements PatrolService {

    @Override
    public Page<Patrol> pagePatrols(int page, int size, String keyword) {
        LambdaQueryWrapper<Patrol> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(keyword)) {
            wrapper.like(Patrol::getPatrolType, keyword)
                    .or()
                    .like(Patrol::getResult, keyword);
        }
        wrapper.orderByDesc(Patrol::getPatrolTime);
        return page(new Page<>(page, size), wrapper);
    }

    @Override
    public PatrolAbnormalSummaryVO abnormalSummary(PatrolAbnormalQueryDTO query) {
        PatrolAbnormalSummaryVO vo = new PatrolAbnormalSummaryVO();

        vo.setTotal(baseMapper.countAbnormal(query));
        vo.setByType(baseMapper.groupByPatrolType(query));
        vo.setByArea(baseMapper.groupByArea(query));
        vo.setByGuard(baseMapper.groupByGuard(query));
        vo.setByHour(baseMapper.groupByHour(query));

        Page<PatrolAbnormalSummaryVO.AbnormalRecord> page = baseMapper.pageAbnormalRecords(
                new Page<>(query.getPage(), query.getSize()), query);

        PatrolAbnormalSummaryVO.PageData<PatrolAbnormalSummaryVO.AbnormalRecord> pageData =
                new PatrolAbnormalSummaryVO.PageData<>();
        pageData.setList(page.getRecords());
        pageData.setTotal(page.getTotal());
        pageData.setPage(query.getPage());
        pageData.setSize(query.getSize());
        vo.setRecords(pageData);

        return vo;
    }
}