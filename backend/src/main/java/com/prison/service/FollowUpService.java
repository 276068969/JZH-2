package com.prison.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.prison.dto.FollowUpMarkDTO;
import com.prison.dto.FollowUpQueryDTO;
import com.prison.vo.FollowUpStatsVO;
import com.prison.vo.FollowUpWorkbenchVO;

public interface FollowUpService {

    Page<FollowUpWorkbenchVO> pageFollowUpWorkbench(FollowUpQueryDTO queryDTO);

    FollowUpStatsVO getFollowUpStats();

    void markFollowUp(FollowUpMarkDTO dto);
}
