package com.aldo.deuda_cero.exception;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import com.aldo.deuda_cero.exception.ApiError.ApiError;

import lombok.extern.slf4j.Slf4j;

@Slf4j
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

    // Cuenta desactivada
    @ExceptionHandler(DisabledException.class)
    public ResponseEntity<ApiError> handleDisabled(DisabledException ex) {

        ApiError apiError = new ApiError(
                HttpStatus.FORBIDDEN.value(),
                "Cuenta desactivada",
                List.of(ex.getMessage())
        );

        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(apiError);
    }

    // Cuenta bloqueada
    @ExceptionHandler(LockedException.class)
    public ResponseEntity<ApiError> handleLocked(LockedException ex) {

        ApiError apiError = new ApiError(
                HttpStatus.FORBIDDEN.value(),
                "Cuenta bloqueada",
                List.of(ex.getMessage())
        );

        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(apiError);
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

    // Cualquier otro error de autenticacion no controlado
    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ApiError> handleAuthentication(AuthenticationException ex) {

        log.warn("Error de autenticación: {}", ex.getMessage());

        ApiError apiError = new ApiError(
                HttpStatus.UNAUTHORIZED.value(),
                "No autenticado",
                null
        );

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(apiError);
    }

    // Peticion con JSON mal formado o con un valor de enum inexistente
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiError> handleMessageNotReadable(HttpMessageNotReadableException ex) {

        log.warn("Peticion con cuerpo ilegible: {}", ex.getMessage());

        ApiError apiError = new ApiError(
                HttpStatus.BAD_REQUEST.value(),
                "El cuerpo de la petición no es válido o tiene un formato incorrecto",
                null
        );

        return ResponseEntity.badRequest().body(apiError);
    }

    // Un parametro de la URL no se pudo convertir al tipo esperado
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ApiError> handleTypeMismatch(MethodArgumentTypeMismatchException ex) {

        String mensaje = "El parámetro '" + ex.getName() + "' tiene un valor inválido";

        ApiError apiError = new ApiError(
                HttpStatus.BAD_REQUEST.value(),
                mensaje,
                List.of(mensaje)
        );

        return ResponseEntity.badRequest().body(apiError);
    }

    // Acceso denegado por @PreAuthorize o por reglas de seguridad
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiError> handleAccessDenied(AccessDeniedException ex) {

        ApiError apiError = new ApiError(
                HttpStatus.FORBIDDEN.value(),
                "No tienes permisos para realizar esta acción",
                null
        );

        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(apiError);
    }

    // Conflictos de integridad de datos (duplicados, constraints, etc.)
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ApiError> handleDataIntegrityViolation(DataIntegrityViolationException ex) {

        log.warn("Conflicto de integridad de datos: {}", ex.getMostSpecificCause().getMessage());

        ApiError apiError = new ApiError(
                HttpStatus.CONFLICT.value(),
                "No se pudo completar la operación por un conflicto con los datos",
                null
        );

        return ResponseEntity.status(HttpStatus.CONFLICT).body(apiError);
    }

    // Manejo general de excepciones no controladas
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiError> handleGeneral(Exception ex) {

        log.error("Error no controlado", ex);

        ApiError apiError = new ApiError(
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                "Error interno del servidor",
                null
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
            HttpStatus.NOT_FOUND.value(),
            "No pertenece al grupo",
            List.of(ex.getMessage())
        );

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(apiError);
    }

    // EL MIEMBRO DEL GRUPO NO ES ADMINISTRADOR PARA (borrar, editar nombre, etc.) DEL GRUPO
    @ExceptionHandler(NoPermisosException.class)
    public ResponseEntity<ApiError> handleNoPermisos(NoPermisosException ex){

        ApiError apiError = new ApiError(
            HttpStatus.FORBIDDEN.value(),
            "Sin permisos para realizar esta acción",
            List.of(ex.getMessage())
        );

        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(apiError);
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
            HttpStatus.NOT_FOUND.value(),
            "No se encontro la deuda",
            List.of(ex.getMessage())
        );

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(apiError);
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

    @ExceptionHandler(MontoParticipanteInvalidoException.class)
    public ResponseEntity<ApiError> handleMontoParticipanteInvalido(MontoParticipanteInvalidoException ex){

        ApiError apiError = new ApiError(
            HttpStatus.BAD_REQUEST.value(),
            "Monto de participante inválido",
            List.of(ex.getMessage())
        );

        return ResponseEntity.badRequest().body(apiError);
    }

    @ExceptionHandler(GrupoNoEncontradoException.class)
    public ResponseEntity<ApiError> handleGrupoNoEncontrado(GrupoNoEncontradoException ex){

        ApiError apiError = new ApiError(
            HttpStatus.NOT_FOUND.value(),
            "Grupo no encontrado",
            List.of(ex.getMessage())
        );

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(apiError);
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
