package com.aldo.deuda_cero.mapper;

import java.math.BigDecimal;

import org.springframework.stereotype.Component;

import com.aldo.deuda_cero.dto.balance.BalanceMiembroResponse;
import com.aldo.deuda_cero.entity.MiembroGrupo;

@Component
public class BalanceMapper {

    public static BalanceMiembroResponse toResponse(MiembroGrupo miembroGrupo, BigDecimal totalPagado, BigDecimal totalConsumido){

        BigDecimal balance = totalPagado.subtract(totalConsumido);

        return new BalanceMiembroResponse(
            miembroGrupo.getUsuario().getId(),
            miembroGrupo.getUsuario().getNombre(),
            totalPagado,
            totalConsumido,
            balance
        );
    }
}