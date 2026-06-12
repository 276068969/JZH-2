package com.prison.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.prison.entity.Patrol;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

@Mapper
public interface PatrolMapper extends BaseMapper<Patrol> {

    @Select("SELECT COUNT(*) FROM patrols WHERE DATE(patrol_time) = CURDATE() AND deleted = 0")
    Long countTodayPatrols();

    @Select("SELECT DAYOFWEEK(patrol_time) AS dow, DATE(patrol_time) AS dt, COUNT(*) AS cnt FROM patrols WHERE YEARWEEK(patrol_time, 1) = YEARWEEK(CURDATE(), 1) AND deleted = 0 GROUP BY DAYOFWEEK(patrol_time), DATE(patrol_time) ORDER BY dt")
    List<Map<String, Object>> countByDayThisWeek();
}
