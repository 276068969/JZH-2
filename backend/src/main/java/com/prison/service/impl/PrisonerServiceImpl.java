package com.prison.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.prison.config.BusinessException;
import com.prison.dto.PrisonerQueryDTO;
import com.prison.dto.ReleaseWarningVO;
import com.prison.entity.Prisoner;
import com.prison.enums.SysLogAction;
import com.prison.enums.SysLogModule;
import com.prison.mapper.PrisonerMapper;
import com.prison.service.CellService;
import com.prison.service.PrisonerService;
import com.prison.service.SysLogService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class PrisonerServiceImpl extends ServiceImpl<PrisonerMapper, Prisoner> implements PrisonerService {

    private final CellService cellService;
    private final SysLogService sysLogService;

    @Override
    public Page<Prisoner> pagePrisoners(int page, int size, String keyword) {
        LambdaQueryWrapper<Prisoner> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(keyword)) {
            wrapper.like(Prisoner::getName, keyword)
                    .or()
                    .like(Prisoner::getPrisonerNumber, keyword)
                    .or()
                    .like(Prisoner::getIdCard, keyword);
        }
        wrapper.orderByDesc(Prisoner::getCreateTime);
        return page(new Page<>(page, size), wrapper);
    }

    @Override
    public Page<Prisoner> advancedSearch(PrisonerQueryDTO queryDTO) {
        LambdaQueryWrapper<Prisoner> wrapper = new LambdaQueryWrapper<>();

        if (StringUtils.hasText(queryDTO.getKeyword())) {
            wrapper.and(w -> w.like(Prisoner::getName, queryDTO.getKeyword())
                    .or()
                    .like(Prisoner::getPrisonerNumber, queryDTO.getKeyword())
                    .or()
                    .like(Prisoner::getIdCard, queryDTO.getKeyword()));
        }

        if (queryDTO.getAreaId() != null) {
            wrapper.eq(Prisoner::getAreaId, queryDTO.getAreaId());
        }

        if (StringUtils.hasText(queryDTO.getDangerLevel())) {
            wrapper.eq(Prisoner::getDangerLevel, queryDTO.getDangerLevel());
        }

        if (StringUtils.hasText(queryDTO.getStatus())) {
            wrapper.eq(Prisoner::getStatus, queryDTO.getStatus());
        }

        if (StringUtils.hasText(queryDTO.getGender())) {
            wrapper.eq(Prisoner::getGender, queryDTO.getGender());
        }

        if (StringUtils.hasText(queryDTO.getCrimeType())) {
            wrapper.like(Prisoner::getCrimeType, queryDTO.getCrimeType());
        }

        if (queryDTO.getMinAge() != null || queryDTO.getMaxAge() != null) {
            LocalDate today = LocalDate.now();
            if (queryDTO.getMaxAge() != null) {
                LocalDate minBirthDate = today.minusYears(queryDTO.getMaxAge());
                wrapper.ge(Prisoner::getBirthDate, minBirthDate);
            }
            if (queryDTO.getMinAge() != null) {
                LocalDate maxBirthDate = today.minusYears(queryDTO.getMinAge());
                wrapper.le(Prisoner::getBirthDate, maxBirthDate);
            }
        }

        wrapper.orderByDesc(Prisoner::getCreateTime);

        int pageNum = queryDTO.getPage() != null ? queryDTO.getPage() : 1;
        int pageSize = queryDTO.getSize() != null ? queryDTO.getSize() : 10;

        return page(new Page<>(pageNum, pageSize), wrapper);
    }

    @Override
    public List<ReleaseWarningVO> getReleaseWarnings(Integer days, String status, String dangerLevel) {
        List<ReleaseWarningVO> result = new ArrayList<>();

        if (days != null && days > 0) {
            ReleaseWarningVO vo = buildWarningVO(days, status, dangerLevel);
            result.add(vo);
        } else {
            result.add(buildWarningVO(30, status, dangerLevel));
            result.add(buildWarningVO(60, status, dangerLevel));
            result.add(buildWarningVO(90, status, dangerLevel));
        }

        return result;
    }

    @Override
    @Transactional
    public void createPrisoner(Prisoner prisoner) {
        if (prisoner.getCellId() != null) {
            cellService.validateCanAssign(prisoner.getCellId());
            save(prisoner);
            cellService.incrementOccupancy(prisoner.getCellId());
        } else {
            save(prisoner);
        }

        sysLogService.logSuccess(
                SysLogModule.PRISONER,
                SysLogAction.CREATE,
                "新增服刑人员：" + prisoner.getName() + "（编号：" + prisoner.getPrisonerNumber() + "）",
                "PRISONER",
                prisoner.getId(),
                prisoner.getName()
        );
    }

    @Override
    @Transactional
    public void updatePrisoner(Long id, Prisoner prisoner) {
        Prisoner existing = getById(id);
        if (existing == null) {
            throw new BusinessException("服刑人员不存在");
        }

        Long oldCellId = existing.getCellId();
        Long newCellId = prisoner.getCellId();

        boolean cellChanged = !cellIdsEqual(oldCellId, newCellId);

        if (cellChanged) {
            if (newCellId != null) {
                cellService.validateCanAssign(newCellId);
            }
            prisoner.setId(id);
            updateById(prisoner);
            if (oldCellId != null) {
                cellService.decrementOccupancy(oldCellId);
            }
            if (newCellId != null) {
                cellService.incrementOccupancy(newCellId);
            }
        } else {
            prisoner.setId(id);
            updateById(prisoner);
        }

        sysLogService.logSuccess(
                SysLogModule.PRISONER,
                SysLogAction.UPDATE,
                "修改服刑人员信息：" + existing.getName() + "（编号：" + existing.getPrisonerNumber() + "）",
                "PRISONER",
                id,
                existing.getName()
        );
    }

    @Override
    @Transactional
    public void deletePrisoner(Long id) {
        Prisoner existing = getById(id);
        if (existing == null) {
            throw new BusinessException("服刑人员不存在");
        }

        Long cellId = existing.getCellId();
        String prisonerName = existing.getName();
        String prisonerNumber = existing.getPrisonerNumber();
        removeById(id);

        if (cellId != null) {
            cellService.decrementOccupancy(cellId);
        }

        sysLogService.logSuccess(
                SysLogModule.PRISONER,
                SysLogAction.DELETE,
                "删除服刑人员：" + prisonerName + "（编号：" + prisonerNumber + "）",
                "PRISONER",
                id,
                prisonerName
        );
    }

    private boolean cellIdsEqual(Long a, Long b) {
        if (a == null && b == null) return true;
        if (a == null || b == null) return false;
        return a.equals(b);
    }

    private ReleaseWarningVO buildWarningVO(int days, String status, String dangerLevel) {
        LocalDate today = LocalDate.now();
        LocalDate endDate = today.plusDays(days);

        LambdaQueryWrapper<Prisoner> wrapper = new LambdaQueryWrapper<>();
        wrapper.isNotNull(Prisoner::getReleaseDate)
                .ge(Prisoner::getReleaseDate, today)
                .le(Prisoner::getReleaseDate, endDate);

        if (StringUtils.hasText(status)) {
            wrapper.eq(Prisoner::getStatus, status);
        } else {
            wrapper.in(Prisoner::getStatus, "INCARCERATED", "TRANSFERRED", "MEDICAL_PAROLE");
        }

        if (StringUtils.hasText(dangerLevel)) {
            wrapper.eq(Prisoner::getDangerLevel, dangerLevel);
        }

        wrapper.orderByAsc(Prisoner::getReleaseDate);

        List<Prisoner> prisoners = list(wrapper);

        log.info("临释预警查询: {}天, today={}, endDate={}, status={}, dangerLevel={}, 匹配人数={}",
                days, today, endDate, status, dangerLevel, prisoners.size());

        String label = days + "天内临释人员";
        return new ReleaseWarningVO(days, label, prisoners.size(), prisoners);
    }
}
