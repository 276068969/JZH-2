package com.prison.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.prison.entity.PrisonArea;

public interface PrisonAreaService extends IService<PrisonArea> {
    Page<PrisonArea> pagePrisonAreas(int page, int size, String keyword);
}