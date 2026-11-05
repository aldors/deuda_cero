package com.aldo.deuda_cero.service.implementaciones;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.aldo.deuda_cero.dto.balance.BalanceMiembroResponse;
import com.aldo.deuda_cero.dto.balance.TotalConsumidoResponse;
import com.aldo.deuda_cero.dto.balance.TotalPagadoResponse;
import com.aldo.deuda_cero.entity.MiembroGrupo;
import com.aldo.deuda_cero.entity.enums.EstadoMiembro;
import com.aldo.deuda_cero.mapper.BalanceMapper;
import com.aldo.deuda_cero.repository.MiembroGrupoRepository;
import com.aldo.deuda_cero.repository.MovimientoRepository;
import com.aldo.deuda_cero.repository.PagoDeudaRepository;
import com.aldo.deuda_cero.repository.ParticipacionMovimientoRepository;
import com.aldo.deuda_cero.security.GroupPermissionService;
import com.aldo.deuda_cero.service.interfaces.BalanceService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BalanceServiceImpl implements BalanceService{ 

    private final MiembroGrupoRepository miembroGrupoRepository;
    private final ParticipacionMovimientoRepository participacionMovimientoRepository;
    private final MovimientoRepository movimientoRepository;
    private final PagoDeudaRepository pagoDeudaRepository;
    private final GroupPermissionService groupPermissionService;

    @Override
    public List<BalanceMiembroResponse> obtenerBalance(Long grupoId) {

        groupPermissionService.obtenerMiembroActual(grupoId);

        List<MiembroGrupo> miembros = miembroGrupoRepository.findByGrupoIdAndEstado(grupoId, EstadoMiembro.ACTIVO);

        List<TotalPagadoResponse> totalesPagados = movimientoRepository.obtenerTotalPagadoPorMiembro(grupoId);

        List<TotalConsumidoResponse> totalesConsumidos = participacionMovimientoRepository.obtenerTotalConsumidoPorMiembro(grupoId);

        List<TotalPagadoResponse> pagosRealizados = pagoDeudaRepository.obtenerTotalPagadoEnDeudas(grupoId);

        List<TotalConsumidoResponse> pagosRecibidos = pagoDeudaRepository.obtenerTotalRecibidoEnDeudas(grupoId);

        Map<Long, BigDecimal> pagadosPorMiembro = totalesPagados.stream()
                        .collect(Collectors.toMap(
                                TotalPagadoResponse::getMiembroGrupoId,
                                TotalPagadoResponse::getTotalPagado
                        ));
        
        Map<Long, BigDecimal> consumidosPorMiembro = totalesConsumidos.stream()
                        .collect(Collectors.toMap(
                                TotalConsumidoResponse::getMiembroGrupoId,
                                TotalConsumidoResponse::getGtotalConsumido
                        ));

        Map<Long, BigDecimal> pagosRealizadosPorMiembro = pagosRealizados.stream()
                        .collect(Collectors.toMap(
                                TotalPagadoResponse::getMiembroGrupoId,
                                TotalPagadoResponse::getTotalPagado
                        ));

        Map<Long, BigDecimal> pagosRecibidosPorMiembro = pagosRecibidos.stream()
                        .collect(Collectors.toMap(
                                TotalConsumidoResponse::getMiembroGrupoId,
                                TotalConsumidoResponse::getGtotalConsumido
                        ));

        return miembros.stream()
                        .map(miembro -> {

                            Long miembroId = miembro.getId();

                            BigDecimal totalPagado = pagadosPorMiembro.getOrDefault(miembroId, BigDecimal.ZERO);

                            BigDecimal totalConsumido = consumidosPorMiembro.getOrDefault(miembroId, BigDecimal.ZERO);

                            BigDecimal balance = totalPagado
                                    .subtract(totalConsumido)
                                    .add(pagosRealizadosPorMiembro.getOrDefault(miembroId, BigDecimal.ZERO))
                                    .subtract(pagosRecibidosPorMiembro.getOrDefault(miembroId, BigDecimal.ZERO));

                            return BalanceMapper.toResponse(miembro, totalPagado, totalConsumido, balance);
                        })
                        .toList();
    }
    
}
