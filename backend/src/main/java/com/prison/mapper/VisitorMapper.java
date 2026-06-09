package com.prison.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.prison.entity.Visitor;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface VisitorMapper extends BaseMapper<Visitor> {

    @Select("SELECT COUNT(*) FROM visitors WHERE visit_date = CURDATE() AND deleted = 0")
    Long countTodayVisitors();
}