package com.prison.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.prison.dto.PrisonerTransferQueryDTO;
import com.prison.entity.PrisonerTransfer;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface PrisonerTransferMapper extends BaseMapper<PrisonerTransfer> {

    Page<PrisonerTransfer> pageTransfers(Page<PrisonerTransfer> page, @Param("q") PrisonerTransferQueryDTO queryDTO);
}
