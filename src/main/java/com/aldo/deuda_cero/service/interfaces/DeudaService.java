package com.aldo.deuda_cero.service.interfaces;

import java.util.List;

import com.aldo.deuda_cero.dto.deuda.DeudaResponse;
import com.aldo.deuda_cero.dto.deuda.ImpactoDeudaResponse;

public interface DeudaService {
    
    List<DeudaResponse> obtenerDeudas(Long grupoId);

    List<DeudaResponse> obtenerDeudasPendientes(Long grupoId);
    DeudaResponse obtenerDeudaPorId(Long grupoId, Long deudaId);
    void procesarImpactos(Long grupoId,List<ImpactoDeudaResponse> impactos);
}
