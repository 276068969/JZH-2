package com.prison.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.prison.entity.Incident;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface IncidentMapper extends BaseMapper<Incident> {

    @Select("SELECT COUNT(*) FROM incidents WHERE status IN ('PENDING', 'PROCESSING') AND deleted = 0")
    Long countPendingIncidents();
}