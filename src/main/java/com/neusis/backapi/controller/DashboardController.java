package com.neusis.backapi.controller;

import com.neusis.backapi.dto.TodayDashboardDto;
import com.neusis.backapi.service.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardService dashboardService;

    @GetMapping("/today")
    public TodayDashboardDto getTodayDashboard() {
        return dashboardService.getTodayDashboard();
    }
}