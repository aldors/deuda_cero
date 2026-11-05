package com.aldo.deuda_cero.service.implementaciones;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.aldo.deuda_cero.dto.Movimientos.CrearMovimientoRequest;
import com.aldo.deuda_cero.dto.Movimientos.MovimientoDetalleResponse;
import com.aldo.deuda_cero.dto.Movimientos.MovimientoResponse;
import com.aldo.deuda_cero.dto.Movimientos.MovimientoResumenResponse;
import com.aldo.deuda_cero.dto.Movimientos.ParticipanteMovimientoRequest;
import com.aldo.deuda_cero.dto.deuda.ImpactoDeudaResponse;
import com.aldo.deuda_cero.entity.MiembroGrupo;
import com.aldo.deuda_cero.entity.Movimiento;
import com.aldo.deuda_cero.entity.ParticipacionMovimiento;
import com.aldo.deuda_cero.entity.enums.TipoDivision;
import com.aldo.deuda_cero.exception.MiembroPagadorNoPerteneceAlGrupoException;
import com.aldo.deuda_cero.exception.MontoParticipanteInvalidoException;
import com.aldo.deuda_cero.exception.MovimientoNoEncontradoException;
import com.aldo.deuda_cero.exception.ParticipantesNoPertenecenAlGrupoException;
import com.aldo.deuda_cero.exception.SumaDeMontosNoCoincideConMontoTotalException;
import com.aldo.deuda_cero.mapper.MovimientoMapper;
import com.aldo.deuda_cero.repository.MiembroGrupoRepository;
import com.aldo.deuda_cero.repository.MovimientoRepository;
import com.aldo.deuda_cero.repository.ParticipacionMovimientoRepository;
import com.aldo.deuda_cero.security.GroupPermissionService;
import com.aldo.deuda_cero.service.interfaces.DeudaService;
import com.aldo.deuda_cero.service.interfaces.MovimientoService;

import lombok.RequiredArgsConstructor;

@Service
@Transactional
@RequiredArgsConstructor
public class MovimientoServiceImpl implements MovimientoService{

    private final GroupPermissionService groupPermissionService;
    private final MiembroGrupoRepository miembroGrupoRepository;
    private final MovimientoRepository movimientoRepository;
    private final ParticipacionMovimientoRepository participacionMovimientoRepository;
    private final DeudaService deudaService;

    @Override
    public MovimientoResponse crearMovimiento(Long grupoId, CrearMovimientoRequest crearMovimientoRequest) {
        
        MiembroGrupo registradoPor = groupPermissionService.obtenerMiembroActual(grupoId);

        MiembroGrupo pagador = obtenerPagador(grupoId, crearMovimientoRequest.getPagadorId());

        Map<Long, MiembroGrupo> participantes = obtenerParticipantes(grupoId, crearMovimientoRequest);

        Movimiento movimiento = guardarMovimiento(crearMovimientoRequest, registradoPor, pagador);

        List<ParticipacionMovimiento> participaciones = guardarParticipaciones(movimiento, crearMovimientoRequest, participantes);

        List<ImpactoDeudaResponse> impactos = crearImpactosDeuda(pagador, participaciones);

        deudaService.procesarImpactos(grupoId, impactos);

        return MovimientoMapper.toResponse(movimiento);
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<MovimientoResumenResponse> obtenerMovimientos(Long grupoId) {

        groupPermissionService.obtenerMiembroActual(grupoId);

        List<Movimiento> movimientos = movimientoRepository.findByGrupoIdOrderByFechaMovimientoDesc(grupoId);

        return movimientos.stream()
            .map(MovimientoMapper::toResumenResponse)
            .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public MovimientoDetalleResponse obtenerDetalleMovimiento(Long grupoId, Long movimientoId) {
        
        groupPermissionService.obtenerMiembroActual(grupoId);

        Movimiento movimiento = movimientoRepository.findByIdAndGrupoId(movimientoId, grupoId)
                .orElseThrow(() -> new MovimientoNoEncontradoException());


        return MovimientoMapper.toDetalleResponse(movimiento);
    }


    private List<ImpactoDeudaResponse> crearImpactosDeuda(MiembroGrupo pagador, List<ParticipacionMovimiento> participaciones) {

        return participaciones.stream().filter(participacion -> !participacion
                                .getMiembroGrupo()
                                .getId()
                                .equals(pagador.getId())
                )
                .filter(participacion -> participacion.getMontoCorrespondiente() != null
                                && participacion.getMontoCorrespondiente().compareTo(BigDecimal.ZERO) > 0
                )
                .map(participacion -> new ImpactoDeudaResponse(
                                participacion.getMiembroGrupo(),
                                pagador,
                                participacion.getMontoCorrespondiente()
                        )
                )
                .toList();
    }
      
    private MiembroGrupo obtenerPagador(Long grupoId, Long usuarioId){

        return miembroGrupoRepository.findActivoByUsuarioIdAndGrupoId(usuarioId, grupoId)
            .orElseThrow(() -> new MiembroPagadorNoPerteneceAlGrupoException());
    }

    private Map<Long, MiembroGrupo> obtenerParticipantes(Long grupoId, CrearMovimientoRequest crearMovimientoRequest){

        List<Long> usuarioIds = crearMovimientoRequest.getParticipantes().stream()
            .map(ParticipanteMovimientoRequest::getUsuarioId)
            .toList();

        List<MiembroGrupo> miembros = miembroGrupoRepository.findActivosByGrupoIdAndUsuarioIdIn(grupoId, usuarioIds);

        if(miembros.size() != usuarioIds.size()) {
            throw new ParticipantesNoPertenecenAlGrupoException();
        }

        return miembros.stream()
            .collect(Collectors.toMap(
                    mg -> mg.getUsuario().getId(),
                    Function.identity()
            ));
    }

    private Movimiento guardarMovimiento(CrearMovimientoRequest crearMovimientoRequest, MiembroGrupo registradoPor, MiembroGrupo pagador){

        Movimiento movimiento = MovimientoMapper.toEntity(crearMovimientoRequest);

        movimiento.setGrupo(registradoPor.getGrupo());
        movimiento.setRegistradoPor(registradoPor);
        movimiento.setPagador(pagador);
        movimiento.setTipoDivision(crearMovimientoRequest.getTipoDivision());

        return movimientoRepository.save(movimiento);
    }

    private List<BigDecimal> calcualarMontosIguales(BigDecimal montoTotal, int participantes){

        BigDecimal totalEnCentavos = montoTotal.setScale(2, RoundingMode.HALF_UP);

        BigDecimal montoBase = totalEnCentavos.divide(BigDecimal.valueOf(participantes), 2, RoundingMode.DOWN);

        BigDecimal sumaBase = montoBase.multiply(BigDecimal.valueOf(participantes));

        BigDecimal diferencia = totalEnCentavos.subtract(sumaBase);

        int centavosRestantes = diferencia.movePointRight(2).intValueExact();

        List<BigDecimal> montos = new ArrayList<>();

        for (int i = 0; i < participantes; i++) {
            
            BigDecimal monto = montoBase;

            if(i < centavosRestantes){

                monto = monto.add(BigDecimal.valueOf(0.01));
            }

            montos.add(monto);
        }

        return montos;
    }

    private void validarMontosPersonalizados(CrearMovimientoRequest crearMovimientoRequest){

        BigDecimal suma = BigDecimal.ZERO;

        for (ParticipanteMovimientoRequest participante : crearMovimientoRequest.getParticipantes()) {

            BigDecimal monto = participante.getMonto();

            if (monto == null || monto.compareTo(BigDecimal.ZERO) <= 0) {
                throw new MontoParticipanteInvalidoException();
            }

            suma = suma.add(monto);
        }

        if(suma.compareTo(crearMovimientoRequest.getMontoTotal()) != 0) {
            throw new SumaDeMontosNoCoincideConMontoTotalException();
        }
    }

    private List<ParticipacionMovimiento> guardarParticipaciones(Movimiento movimiento, CrearMovimientoRequest crearMovimientoRequest, Map<Long, MiembroGrupo> participantes){

        List<ParticipacionMovimiento> participaciones = new ArrayList<>();

        List<BigDecimal> montoIgual = null;

        if(crearMovimientoRequest.getTipoDivision() == TipoDivision.IGUAL){

            montoIgual = calcualarMontosIguales(crearMovimientoRequest.getMontoTotal(), crearMovimientoRequest.getParticipantes().size());
        }
        else{
            validarMontosPersonalizados(crearMovimientoRequest);
        }

        for(int i = 0; i < crearMovimientoRequest.getParticipantes().size(); i++){

            ParticipanteMovimientoRequest participante = crearMovimientoRequest.getParticipantes().get(i);

            BigDecimal monto = crearMovimientoRequest.getTipoDivision() == TipoDivision.IGUAL ? montoIgual.get(i) : participante.getMonto();

            ParticipacionMovimiento participacionMovimiento = ParticipacionMovimiento.builder()
                .movimiento(movimiento)
                .miembroGrupo(participantes.get(participante.getUsuarioId()))
                .montoCorrespondiente(monto)
                .build();

            participaciones.add(participacionMovimiento);
        }

        return participacionMovimientoRepository.saveAll(participaciones);

    }
}
