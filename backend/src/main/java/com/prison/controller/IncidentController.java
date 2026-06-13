package com.prison.controller;

import com.prison.Result;
import com.prison.dto.IncidentDTO;
import com.prison.dto.IncidentResolveDTO;
import com.prison.entity.Incident;
import com.prison.enums.IncidentStatus;
import com.prison.service.IncidentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/incidents")
@RequiredArgsConstructor
public class IncidentController {

    private final IncidentService incidentService;

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'GUARD', 'VIEWER')")
    public Result<?> list(@RequestParam(defaultValue = "1") int page,
                          @RequestParam(defaultValue = "10") int size,
                          @RequestParam(required = false) String keyword) {
        return Result.success(incidentService.pageIncidents(page, size, keyword));
    }

    @GetMapping("/all")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'GUARD', 'VIEWER')")
    public Result<List<Incident>> all() {
        return Result.success(incidentService.list());
    }

    @GetMapping("/statuses")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'GUARD', 'VIEWER')")
    public Result<?> getStatuses() {
        List<Map<String, Object>> statuses = Arrays.stream(IncidentStatus.values())
                .map(status -> Map.of(
                        "code", status.name(),
                        "description", status.getDescription(),
                        "order", status.getOrder()
                ))
                .collect(Collectors.toList());
        return Result.success(statuses);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'GUARD', 'VIEWER')")
    public Result<Incident> getById(@PathVariable Long id) {
        return Result.success(incidentService.getById(id));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'GUARD')")
    public Result<?> create(@Valid @RequestBody IncidentDTO dto) {
        incidentService.createIncident(dto);
        return Result.success("创建成功");
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'GUARD')")
    public Result<?> update(@PathVariable Long id, @Valid @RequestBody IncidentDTO dto) {
        incidentService.updateIncident(id, dto);
        return Result.success("更新成功");
    }

    @PutMapping("/{id}/start-processing")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'GUARD')")
    public Result<Incident> startProcessing(@PathVariable Long id) {
        return Result.success(incidentService.startProcessing(id));
    }

    @PutMapping("/{id}/resolve")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'GUARD')")
    public Result<Incident> resolve(@PathVariable Long id, @Valid @RequestBody IncidentResolveDTO dto) {
        return Result.success(incidentService.resolve(id, dto.getHandlerResult()));
    }

    @PutMapping("/{id}/close")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public Result<Incident> close(@PathVariable Long id) {
        return Result.success(incidentService.close(id));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<?> delete(@PathVariable Long id) {
        incidentService.removeById(id);
        return Result.success("删除成功");
    }
}