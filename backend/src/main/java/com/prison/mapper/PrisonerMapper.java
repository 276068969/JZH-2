package com.prison.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.prison.entity.Prisoner;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

@Mapper
public interface PrisonerMapper extends BaseMapper<Prisoner> {

    @Select("SELECT COUNT(*) FROM prisoners WHERE cell_id = #{cellId} AND deleted = 0")
    int countByCellId(@Param("cellId") Long cellId);

    @Select("SELECT COUNT(*) FROM prisoners WHERE area_id = #{areaId} AND deleted = 0")
    int countByAreaId(@Param("areaId") Long areaId);

    @Select("SELECT MONTH(entry_date) AS m, COUNT(*) AS cnt FROM prisoners WHERE YEAR(entry_date) = #{year} AND deleted = 0 GROUP BY MONTH(entry_date) ORDER BY m")
    List<Map<String, Object>> countNewEntriesByMonth(@Param("year") int year);

    @Select("SELECT MONTH(update_time) AS m, COUNT(*) AS cnt FROM prisoners WHERE status = 'RELEASED' AND YEAR(update_time) = #{year} AND deleted = 0 GROUP BY MONTH(update_time) ORDER BY m")
    List<Map<String, Object>> countReleasesByMonth(@Param("year") int year);

    @Select("SELECT DATE_FORMAT(entry_date, '%Y-%m') AS ym, COUNT(*) AS cnt FROM prisoners WHERE entry_date <= CONCAT(#{year}, '-', LPAD(#{month}, 2, '0'), '-31') AND status != 'RELEASED' AND deleted = 0 GROUP BY DATE_FORMAT(entry_date, '%Y-%m') ORDER BY ym")
    List<Map<String, Object>> countPrisonersByMonth(@Param("year") int year, @Param("month") int month);

    @Select("SELECT pa.area_name FROM prison_areas pa WHERE pa.id = #{areaId} AND pa.deleted = 0")
    String getAreaNameById(@Param("areaId") Long areaId);
}
