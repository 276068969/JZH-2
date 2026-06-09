package com.prison.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.prison.entity.Prisoner;

public interface PrisonerService extends IService<Prisoner> {
    Page<Prisoner> pagePrisoners(int page, int size, String keyword);
}