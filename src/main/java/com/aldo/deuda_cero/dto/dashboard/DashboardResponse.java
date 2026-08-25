package com.aldo.deuda_cero.dto.dashboard;

import java.math.BigDecimal;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class DashboardResponse {
    
    private BigDecimal balanceGeneral;
    private List<DeudaResumenResponse> debes;
    private List<DeudaResumenResponse> teDeben;
    private List<MovimientoRecienteResponse> movimientosRecientes;
}
