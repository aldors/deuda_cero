package com.aldo.deuda_cero.service.implementaciones;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
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
                .orElseThrow(() -> new RuntimeException("Movimiento no encontrado"));


        return MovimientoMapper.toDetalleResponse(movimiento);
    }


    /*
    Aqui me quede en que la logica repetida la metere dentro de metodos privados
    aqui en este mismo servicio para no estar repitiendola
    */

    //Estos metodos privados se reutilizan solo dentro de crearMovimiento()

    private List<ImpactoDeudaResponse> crearImpactosDeuda(MiembroGrupo pagador, List<ParticipacionMovimiento> participaciones) {

        return participaciones.stream().filter(participacion -> !participacion
                                .getMiembroGrupo()
                                .getId()
                                .equals(pagador.getId())
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

        return miembroGrupoRepository.findByUsuarioIdAndGrupoId(usuarioId, grupoId)
            .orElseThrow(() -> new MiembroPagadorNoPerteneceAlGrupoException());
    }

    private Map<Long, MiembroGrupo> obtenerParticipantes(Long grupoId, CrearMovimientoRequest crearMovimientoRequest){

        List<Long> usuarioIds = crearMovimientoRequest.getParticipantes().stream()
            .map(ParticipanteMovimientoRequest::getUsuarioId)
            .toList();

        List<MiembroGrupo> miembros = miembroGrupoRepository.findByGrupoIdAndUsuarioIdIn(grupoId, usuarioIds);

        if(miembros.size() != usuarioIds.size()) {
            throw new ParticipantesNoPertenecenAlGrupoException();
        }

        return miembros.stream()
            .collect(Collectors.toMap(
                    mg -> mg.getUsuario().getId(),
                    Function.identity() // ¿Que hace esto?
            ));
    }

    private Movimiento guardarMovimiento(CrearMovimientoRequest crearMovimientoRequest, MiembroGrupo registradoPor, MiembroGrupo pagador){

        Movimiento movimiento = MovimientoMapper.toEntity(crearMovimientoRequest);

        movimiento.setGrupo(registradoPor.getGrupo());
        movimiento.setRegistradoPor(registradoPor);
        movimiento.setPagador(pagador);
        movimiento.setFechaMovimiento(LocalDateTime.now());
        movimiento.setFechaCreacion(LocalDateTime.now());
        movimiento.setTipoDivision(crearMovimientoRequest.getTipoDivision());

        return movimientoRepository.save(movimiento);
    }

    private List<BigDecimal> calcualarMontosIguales(BigDecimal montoTotal, int participantes){

        // return montoTotal.divide(BigDecimal.valueOf(participantes), 2, RoundingMode.HALF_UP);

        BigDecimal montoBase =  montoTotal.divide(BigDecimal.valueOf(participantes), 2, RoundingMode.DOWN); // HALF_UP

        BigDecimal sumaBase = montoBase.multiply(BigDecimal.valueOf(participantes));

        BigDecimal diferencia  = montoTotal.subtract(sumaBase);

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

        BigDecimal suma = crearMovimientoRequest.getParticipantes().stream()
            .map(ParticipanteMovimientoRequest::getMonto)
            .reduce(BigDecimal.ZERO, BigDecimal::add);

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

        //for(ParticipanteMovimientoRequest participante : crearMovimientoRequest.getParticipantes()){
        for(int i = 0; i < crearMovimientoRequest.getParticipantes().size(); i++){

            //Esto es nuevo para lo de los centavos en movimientos con division TipoDivision.IGUAL
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

    //DECIRLE A CHAT QUE YA PROBE EL CODIGO Y SI FUNIONA BIEN,
    //(**por si se me olvida esto** debo investigar a que se refiere con
    //(Fíjate que aquí no hay búsquedas a la base de datos. Todo ocurre en memoria gracias al Map.) en el metodo
    //guardarParticipaciones()

    //Y ademas mandarle las clases que me pidio para que las analice
}
