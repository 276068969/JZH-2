package com.prison.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.prison.entity.Prisoner;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface PrisonerMapper extends BaseMapper<Prisoner> {
}