package com.prison.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.prison.entity.Incident;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

@Mapper
public interface IncidentMapper extends BaseMapper<Incident> {

    @Select("SELECT COUNT(*) FROM incidents WHERE status IN ('PENDING', 'PROCESSING') AND deleted = 0")
    Long countPendingIncidents();

    @Select("SELECT incident_type, COUNT(*) AS cnt FROM incidents WHERE YEAR(occur_time) = YEAR(CURDATE()) AND MONTH(occur_time) = MONTH(CURDATE()) AND deleted = 0 GROUP BY incident_type")
    List<Map<String, Object>> countByTypeCurrentMonth();
}
