package com.aldo.deuda_cero.service.implementaciones;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.aldo.deuda_cero.dto.deuda.DeudaResponse;
import com.aldo.deuda_cero.dto.deuda.ImpactoDeudaResponse;
import com.aldo.deuda_cero.entity.Deuda;
import com.aldo.deuda_cero.entity.Grupo;
import com.aldo.deuda_cero.entity.MiembroGrupo;
import com.aldo.deuda_cero.entity.enums.EstadoDeuda;
import com.aldo.deuda_cero.exception.DeudaNoEncontradaException;
import com.aldo.deuda_cero.exception.DeudaNoPerteneceAlGrupoException;
import com.aldo.deuda_cero.mapper.DeudaMapper;
import com.aldo.deuda_cero.repository.DeudaRepository;
import com.aldo.deuda_cero.security.GroupPermissionService;
import com.aldo.deuda_cero.service.interfaces.DeudaService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class DeudaServiceImpl implements DeudaService{
    
    private final DeudaRepository deudaRepository;
    private final GroupPermissionService groupPermissionService;

    @Override
    @Transactional(readOnly = true)
    public List<DeudaResponse> obtenerDeudas(Long grupoId) {
        
        groupPermissionService.obtenerMiembroActual(grupoId);

        List<Deuda> deudas = deudaRepository.findByGrupoId(grupoId);
        return DeudaMapper.toResponseList(deudas);
    }

    @Override
    @Transactional(readOnly = true)
    public List<DeudaResponse> obtenerDeudasPendientes(Long grupoId) {
        
        groupPermissionService.obtenerMiembroActual(grupoId);

        List<Deuda> deudas = deudaRepository.findByGrupoIdAndEstado(grupoId, EstadoDeuda.PENDIENTE);

        return DeudaMapper.toResponseList(deudas);
    }

    @Override
    @Transactional(readOnly = true)
    public DeudaResponse obtenerDeudaPorId(Long grupoId, Long deudaId) {
        
        groupPermissionService.obtenerMiembroActual(grupoId);

        Deuda deuda = deudaRepository.findById(deudaId)
            .orElseThrow(() -> new DeudaNoEncontradaException());

        if(!deuda.getGrupo().getId().equals(grupoId)){
            throw new DeudaNoPerteneceAlGrupoException();
        }

        return DeudaMapper.toResponse(deuda);
    }

    @Override
    @Transactional
    public void procesarImpactos(Long grupoId, List<ImpactoDeudaResponse> impactos) {

        for (ImpactoDeudaResponse impacto : impactos) {

            aplicarImpacto(grupoId, impacto.getDeudor(), impacto.getAcreedor(), impacto.getMonto());
        }
    }

    private void aplicarImpacto(Long grupoId, MiembroGrupo deudor, MiembroGrupo acreedor, BigDecimal monto) {

        if (deudor.getId().equals(acreedor.getId())) {
            return;
        }

        Optional<Deuda> deudaDirecta = deudaRepository.findByGrupoIdAndDeudorIdAndAcreedorIdAndEstado(
                                grupoId,
                                deudor.getId(),
                                acreedor.getId(),
                                EstadoDeuda.PENDIENTE
                        );

        if (deudaDirecta.isPresent()) {

            aumentarDeuda(deudaDirecta.get(), monto);
            return;
        }

        Optional<Deuda> deudaInversa = deudaRepository.findByGrupoIdAndDeudorIdAndAcreedorIdAndEstado(
                                grupoId,
                                acreedor.getId(),
                                deudor.getId(),
                                EstadoDeuda.PENDIENTE
                        );

        if (deudaInversa.isEmpty()) {

            crearDeuda(deudor.getGrupo(), deudor, acreedor, monto);
            return;
        }

        compensarDeuda(deudaInversa.get(), monto);
    }

    private void aumentarDeuda(Deuda deuda, BigDecimal monto) {

        deuda.setMontoOriginal(deuda.getMontoOriginal().add(monto));

        deuda.setMontoPendiente(deuda.getMontoPendiente().add(monto));

        deudaRepository.save(deuda);
    }

    private void crearDeuda(Grupo grupo, MiembroGrupo deudor, MiembroGrupo acreedor, BigDecimal monto) {

        Deuda deuda = Deuda.builder()
                .grupo(grupo)
                .deudor(deudor)
                .acreedor(acreedor)
                .montoOriginal(monto)
                .montoPendiente(monto)
                .build();

        deudaRepository.save(deuda);
    }

    private void compensarDeuda(Deuda deudaInversa, BigDecimal monto) {

        BigDecimal montoPendiente = deudaInversa.getMontoPendiente();

        int comparacion = monto.compareTo(montoPendiente);

        if (comparacion < 0) {

            deudaInversa.setMontoPendiente(montoPendiente.subtract(monto));

            deudaRepository.save(deudaInversa);

            return;
        }

        if (comparacion == 0) {

            deudaInversa.setMontoPendiente(BigDecimal.ZERO);

            deudaInversa.setEstado(EstadoDeuda.SALDADA);

            deudaRepository.save(deudaInversa);

            return;
        }

        BigDecimal excedente = monto.subtract(montoPendiente);

        deudaInversa.setMontoPendiente(BigDecimal.ZERO);

        deudaInversa.setEstado(EstadoDeuda.SALDADA);

        deudaRepository.save(deudaInversa);

        crearDeuda(deudaInversa.getGrupo(), deudaInversa.getAcreedor(), deudaInversa.getDeudor(), excedente);
    }

}
