package com.prison.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.prison.dto.ObservationListQueryDTO;
import com.prison.vo.ObservationListVO;
import com.prison.vo.ObservationStatsVO;

public interface ObservationListService {

    Page<ObservationListVO> pageObservationList(ObservationListQueryDTO queryDTO);

    ObservationStatsVO getObservationStats(Integer incidentDaysThreshold);
}
