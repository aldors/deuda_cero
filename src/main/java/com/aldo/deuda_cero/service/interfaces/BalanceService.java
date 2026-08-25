package com.aldo.deuda_cero.service.interfaces;

import java.util.List;

import com.aldo.deuda_cero.dto.balance.BalanceMiembroResponse;

public interface BalanceService {
    
    List<BalanceMiembroResponse> obtenerBalance(Long grupoId);
}
