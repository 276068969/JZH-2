package com.prison.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.prison.config.BusinessException;
import com.prison.dto.PrisonerTransferDTO;
import com.prison.dto.PrisonerTransferQueryDTO;
import com.prison.entity.Cell;
import com.prison.entity.PrisonArea;
import com.prison.entity.Prisoner;
import com.prison.entity.PrisonerTransfer;
import com.prison.enums.SysLogAction;
import com.prison.enums.SysLogModule;
import com.prison.mapper.PrisonerTransferMapper;
import com.prison.service.CellService;
import com.prison.service.PrisonAreaService;
import com.prison.service.PrisonerService;
import com.prison.service.PrisonerTransferService;
import com.prison.service.SysLogService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class PrisonerTransferServiceImpl extends ServiceImpl<PrisonerTransferMapper, PrisonerTransfer> implements PrisonerTransferService {

    private final PrisonerService prisonerService;
    private final CellService cellService;
    private final PrisonAreaService prisonAreaService;
    private final SysLogService sysLogService;

    @Override
    public Page<PrisonerTransfer> pageTransfers(PrisonerTransferQueryDTO queryDTO) {
        int pageNum = queryDTO.getPage() != null ? queryDTO.getPage() : 1;
        int pageSize = queryDTO.getSize() != null ? queryDTO.getSize() : 10;
        Page<PrisonerTransfer> page = new Page<>(pageNum, pageSize);
        return baseMapper.pageTransfers(page, queryDTO);
    }

    @Override
    public PrisonerTransfer getTransferById(Long id) {
        PrisonerTransfer transfer = getById(id);
        if (transfer == null) {
            throw new BusinessException("调动记录不存在");
        }
        return transfer;
    }

    @Override
    public List<PrisonerTransfer> getTransfersByPrisonerId(Long prisonerId) {
        LambdaQueryWrapper<PrisonerTransfer> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(PrisonerTransfer::getPrisonerId, prisonerId)
                .orderByDesc(PrisonerTransfer::getTransferTime)
                .orderByDesc(PrisonerTransfer::getId);
        return list(wrapper);
    }

    @Override
    @Transactional
    public void createTransfer(PrisonerTransferDTO dto) {
        Prisoner prisoner = prisonerService.getById(dto.getPrisonerId());
        if (prisoner == null) {
            throw new BusinessException("服刑人员不存在");
        }

        Long fromAreaId = prisoner.getAreaId();
        Long fromCellId = prisoner.getCellId();
        Long toAreaId = dto.getToAreaId();
        Long toCellId = dto.getToCellId();

        if (toAreaId == null && toCellId == null) {
            throw new BusinessException("新监区和新监舍不能同时为空");
        }

        if (toCellId != null) {
            Cell toCell = cellService.getById(toCellId);
            if (toCell == null) {
                throw new BusinessException("新监舍不存在");
            }
            if (toAreaId == null) {
                toAreaId = toCell.getAreaId();
            }
            if (!toAreaId.equals(toCell.getAreaId())) {
                throw new BusinessException("新监舍所属监区与指定新监区不一致");
            }
            if (!toCellId.equals(fromCellId)) {
                cellService.validateCanAssign(toCellId);
            }
        }

        if (toAreaId != null) {
            PrisonArea toArea = prisonAreaService.getById(toAreaId);
            if (toArea == null) {
                throw new BusinessException("新监区不存在");
            }
        }

        boolean areaChanged = !idsEqual(fromAreaId, toAreaId);
        boolean cellChanged = !idsEqual(fromCellId, toCellId);

        if (!areaChanged && !cellChanged) {
            throw new BusinessException("新位置与原位置相同，无需调动");
        }

        String transferType = determineTransferType(areaChanged, cellChanged);

        PrisonArea fromArea = fromAreaId != null ? prisonAreaService.getById(fromAreaId) : null;
        Cell fromCell = fromCellId != null ? cellService.getById(fromCellId) : null;
        PrisonArea toArea = toAreaId != null ? prisonAreaService.getById(toAreaId) : null;
        Cell toCell = toCellId != null ? cellService.getById(toCellId) : null;

        PrisonerTransfer transfer = new PrisonerTransfer();
        transfer.setPrisonerId(prisoner.getId());
        transfer.setPrisonerNumber(prisoner.getPrisonerNumber());
        transfer.setPrisonerName(prisoner.getName());

        transfer.setFromAreaId(fromAreaId);
        transfer.setFromAreaName(fromArea != null ? fromArea.getAreaName() : null);
        transfer.setFromCellId(fromCellId);
        transfer.setFromCellNumber(fromCell != null ? fromCell.getCellNumber() : null);

        transfer.setToAreaId(toAreaId);
        transfer.setToAreaName(toArea != null ? toArea.getAreaName() : null);
        transfer.setToCellId(toCellId);
        transfer.setToCellNumber(toCell != null ? toCell.getCellNumber() : null);

        transfer.setTransferType(transferType);
        transfer.setTransferTime(dto.getTransferTime() != null ? dto.getTransferTime() : LocalDateTime.now());
        transfer.setTransferReason(dto.getTransferReason());
        transfer.setOperatorId(dto.getOperatorId());
        transfer.setOperatorName(dto.getOperatorName());
        transfer.setRemark(dto.getRemark());

        save(transfer);

        prisoner.setAreaId(toAreaId);
        prisoner.setCellId(toCellId);
        prisonerService.updateById(prisoner);

        if (cellChanged) {
            if (fromCellId != null) {
                cellService.decrementOccupancy(fromCellId);
            }
            if (toCellId != null) {
                cellService.incrementOccupancy(toCellId);
            }
        }

        if (areaChanged) {
            if (fromAreaId != null) {
                prisonAreaService.decrementPopulation(fromAreaId);
            }
            if (toAreaId != null) {
                prisonAreaService.incrementPopulation(toAreaId);
            }
        }

        log.info("服刑人员调动成功: 姓名={}, 原监区={}, 原监舍={}, 新监区={}, 新监舍={}, 类型={}",
                prisoner.getName(),
                fromArea != null ? fromArea.getAreaName() : "无",
                fromCell != null ? fromCell.getCellNumber() : "无",
                toArea != null ? toArea.getAreaName() : "无",
                toCell != null ? toCell.getCellNumber() : "无",
                transferType);

        String transferTypeText = "BOTH".equals(transferType) ? "调监调舍"
                : "AREA_TRANSFER".equals(transferType) ? "调监" : "调舍";
        sysLogService.logSuccess(
                SysLogModule.PRISONER_TRANSFER,
                SysLogAction.TRANSFER,
                "服刑人员调动：" + prisoner.getName() + "（编号：" + prisoner.getPrisonerNumber() + "），"
                        + "类型：" + transferTypeText + "，"
                        + "原监区/监舍：" + (fromArea != null ? fromArea.getAreaName() : "无") + "/"
                        + (fromCell != null ? fromCell.getCellNumber() : "无") + " → "
                        + "新监区/监舍：" + (toArea != null ? toArea.getAreaName() : "无") + "/"
                        + (toCell != null ? toCell.getCellNumber() : "无")
                        + (dto.getTransferReason() != null ? "，原因：" + dto.getTransferReason() : ""),
                "PRISONER",
                prisoner.getId(),
                prisoner.getName()
        );
    }

    @Override
    @Transactional
    public void updateTransfer(Long id, PrisonerTransferDTO dto) {
        PrisonerTransfer existing = getById(id);
        if (existing == null) {
            throw new BusinessException("调动记录不存在");
        }

        if (StringUtils.hasText(dto.getTransferReason())) {
            existing.setTransferReason(dto.getTransferReason());
        }
        if (dto.getTransferTime() != null) {
            existing.setTransferTime(dto.getTransferTime());
        }
        if (StringUtils.hasText(dto.getRemark())) {
            existing.setRemark(dto.getRemark());
        }
        if (dto.getOperatorId() != null) {
            existing.setOperatorId(dto.getOperatorId());
        }
        if (StringUtils.hasText(dto.getOperatorName())) {
            existing.setOperatorName(dto.getOperatorName());
        }

        updateById(existing);
        log.info("调动记录更新成功: id={}", id);
    }

    @Override
    @Transactional
    public void deleteTransfer(Long id) {
        PrisonerTransfer transfer = getById(id);
        if (transfer == null) {
            throw new BusinessException("调动记录不存在");
        }

        Prisoner prisoner = prisonerService.getById(transfer.getPrisonerId());
        if (prisoner == null) {
            removeById(id);
            log.warn("服刑人员已不存在，仅删除调动记录: id={}", id);
            return;
        }

        boolean areaChanged = !idsEqual(transfer.getFromAreaId(), transfer.getToAreaId());
        boolean cellChanged = !idsEqual(transfer.getFromCellId(), transfer.getToCellId());

        boolean isLastTransfer = isLatestTransfer(prisoner.getId(), id);

        if (isLastTransfer) {
            prisoner.setAreaId(transfer.getFromAreaId());
            prisoner.setCellId(transfer.getFromCellId());
            prisonerService.updateById(prisoner);

            if (cellChanged) {
                if (transfer.getToCellId() != null) {
                    cellService.decrementOccupancy(transfer.getToCellId());
                }
                if (transfer.getFromCellId() != null) {
                    cellService.incrementOccupancy(transfer.getFromCellId());
                }
            }

            if (areaChanged) {
                if (transfer.getToAreaId() != null) {
                    prisonAreaService.decrementPopulation(transfer.getToAreaId());
                }
                if (transfer.getFromAreaId() != null) {
                    prisonAreaService.incrementPopulation(transfer.getFromAreaId());
                }
            }

            log.info("删除最近一次调动记录，已回滚服刑人员位置: 姓名={}", prisoner.getName());
        }

        removeById(id);
        log.info("调动记录删除成功: id={}", id);

        sysLogService.logSuccess(
                SysLogModule.PRISONER_TRANSFER,
                SysLogAction.DELETE,
                "删除服刑人员调动记录：" + transfer.getPrisonerName()
                        + "（编号：" + transfer.getPrisonerNumber() + "）",
                "PRISONER_TRANSFER",
                id,
                transfer.getPrisonerName()
        );
    }

    private boolean isLatestTransfer(Long prisonerId, Long transferId) {
        LambdaQueryWrapper<PrisonerTransfer> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(PrisonerTransfer::getPrisonerId, prisonerId)
                .orderByDesc(PrisonerTransfer::getTransferTime)
                .orderByDesc(PrisonerTransfer::getId)
                .last("LIMIT 1");
        PrisonerTransfer latest = getOne(wrapper);
        return latest != null && latest.getId().equals(transferId);
    }

    private String determineTransferType(boolean areaChanged, boolean cellChanged) {
        if (areaChanged && cellChanged) {
            return "BOTH";
        } else if (areaChanged) {
            return "AREA_TRANSFER";
        } else {
            return "CELL_TRANSFER";
        }
    }

    private boolean idsEqual(Long a, Long b) {
        if (a == null && b == null) return true;
        if (a == null || b == null) return false;
        return a.equals(b);
    }
}
