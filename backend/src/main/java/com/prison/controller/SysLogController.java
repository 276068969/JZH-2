package com.prison.controller;

import com.prison.Result;
import com.prison.dto.SysLogQueryDTO;
import com.prison.entity.SysLog;
import com.prison.enums.SysLogAction;
import com.prison.enums.SysLogModule;
import com.prison.service.SysLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/sys-logs")
@RequiredArgsConstructor
public class SysLogController {

    private final SysLogService sysLogService;

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'VIEWER')")
    public Result<?> page(SysLogQueryDTO queryDTO) {
        return Result.success(sysLogService.pageLogs(queryDTO));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'VIEWER')")
    public Result<SysLog> getById(@PathVariable Long id) {
        return Result.success(sysLogService.getById(id));
    }

    @GetMapping("/modules")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'VIEWER')")
    public Result<?> getModules() {
        List<Map<String, Object>> modules = Arrays.stream(SysLogModule.values())
                .map(m -> Map.<String, Object>of(
                        "code", m.name(),
                        "description", m.getDescription()
                ))
                .collect(Collectors.toList());
        return Result.success(modules);
    }

    @GetMapping("/actions")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'VIEWER')")
    public Result<?> getActions() {
        List<Map<String, Object>> actions = Arrays.stream(SysLogAction.values())
                .map(a -> Map.<String, Object>of(
                        "code", a.name(),
                        "description", a.getDescription()
                ))
                .collect(Collectors.toList());
        return Result.success(actions);
    }
}
