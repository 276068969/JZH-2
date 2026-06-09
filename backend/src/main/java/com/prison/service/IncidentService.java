package com.prison.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.prison.entity.Incident;

public interface IncidentService extends IService<Incident> {
    Page<Incident> pageIncidents(int page, int size, String keyword);
}