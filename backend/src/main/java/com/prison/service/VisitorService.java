package com.prison.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.prison.entity.Visitor;

public interface VisitorService extends IService<Visitor> {
    Page<Visitor> pageVisitors(int page, int size, String keyword);
}