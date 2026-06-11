package com.prison.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.prison.entity.Prisoner;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface PrisonerMapper extends BaseMapper<Prisoner> {

    @Select("SELECT COUNT(*) FROM prisoners WHERE cell_id = #{cellId} AND deleted = 0")
    int countByCellId(@Param("cellId") Long cellId);
}