package com.aldo.deuda_cero.service.implementaciones;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.aldo.deuda_cero.dto.pagos.PagoResponse;
import com.aldo.deuda_cero.dto.pagos.RegistrarPagoRequest;
import com.aldo.deuda_cero.entity.Deuda;
import com.aldo.deuda_cero.entity.Grupo;
import com.aldo.deuda_cero.entity.MiembroGrupo;
import com.aldo.deuda_cero.entity.PagoDeuda;
import com.aldo.deuda_cero.entity.Usuario;
import com.aldo.deuda_cero.entity.enums.EstadoDeuda;
import com.aldo.deuda_cero.exception.DeudaNoEncontradaException;
import com.aldo.deuda_cero.exception.DeudaNoPerteneceAlGrupoException;
import com.aldo.deuda_cero.exception.DeudaSaldadaException;
import com.aldo.deuda_cero.exception.GrupoNoEncontradoException;
import com.aldo.deuda_cero.exception.MiembroPagadorNoPerteneceAlGrupoException;
import com.aldo.deuda_cero.exception.MontoAPagarDebeSerMayorQueCeroException;
import com.aldo.deuda_cero.exception.NoPuedesPagarUnaDeudaQueNoTePerteneceException;
import com.aldo.deuda_cero.exception.PagoNoPuedeSuperarLaDeudaPendiente;
import com.aldo.deuda_cero.mapper.PagoDeudaMapper;
import com.aldo.deuda_cero.repository.DeudaRepository;
import com.aldo.deuda_cero.repository.GrupoRepository;
import com.aldo.deuda_cero.repository.MiembroGrupoRepository;
import com.aldo.deuda_cero.repository.PagoDeudaRepository;
import com.aldo.deuda_cero.security.CurrentUserService;
import com.aldo.deuda_cero.security.GroupPermissionService;
import com.aldo.deuda_cero.service.interfaces.PagoDeudaService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class PagoDeudaServiceImpl implements PagoDeudaService{

    private final GrupoRepository grupoRepository;
    private final CurrentUserService currentUserService;
    private final MiembroGrupoRepository miembroGrupoRepository;
    private final PagoDeudaRepository pagoDeudaRepository;
    private final DeudaRepository deudaRepository;
    private final GroupPermissionService groupPermissionService;

    @Override
    public void registrarPago(Long grupoId, RegistrarPagoRequest registrarPagoRequest) {

        //Podemos cambiar a un condificional, ya que el objeto grupo no se usará
        Grupo grupo = grupoRepository.findById(grupoId).
            orElseThrow(() -> new GrupoNoEncontradoException());

        // Este usuario sera automaticamente el PAGADOR
        Usuario usuarioActual = currentUserService.obtenerUsuarioActual();

        MiembroGrupo pagador = miembroGrupoRepository.findByUsuarioIdAndGrupoId(usuarioActual.getId(), grupoId)//
            .orElseThrow(() -> new MiembroPagadorNoPerteneceAlGrupoException());

        if(registrarPagoRequest.getMonto() == null || registrarPagoRequest.getMonto().compareTo(BigDecimal.ZERO) <= 0){
            throw new MontoAPagarDebeSerMayorQueCeroException();
        }

        Deuda deuda = deudaRepository.findById(registrarPagoRequest.getDeudaId())
            .orElseThrow(() -> new DeudaNoEncontradaException());

        if(!deuda.getGrupo().getId().equals(grupoId)){
            throw new DeudaNoPerteneceAlGrupoException();
        }

        if(!deuda.getDeudor().getId().equals(pagador.getId())){
            throw new NoPuedesPagarUnaDeudaQueNoTePerteneceException();
        }

        if(deuda.getEstado() != EstadoDeuda.PENDIENTE){
            throw new DeudaSaldadaException();
        }

        if(registrarPagoRequest.getMonto().compareTo(deuda.getMontoPendiente()) > 0){
            throw new PagoNoPuedeSuperarLaDeudaPendiente();
        }

        //MiembroGrupo receptor = deuda.getAcreedor();

        PagoDeuda pago = PagoDeudaMapper.toEntity(registrarPagoRequest, deuda);

        pagoDeudaRepository.save(pago);

        BigDecimal nuevoMontoPendiente = deuda.getMontoPendiente().subtract(registrarPagoRequest.getMonto());

        deuda.setMontoPendiente(nuevoMontoPendiente);

        if(nuevoMontoPendiente.compareTo(BigDecimal.ZERO) == 0){
            deuda.setEstado(EstadoDeuda.SALDADA);
        }

        deuda.setFechaActualizacion(LocalDateTime.now());

        deudaRepository.save(deuda);

    }

    @Override
    public List<PagoResponse> obtenerPagos(Long grupoId) {

        groupPermissionService.obtenerMiembroActual(grupoId);

        List<PagoDeuda> pagos = pagoDeudaRepository.findByDeudaGrupoIdOrderByFechaPagoDesc(grupoId);
        
        return pagos.stream()
            .map(PagoDeudaMapper::toResponse)
            .toList();
    }
    
}
