package com.aldo.deuda_cero.service.interfaces;

import java.util.List;

import com.aldo.deuda_cero.dto.grupo.AbandonarGrupoResponse;
import com.aldo.deuda_cero.dto.grupo.CambiarRolMiembroRequest;
import com.aldo.deuda_cero.dto.grupo.GrupoRequest;
import com.aldo.deuda_cero.dto.grupo.GrupoResponse;
import com.aldo.deuda_cero.dto.grupo.MiembrosResponse;

public interface GrupoService {
    
    public GrupoResponse crearGrupo(GrupoRequest grupoRequest);
    public List<GrupoResponse> obtenerMisGrupos();
    public List<MiembrosResponse> obtenerMiembros(Long grupoId);
    public AbandonarGrupoResponse abandonarGrupo(Long grupoId);
    public MiembrosResponse cambiarRolMiembro(Long grupoId, Long miembroId, CambiarRolMiembroRequest rol);
}
