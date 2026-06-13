package com.prison.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.prison.dto.PrisonerTransferDTO;
import com.prison.dto.PrisonerTransferQueryDTO;
import com.prison.entity.PrisonerTransfer;

import java.util.List;

public interface PrisonerTransferService extends IService<PrisonerTransfer> {

    Page<PrisonerTransfer> pageTransfers(PrisonerTransferQueryDTO queryDTO);

    PrisonerTransfer getTransferById(Long id);

    List<PrisonerTransfer> getTransfersByPrisonerId(Long prisonerId);

    void createTransfer(PrisonerTransferDTO dto);

    void updateTransfer(Long id, PrisonerTransferDTO dto);

    void deleteTransfer(Long id);
}
