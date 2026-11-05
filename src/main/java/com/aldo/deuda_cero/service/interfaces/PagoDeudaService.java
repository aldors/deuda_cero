package com.aldo.deuda_cero.service.interfaces;

import java.util.List;

import com.aldo.deuda_cero.dto.pagos.RegistrarPagoRequest;
import com.aldo.deuda_cero.dto.pagos.PagoResponse;

public interface PagoDeudaService {
    
    void registrarPago(Long grupoId, RegistrarPagoRequest registrarPagoRequest);
    List<PagoResponse> obtenerPagos(Long grupoId);
}
