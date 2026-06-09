package com.prison.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.prison.entity.Patrol;

public interface PatrolService extends IService<Patrol> {
    Page<Patrol> pagePatrols(int page, int size, String keyword);
}