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

    @Select("SELECT id, incident_title, incident_type, severity, status, DATE_FORMAT(occur_time, '%Y-%m-%d %H:%i') AS occur_time FROM incidents WHERE related_prisoner_id = #{prisonerId} AND occur_time >= DATE_SUB(CURDATE(), INTERVAL 30 DAY) AND deleted = 0 ORDER BY occur_time DESC")
    List<Map<String, Object>> findRecentByPrisonerId(@Param("prisonerId") Long prisonerId);

    @Select("SELECT id, incident_title, incident_type, severity, status, DATE_FORMAT(occur_time, '%Y-%m-%d %H:%i') AS occur_time, related_prisoner_id FROM incidents WHERE related_prisoner_id IN (${prisonerIds}) AND occur_time >= DATE_SUB(CURDATE(), INTERVAL 30 DAY) AND deleted = 0 ORDER BY occur_time DESC")
    List<Map<String, Object>> findRecentByPrisonerIds(@Param("prisonerIds") String prisonerIds);
}
