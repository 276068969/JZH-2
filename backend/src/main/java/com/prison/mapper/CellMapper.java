package com.prison.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.prison.entity.Cell;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

@Mapper
public interface CellMapper extends BaseMapper<Cell> {

    @Select("SELECT status, COUNT(*) AS cnt FROM cells WHERE deleted = 0 GROUP BY status")
    List<Map<String, Object>> countByStatus();

    @Select("SELECT status, COUNT(*) AS cnt FROM cells WHERE deleted = 0 AND area_id = #{areaId} GROUP BY status")
    List<Map<String, Object>> countByStatusAndAreaId(Long areaId);
}
