package com.prison.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.prison.dto.IncidentDTO;
import com.prison.entity.Incident;

public interface IncidentService extends IService<Incident> {
    Page<Incident> pageIncidents(int page, int size, String keyword);

    void validateStatusTransition(Incident incident, String targetStatus);

    Incident startProcessing(Long id);

    Incident resolve(Long id, String handlerResult);

    Incident close(Long id);

    void updateIncident(Long id, IncidentDTO dto);

    void createIncident(IncidentDTO dto);
}