package com.prison.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.prison.entity.Patrol;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface PatrolMapper extends BaseMapper<Patrol> {

    @Select("SELECT COUNT(*) FROM patrols WHERE DATE(patrol_time) = CURDATE() AND deleted = 0")
    Long countTodayPatrols();
}