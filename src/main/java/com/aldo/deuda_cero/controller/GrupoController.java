package com.aldo.deuda_cero.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.aldo.deuda_cero.dto.grupo.GrupoRequest;
import com.aldo.deuda_cero.dto.grupo.GrupoResponse;
import com.aldo.deuda_cero.dto.grupo.MiembrosResponse;
import com.aldo.deuda_cero.service.interfaces.GrupoService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/grupos")
@RequiredArgsConstructor
public class GrupoController {

    private final GrupoService grupoService;
    
    @PostMapping("/crear")
    public ResponseEntity<GrupoResponse> crearGrupo(@Valid @RequestBody GrupoRequest grupoRequest){
        return ResponseEntity.status(HttpStatus.CREATED).body(grupoService.crearGrupo(grupoRequest));
    }

    @GetMapping("/grupos")
    public ResponseEntity<List<GrupoResponse>> obtenerMisGrupos(){
        return ResponseEntity.ok(grupoService.obtenerMisGrupos());
    }

    //Este si es de este controller
    @GetMapping("/{grupoId}/miembros")
    public ResponseEntity<List<MiembrosResponse>> obtenerMiembros(@PathVariable Long grupoId){
        return ResponseEntity.ok(grupoService.obtenerMiembros(grupoId));
    }
}
