package com.aldo.deuda_cero.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.aldo.deuda_cero.dto.dashboard.DashboardResponse;
import com.aldo.deuda_cero.service.interfaces.DashboardService;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/grupos")
public class DashboardController {

    private final DashboardService dashboardService;
    
    @GetMapping("/{grupoId}/dashboard")
    public ResponseEntity<DashboardResponse> obtenerDashboard(@PathVariable Long grupoId){
        return ResponseEntity.ok(dashboardService.obtenerDashboard(grupoId));
    }
}
