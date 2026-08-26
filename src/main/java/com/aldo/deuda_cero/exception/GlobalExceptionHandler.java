package com.aldo.deuda_cero.exception;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.aldo.deuda_cero.exception.ApiError.ApiError;

@RestControllerAdvice
public class GlobalExceptionHandler {

    // Validaciones @valid
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiError> handleValidationErrors(MethodArgumentNotValidException ex) {

        List<String> errors = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(error -> error.getDefaultMessage())
                .collect(Collectors.toList());

        ApiError apiError = new ApiError(
                HttpStatus.BAD_REQUEST.value(),
                "Error de validación",
                errors
        );

        return ResponseEntity.badRequest().body(apiError);
    }
    
    // Para errores de autenticación, como credenciales incorrectas
    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ApiError> handleBadCredentials(BadCredentialsException ex) {

        ApiError apiError = new ApiError(
                HttpStatus.UNAUTHORIZED.value(),
                "Credenciales incorrectas",
                null //Aqui lo deje null por un problema que habia con el consumo del endpoint de login en auth.js
        );

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(apiError);
    }

    // Manejo general de excepciones no controladas
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiError> handleGeneral(Exception ex) {

        // Modificacion ya que daba errores en DeudaServiceImpl
        List<String> errores = (ex.getMessage() != null) ? List.of(ex.getMessage()) : Collections.emptyList();
        
        ApiError apiError = new ApiError(
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                "Error interno del servidor",
                errores
        );

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(apiError);
    }


    //***************************************************************************
    // MANEJO DE EXCEPCIONES PERSONALIZADAS
    //***************************************************************************

    // EL EMAIL YA ESTA REGISTRADO
    @ExceptionHandler(EmailExistenteException.class)
    public ResponseEntity<ApiError> handleEmailExistente(EmailExistenteException ex) {

        ApiError apiError = new ApiError(
                HttpStatus.BAD_REQUEST.value(),
                "Email en uso por otro usuario",
                null // Quite la lista para que imprima message directamente  y no un error con formato de lista
        );

        return ResponseEntity.badRequest().body(apiError);
    }

    // NO SE ENCONTRO EL USUARIO BUSCADO
    @ExceptionHandler(UsuarioNoEncontradoException.class)
    public ResponseEntity<ApiError> handleUsuarioNoEncontrado(UsuarioNoEncontradoException ex){

        ApiError apiError = new ApiError(
            HttpStatus.BAD_REQUEST.value(),
            "Usuario no encontrado",
            List.of(ex.getMessage())
        );

        return ResponseEntity.badRequest().body(apiError);
    }

    // NO SE ENCONTRO EN REFRESH TOKEN BUSCADO
    @ExceptionHandler(RefreshTokenNoEncontradoException.class)
    public ResponseEntity<ApiError> handleRefreshTokenNoEncontrado(RefreshTokenNoEncontradoException ex){

        ApiError apiError = new ApiError(
            HttpStatus.BAD_REQUEST.value(),
            "Refresh Token no encontrado",
            List.of(ex.getMessage())
        );

        return ResponseEntity.badRequest().body(apiError);
    }

    // EL REFRESH TOKEN YA EXPIRO
    @ExceptionHandler(RefreshTokenExpiradoException.class)
    public ResponseEntity<ApiError> handleRefreshTokenExpirado(RefreshTokenExpiradoException ex){

        ApiError apiError = new ApiError(
            HttpStatus.BAD_REQUEST.value(),
            "Refresh Token expirado",
            List.of(ex.getMessage())
        );

        return ResponseEntity.badRequest().body(apiError);
    }

    // EL REFRRESH TOKEN NO ES VALIDO
    @ExceptionHandler(RefreshTokenNoValidoException.class)
    public ResponseEntity<ApiError> handleRefreshTokenNoValido(RefreshTokenNoValidoException ex){

        ApiError apiError = new ApiError(
            HttpStatus.BAD_REQUEST.value(),
            "Refresh Token no válido",
            List.of(ex.getMessage())
        );

        return ResponseEntity.badRequest().body(apiError);
    }

    // EL USUARIO NO PERTENECE A UN GRUPO
    @ExceptionHandler(NoPerteneceAlGrupoException.class)
    public ResponseEntity<ApiError> handleNoPerteneceAlGrupo(NoPerteneceAlGrupoException ex){

        ApiError apiError = new ApiError(
            HttpStatus.BAD_REQUEST.value(),
            "No pertenece al grupo",
            List.of(ex.getMessage())
        );

        return ResponseEntity.badRequest().body(apiError);
    }

    // EL MIEMBRO DEL GRUPO NO ES ADMINISTRADOR PARA (borrar, editar nombre, etc.) DEL GRUPO
    @ExceptionHandler(NoPermisosException.class)
    public ResponseEntity<ApiError> handleNoPermisos(NoPermisosException ex){

        ApiError apiError = new ApiError(
            HttpStatus.BAD_REQUEST.value(),
            "Sin permisos para realizar esta acción",
            List.of(ex.getMessage())
        );

        return ResponseEntity.badRequest().body(apiError);
    }

    @ExceptionHandler(InvitacionATiMismoException.class)
    public ResponseEntity<ApiError> handleInvitacionATiMismo(InvitacionATiMismoException ex){

        ApiError apiError = new ApiError(
            HttpStatus.BAD_REQUEST.value(),
            "No te puedes invitar a ti mismo",
            List.of(ex.getMessage())
        );

        return ResponseEntity.badRequest().body(apiError);
    }

    @ExceptionHandler(UsuarioPerteneceAlGrupoException.class)
    public ResponseEntity<ApiError> handleUsuarioPerteneceAlGrupo(UsuarioPerteneceAlGrupoException ex){

        ApiError apiError = new ApiError(
            HttpStatus.BAD_REQUEST.value(),
            "Ya pertenece a este grupo",
            List.of(ex.getMessage())
        );

        return ResponseEntity.badRequest().body(apiError);
    }

    @ExceptionHandler(InvitacionPendienteException.class)
    public ResponseEntity<ApiError> handleInvitacionPendiente(InvitacionPendienteException ex){

        ApiError apiError = new ApiError(
            HttpStatus.BAD_REQUEST.value(),
            "Hay una Invitación pendiente",
            List.of(ex.getMessage())
        );

        return ResponseEntity.badRequest().body(apiError);
    }

    @ExceptionHandler(InvitacionNoEncontrada.class)
    public ResponseEntity<ApiError> handleInvitacionNoEncontrada(InvitacionNoEncontrada ex){

        ApiError apiError = new ApiError(
            HttpStatus.BAD_REQUEST.value(),
            "Invitación no encontrada",
            List.of(ex.getMessage())
        );

        return ResponseEntity.badRequest().body(apiError);
    }

    @ExceptionHandler(NoEsTuInvitacionException.class)
    public ResponseEntity<ApiError> handleNoEsTuInvitacion(NoEsTuInvitacionException ex){

        ApiError apiError = new ApiError(
            HttpStatus.BAD_REQUEST.value(),
            "No es tu invitación",
            List.of(ex.getMessage())
        );

        return ResponseEntity.badRequest().body(apiError);
    }

    @ExceptionHandler(InvitacionRespondidaException.class)
    public ResponseEntity<ApiError> handleInvitacionRespondida(InvitacionRespondidaException ex){

        ApiError apiError = new ApiError(
            HttpStatus.BAD_REQUEST.value(),
            "Ya se respondió la invitación",
            List.of(ex.getMessage())
        );

        return ResponseEntity.badRequest().body(apiError);
    }

    @ExceptionHandler(DeudaNoEncontradaException.class)
    public ResponseEntity<ApiError> handleDeudaNoEncontrada(DeudaNoEncontradaException ex){

        ApiError apiError = new ApiError(
            HttpStatus.BAD_REQUEST.value(),
            "No se encontro la deuda",
            List.of(ex.getMessage())
        );

        return ResponseEntity.badRequest().body(apiError);
    }

    @ExceptionHandler(DeudaNoPerteneceAlGrupoException.class)
    public ResponseEntity<ApiError> handleDeudaNoPerteneceAlGrupo(DeudaNoPerteneceAlGrupoException ex){

        ApiError apiError = new ApiError(
            HttpStatus.BAD_REQUEST.value(),
            "La deuda no pertenece al grupo",
            List.of(ex.getMessage())
        );

        return ResponseEntity.badRequest().body(apiError);
    }

    @ExceptionHandler(MiembroPagadorNoPerteneceAlGrupoException.class)
    public ResponseEntity<ApiError> handleMiembroPagadorNoPerteneceAlGrupo(MiembroPagadorNoPerteneceAlGrupoException ex){

        ApiError apiError = new ApiError(
            HttpStatus.BAD_REQUEST.value(),
            "El pagador no pertenece al grupo",
            List.of(ex.getMessage())
        );

        return ResponseEntity.badRequest().body(apiError);
    }

    @ExceptionHandler(ParticipantesNoPertenecenAlGrupoException.class)
    public ResponseEntity<ApiError> handleParticipantesNoPertenecenAlGrupo(ParticipantesNoPertenecenAlGrupoException ex){

        ApiError apiError = new ApiError(
            HttpStatus.BAD_REQUEST.value(),
            "Uno o mas participantes no pertenecen al grupo",
            List.of(ex.getMessage())
        );

        return ResponseEntity.badRequest().body(apiError);
    }

    @ExceptionHandler(SumaDeMontosNoCoincideConMontoTotalException.class)
    public ResponseEntity<ApiError> handleSumaDeMontosNoCoincideConMontoTotal(SumaDeMontosNoCoincideConMontoTotalException ex){

        ApiError apiError = new ApiError(
            HttpStatus.BAD_REQUEST.value(),
            "La suma de los montos es diferente al monto total",
            List.of(ex.getMessage())
        );

        return ResponseEntity.badRequest().body(apiError);
    }

    @ExceptionHandler(GrupoNoEncontradoException.class)
    public ResponseEntity<ApiError> handleGrupoNoEncontrado(GrupoNoEncontradoException ex){

        ApiError apiError = new ApiError(
            HttpStatus.BAD_REQUEST.value(),
            "Grupo no encontrado",
            List.of(ex.getMessage())
        );

        return ResponseEntity.badRequest().body(apiError);
    }

    @ExceptionHandler(MontoAPagarDebeSerMayorQueCeroException.class)
    public ResponseEntity<ApiError> handleMontoAPagarDebeSerMayorQueCero(MontoAPagarDebeSerMayorQueCeroException ex){

        ApiError apiError = new ApiError(
            HttpStatus.BAD_REQUEST.value(),
            "El pago debe ser mayor que cero",
            List.of(ex.getMessage())
        );

        return ResponseEntity.badRequest().body(apiError);
    }

    @ExceptionHandler(NoPuedesPagarUnaDeudaQueNoTePerteneceException.class)
    public ResponseEntity<ApiError> hadleNoPuedesPagarUnaDeudaQueNoTePertenece(NoPuedesPagarUnaDeudaQueNoTePerteneceException ex){

        ApiError apiError = new ApiError(
            HttpStatus.BAD_REQUEST.value(),
            "Esta deuda no te pertenece, no puedes saldarla",
            List.of(ex.getMessage())
        );

        return ResponseEntity.badRequest().body(apiError);
    }

    @ExceptionHandler(DeudaSaldadaException.class)
    public ResponseEntity<ApiError> hadleDeudaSaldada(DeudaSaldadaException ex){

        ApiError apiError = new ApiError(
            HttpStatus.BAD_REQUEST.value(),
            "La deuda ya esta saldada",
            List.of(ex.getMessage())
        );

        return ResponseEntity.badRequest().body(apiError);
    }

    @ExceptionHandler(PagoNoPuedeSuperarLaDeudaPendiente.class)
    public ResponseEntity<ApiError> handlePagoNoPuedeSuperarLaDeudaPendiente(PagoNoPuedeSuperarLaDeudaPendiente ex){

        ApiError apiError = new ApiError(
            HttpStatus.BAD_REQUEST.value(),
            "El pago no puede superar a la deuda pendiente",
            List.of(ex.getMessage())
        );

        return ResponseEntity.badRequest().body(apiError);
    }

    @ExceptionHandler(MovimientoNoEncontradoException.class)
    public ResponseEntity<ApiError> handleMovimientoNoEncontrado(MovimientoNoEncontradoException ex){

        ApiError apiError = new ApiError(
            HttpStatus.BAD_REQUEST.value(),
            "Movimiento no encontrado",
            List.of(ex.getMessage())
        );

        return ResponseEntity.badRequest().body(apiError);
    }

}
