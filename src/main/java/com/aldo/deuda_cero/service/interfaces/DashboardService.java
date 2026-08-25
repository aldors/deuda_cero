package com.aldo.deuda_cero.service.interfaces;

import com.aldo.deuda_cero.dto.dashboard.DashboardResponse;

public interface DashboardService {
    
    DashboardResponse obtenerDashboard(Long grupoId);
}
