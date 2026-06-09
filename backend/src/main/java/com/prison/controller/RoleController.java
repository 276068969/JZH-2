package com.prison.controller;

import com.prison.Result;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/roles")
public class RoleController {

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public Result<List<Map<String, String>>> list() {
        List<Map<String, String>> roles = Arrays.asList(
                role("ROLE_ADMIN", "超级管理员"),
                role("ROLE_MANAGER", "监狱管理员"),
                role("ROLE_GUARD", "狱警"),
                role("ROLE_DOCTOR", "医务人员"),
                role("ROLE_VIEWER", "普通查看人员")
        );
        return Result.success(roles);
    }

    private Map<String, String> role(String code, String name) {
        Map<String, String> map = new LinkedHashMap<>();
        map.put("code", code);
        map.put("name", name);
        return map;
    }
}