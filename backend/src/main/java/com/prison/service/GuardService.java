package com.prison.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.prison.entity.Guard;

public interface GuardService extends IService<Guard> {
    Page<Guard> pageGuards(int page, int size, String keyword);
}