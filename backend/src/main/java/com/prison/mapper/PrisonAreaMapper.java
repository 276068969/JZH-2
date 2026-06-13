package com.prison.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.prison.entity.PrisonArea;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface PrisonAreaMapper extends BaseMapper<PrisonArea> {

    @Select("SELECT COUNT(*) FROM prisoners WHERE area_id = #{areaId} AND deleted = 0")
    int countByAreaId(@Param("areaId") Long areaId);
}