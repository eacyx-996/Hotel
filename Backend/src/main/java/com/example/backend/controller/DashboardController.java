package com.example.backend.controller;

import com.example.backend.common.Result;
import com.example.backend.service.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardService dashboardService;

    @GetMapping("/stats")
    public Result<Map<String, Object>> stats() {
        return Result.success(dashboardService.getStats());
    }
}
