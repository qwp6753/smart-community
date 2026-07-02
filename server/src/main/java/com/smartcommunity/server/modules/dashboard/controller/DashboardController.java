package com.smartcommunity.server.modules.dashboard.controller;

import com.smartcommunity.server.common.api.ApiResponse;
import com.smartcommunity.server.modules.dashboard.service.DashboardService;
import com.smartcommunity.server.security.RequirePermission;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {

    private final DashboardService dashboardService;

    public DashboardController(DashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    @RequirePermission("dashboard:view")
    @GetMapping("/stats")
    public ApiResponse<Map<String, Object>> stats() {
        return ApiResponse.success(dashboardService.stats());
    }
}
