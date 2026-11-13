package com.aldo.deuda_cero.dto.grupo;

import com.aldo.deuda_cero.entity.enums.RolGrupo;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CambiarRolMiembroRequest {

    @NotNull(message = "El rol es obligatorio")
    private RolGrupo rol;
}
