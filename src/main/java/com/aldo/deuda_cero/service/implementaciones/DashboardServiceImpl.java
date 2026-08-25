package com.aldo.deuda_cero.service.implementaciones;

import java.math.BigDecimal;
import java.util.List;
import java.util.function.Function;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.aldo.deuda_cero.dto.dashboard.DashboardResponse;
import com.aldo.deuda_cero.dto.dashboard.DeudaResumenResponse;
import com.aldo.deuda_cero.dto.dashboard.MovimientoRecienteResponse;
import com.aldo.deuda_cero.entity.Deuda;
import com.aldo.deuda_cero.entity.MiembroGrupo;
import com.aldo.deuda_cero.entity.enums.EstadoDeuda;
import com.aldo.deuda_cero.repository.DeudaRepository;
import com.aldo.deuda_cero.repository.MovimientoRepository;
import com.aldo.deuda_cero.security.GroupPermissionService;
import com.aldo.deuda_cero.service.interfaces.DashboardService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DashboardServiceImpl implements DashboardService{

    private final GroupPermissionService groupPermissionService;
    private final DeudaRepository deudaRepository;
    private final MovimientoRepository movimientoRepository;

    @Override
    public DashboardResponse obtenerDashboard(Long grupoId) {

        MiembroGrupo miembroActual =
            groupPermissionService.obtenerMiembroActual(grupoId);

        List<Deuda> deudas = deudaRepository.findByGrupoIdAndDeudorIdAndEstado(
                        grupoId,
                        miembroActual.getId(),
                        EstadoDeuda.PENDIENTE
                );

        List<Deuda> acreencias = deudaRepository.findByGrupoIdAndAcreedorIdAndEstado(
                        grupoId,
                        miembroActual.getId(),
                        EstadoDeuda.PENDIENTE
                );

        return DashboardResponse.builder()
                .balanceGeneral(calcularBalanceGeneral(deudas, acreencias))
                .debes(construirResumenDeudas(deudas, Deuda::getAcreedor))
                .teDeben(construirResumenDeudas(acreencias, Deuda::getDeudor))
                .movimientosRecientes(obtenerMovimientosRecientes(grupoId))
                .build();
    }

    private BigDecimal calcularBalanceGeneral(List<Deuda> deudas, List<Deuda> acreencias){

        BigDecimal totalDebes = deudas.stream()
            .map(Deuda::getMontoPendiente)
            .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalTeDeben = acreencias.stream()
            .map(Deuda::getMontoPendiente)
            .reduce(BigDecimal.ZERO, BigDecimal::add);

        return totalTeDeben.subtract(totalDebes);
    }
    
    private List<DeudaResumenResponse> construirResumenDeudas(List<Deuda> deudas, Function<Deuda, MiembroGrupo> miembroMapper){

        return deudas.stream()
            .map(deuda -> {
                MiembroGrupo miembro = miembroMapper.apply(deuda);

                return DeudaResumenResponse.builder()
                            .deudaId(deuda.getId())
                            .usuarioId(miembro.getUsuario().getId())
                            .nombre(miembro.getUsuario().getNombre())
                            .montoPendiente(deuda.getMontoPendiente())
                            .build();
            })
            .toList();
    }


    /* El metodo construirResumenDeudas esta refactorizado
    private List<DeudaResumenResponse> construirDebes(List<Deuda> deudas){

        return deudas.stream()
            .map(deuda -> DeudaResumenResponse.builder()
                            .deudaId(deuda.getId())
                            .usuarioId(deuda.getAcreedor().getUsuario().getId())
                            .nombre(deuda.getAcreedor().getUsuario().getNombre())
                            .montoPendiente(deuda.getMontoPendiente())
                            .build()
            )
            .toList();
    }

    private List<DeudaResumenResponse> construirTeDeben(List<Deuda> acreencias){

        return acreencias.stream()
            .map(deuda -> DeudaResumenResponse.builder()
                            .deudaId(deuda.getId())
                            .usuarioId(deuda.getDeudor().getUsuario().getId())
                            .nombre(deuda.getDeudor().getUsuario().getNombre())
                            .montoPendiente(deuda.getMontoPendiente())
                            .build()
            )
            .toList();
    }
    */

    private List<MovimientoRecienteResponse> obtenerMovimientosRecientes(Long grupoId){

        return movimientoRepository.findTop10ByGrupoIdOrderByFechaMovimientoDesc(grupoId).stream()
            .map(movimiento -> MovimientoRecienteResponse.builder()
                            .movimientoId(movimiento.getId())
                            .descripcion(movimiento.getDescripcion())
                            .pagador(movimiento.getPagador().getUsuario().getNombre())
                            .montoTotal(movimiento.getMontoTotal())
                            .fechaMovimiento(movimiento.getFechaMovimiento())
                            .build()
            )
            .toList();
    }

}
