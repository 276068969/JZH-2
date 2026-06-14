package com.prison.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.prison.dto.PatrolHandoverQueryDTO;
import com.prison.entity.PatrolHandover;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface PatrolHandoverMapper extends BaseMapper<PatrolHandover> {

    Page<PatrolHandover> pageHandovers(Page<?> page, @Param("q") PatrolHandoverQueryDTO query);

    @Select("SELECT * FROM patrol_handovers WHERE area_id = #{areaId} AND deleted = 0 ORDER BY handover_time DESC LIMIT 1")
    PatrolHandover getLatestHandoverByArea(@Param("areaId") Long areaId);
}
