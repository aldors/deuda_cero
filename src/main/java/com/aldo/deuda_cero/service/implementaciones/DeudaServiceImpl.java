package com.aldo.deuda_cero.service.implementaciones;

import java.math.BigDecimal;
import java.time.LocalDateTime;
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
import com.aldo.deuda_cero.service.interfaces.DeudaService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DeudaServiceImpl implements DeudaService{
    
    private final DeudaRepository deudaRepository;

    @Override
    public List<DeudaResponse> obtenerDeudas(Long grupoId) {
        
        List<Deuda> deudas = deudaRepository.findByGrupoId(grupoId);
        return DeudaMapper.toResponseList(deudas);

        /*
        Aqui no se porque no se pone List<DeudaResponse> directamente en el repository
        asi evitando el response toResponseList
        */
    }

    @Override
    public List<DeudaResponse> obtenerDeudasPendientes(Long grupoId) {
        
        List<Deuda> deudas = deudaRepository.findByGrupoIdAndEstado(grupoId, EstadoDeuda.PENDIENTE);

        return DeudaMapper.toResponseList(deudas);
    }

    @Override
    public DeudaResponse obtenerDeudaPorId(Long grupoId, Long deudaId) {
        
        Deuda deuda = deudaRepository.findById(deudaId)
            .orElseThrow(() -> new DeudaNoEncontradaException());

        if(!deuda.getGrupo().getId().equals(grupoId)){
            throw new DeudaNoPerteneceAlGrupoException();
        }

        return DeudaMapper.toResponse(deuda);
    }

    @Override
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

        deuda.setFechaActualizacion(LocalDateTime.now());

        deudaRepository.save(deuda);
    }

    private void crearDeuda(Grupo grupo, MiembroGrupo deudor, MiembroGrupo acreedor, BigDecimal monto) {

        Deuda deuda = Deuda.builder()
                .grupo(grupo)
                .deudor(deudor)
                .acreedor(acreedor)
                .montoOriginal(monto)
                .montoPendiente(monto)
                .estado(EstadoDeuda.PENDIENTE)
                .fechaCreacion(LocalDateTime.now())
                .fechaActualizacion(LocalDateTime.now())
                .build();

        deudaRepository.save(deuda);
    }

    private void compensarDeuda(Deuda deudaInversa, BigDecimal monto) {

        BigDecimal montoPendiente = deudaInversa.getMontoPendiente();

        int comparacion = monto.compareTo(montoPendiente);

        if (comparacion < 0) {

            deudaInversa.setMontoPendiente(montoPendiente.subtract(monto));

            deudaInversa.setFechaActualizacion(LocalDateTime.now());

            deudaRepository.save(deudaInversa);

            return;
        }

        if (comparacion == 0) {

            deudaInversa.setMontoPendiente(BigDecimal.ZERO);

            deudaInversa.setEstado(EstadoDeuda.SALDADA);

            deudaInversa.setFechaActualizacion(LocalDateTime.now());

            deudaRepository.save(deudaInversa);

            return;
        }

        BigDecimal excedente = monto.subtract(montoPendiente);

        deudaInversa.setMontoPendiente(BigDecimal.ZERO);

        deudaInversa.setEstado(EstadoDeuda.SALDADA);

        deudaInversa.setFechaActualizacion(LocalDateTime.now());

        deudaRepository.save(deudaInversa);

        crearDeuda(deudaInversa.getGrupo(), deudaInversa.getAcreedor(), deudaInversa.getDeudor(), excedente);
    }







    
    /* Metodo principal
    @Override
    public List<DeudaResponse> obtenerDeudas(Long grupoId) {

        List<BalanceMiembroResponse> balances = balanceService.obtenerBalance(grupoId);

        List<DeudaBrutaResponse> deudasBrutas = calcularDeudasBrutas(balances);

        List<PagoDeuda> pagos = pagoDeudaRepository.findByGrupoId(grupoId);

        return aplicarPagos(deudasBrutas, pagos);


        
        //List<BalanceMiembroResponse> balances = balanceService.obtenerBalance(grupoId);

        //List<SaldoPendienteResponse> deudores = obtenerDeudores(balances);
        //List<SaldoPendienteResponse> acreedores = obtenerAcreedores(balances);

        //return calcularLiquidaciones(deudores, acreedores);
        
    }
    */

    //Nuevos metodos privados
    /*
    private List<DeudaBrutaResponse> calcularDeudasBrutas(List<BalanceMiembroResponse> balances) {

        List<SaldoPendienteResponse> deudores = obtenerDeudores(balances);
        List<SaldoPendienteResponse> acreedores = obtenerAcreedores(balances);

        return calcularLiquidaciones(deudores, acreedores);
    }

    private List<DeudaBrutaResponse> calcularLiquidaciones(List<SaldoPendienteResponse> deudores, List<SaldoPendienteResponse> acreedores) {

        List<DeudaBrutaResponse> deudas = new ArrayList<>();

        int indiceDeudor = 0;
        int indiceAcreedor = 0;

        while (indiceDeudor < deudores.size() && indiceAcreedor < acreedores.size()) {
                
            SaldoPendienteResponse deudor = deudores.get(indiceDeudor);
            SaldoPendienteResponse acreedor = acreedores.get(indiceAcreedor);

            BigDecimal montoDeudor = deudor.getMonto();
            BigDecimal montoAcreedor = acreedor.getMonto();

            BigDecimal monto = montoDeudor.min(montoAcreedor);

            deudas.add(new DeudaBrutaResponse(
                            deudor.getMiembroGrupoId(),
                            deudor.getNombre(),
                            acreedor.getMiembroGrupoId(),
                            acreedor.getNombre(),
                            monto
                    )
            );

            BigDecimal restanteDeudor =
                    deudor.getMonto()
                            .subtract(monto);

            BigDecimal restanteAcreedor =
                    acreedor.getMonto()
                            .subtract(monto);

            deudores.set(
                    indiceDeudor,
                    new SaldoPendienteResponse(
                            deudor.getMiembroGrupoId(),
                            deudor.getNombre(),
                            restanteDeudor
                    )
            );

            acreedores.set(
                    indiceAcreedor,
                    new SaldoPendienteResponse(
                            acreedor.getMiembroGrupoId(),
                            acreedor.getNombre(),
                            restanteAcreedor
                    )
            );

            if (restanteDeudor.compareTo(BigDecimal.ZERO) == 0) {
                indiceDeudor++;
            }

            if (restanteAcreedor.compareTo(BigDecimal.ZERO) == 0) {
                indiceAcreedor++;
            }
        }

        return deudas;
    }

    private List<DeudaResponse> aplicarPagos(List<DeudaBrutaResponse> deudasBrutas, List<PagoDeuda> pagos) {

        Map<String, BigDecimal> pagosPorRelacion = pagos.stream()
                        .collect(Collectors.groupingBy(pago ->
                                                construirClave(
                                                        pago.getPagador().getId(),
                                                        pago.getReceptor().getId()
                                                ),
                                        Collectors.reducing(
                                                BigDecimal.ZERO,
                                                PagoDeuda::getMonto,
                                                BigDecimal::add
                                        )
                                )
                        );

        List<DeudaResponse> resultado = new ArrayList<>();

        for (DeudaBrutaResponse deuda : deudasBrutas) {

            String clave = construirClave(
                            deuda.getDeudorId(),
                            deuda.getAcreedorId()
                        );

            BigDecimal totalPagado = pagosPorRelacion.getOrDefault(
                            clave,
                            BigDecimal.ZERO
                        );

            BigDecimal deudaPendiente = deuda.getMonto()
                            .subtract(totalPagado);

            if (deudaPendiente.compareTo(BigDecimal.ZERO) > 0) {

                resultado.add(new DeudaResponse(
                                deuda.getDeudorId(),
                                deuda.getDeudor(),
                                deuda.getAcreedorId(),
                                deuda.getAcreedor(),
                                deudaPendiente
                        )
                );
            }
        }

        return resultado;
    }

    private String construirClave(Long deudorId, Long acreedorId) {

        return deudorId + "-" + acreedorId;
    }
    */

    //Metodos privados
    /*
    private List<SaldoPendienteResponse> obtenerDeudores(List<BalanceMiembroResponse> balances){

        return balances.stream()
            .filter(balance -> balance.getBalance().compareTo(BigDecimal.ZERO) < 0)
            .map(balance -> new SaldoPendienteResponse(
                        balance.getMiembroGrupoId(), // cambie de usuarioId a miembroGrupoId
                        balance.getNombre(),
                        balance.getBalance().abs()
                    )
                )
            .collect(Collectors.toCollection(ArrayList::new)); //toList();
    }

    private List<SaldoPendienteResponse> obtenerAcreedores(List<BalanceMiembroResponse> balances){

        return balances.stream()
            .filter(balance -> balance.getBalance().compareTo(BigDecimal.ZERO) > 0)
            .map(balance -> new SaldoPendienteResponse(
                        balance.getMiembroGrupoId(), // cambie de usuarioId a miembroGrupoId
                        balance.getNombre(),
                        balance.getBalance()
                    )
                )
            .collect(Collectors.toCollection(ArrayList::new)); //toList();
    }
    */

    /* Este metodo es el mismo que el otro calcularLiquidaciones, solo que el otro retorna otra cosa
    private List<DeudaResponse> calcularLiquidaciones(List<SaldoPendienteResponse> deudores, List<SaldoPendienteResponse> acreedores){

        List<DeudaResponse> deudas = new ArrayList<>();

        int indiceDeudor = 0;
        int indiceAcreedor = 0;

        while (indiceDeudor < deudores.size() && indiceAcreedor < acreedores.size()) {
            
            SaldoPendienteResponse deudor = deudores.get(indiceDeudor);
            SaldoPendienteResponse acreedor = acreedores.get(indiceAcreedor);

            BigDecimal montoDeudor = deudor.getMonto();
            BigDecimal montoAcreedor = acreedor.getMonto();

            BigDecimal monto = montoDeudor.min(montoAcreedor);

            deudas.add(new DeudaResponse(
                            deudor.getMiembroGrupoId(), //Cambie de getUsuarioId() a getMiembroGrupoId()
                            deudor.getNombre(),
                            acreedor.getMiembroGrupoId(), //Cambie de getUsuarioId() a getMiembroGrupoId()
                            acreedor.getNombre(),
                            monto
                    )
            );

            BigDecimal restanteDeudor = montoDeudor.subtract(monto);

            BigDecimal restanteAcreedor = montoAcreedor.subtract(monto);

            deudores.set(
                    indiceDeudor,
                    new SaldoPendienteResponse(
                            deudor.getMiembroGrupoId(), //Cambie de getUsuarioId() a getMiembroGrupoId()
                            deudor.getNombre(),
                            restanteDeudor
                    )
            );

            acreedores.set(
                    indiceAcreedor,
                    new SaldoPendienteResponse(
                            deudor.getMiembroGrupoId(), //Cambie de getUsuarioId() a getMiembroGrupoId()
                            deudor.getNombre(),
                            restanteAcreedor
                    )
            );

            if (restanteDeudor.compareTo(BigDecimal.ZERO) == 0) {
                indiceDeudor++;
            }

            if (restanteAcreedor.compareTo(BigDecimal.ZERO) == 0) {
                indiceAcreedor++;
            }

        }

        return deudas;

    }
    */

}
