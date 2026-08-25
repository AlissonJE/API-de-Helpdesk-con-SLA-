package com.helpdesk.exceptions;

import com.helpdesk.dto.ErrorResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.List;

@RestControllerAdvice
public class GlobalExceptionHandler {

    // 400 - Validaciones de @Valid (email, password, titulo, descripcion, enums invalidos, etc.)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidation(MethodArgumentNotValidException ex) {
        List<String> detalles = ex.getBindingResult().getFieldErrors().stream()
                .map(e -> e.getField() + ": " + e.getDefaultMessage())
                .toList();
        return construir(HttpStatus.BAD_REQUEST, "Datos invalidos", detalles);
    }

    // 400 - JSON malformado o enum invalido (prioridad/estado con valor fuera del enum)
    @ExceptionHandler(org.springframework.http.converter.HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> handleNoLegible(org.springframework.http.converter.HttpMessageNotReadableException ex) {
        return construir(HttpStatus.BAD_REQUEST, "Cuerpo de la peticion invalido o valor de enum invalido", null);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgument(IllegalArgumentException ex) {
        return construir(HttpStatus.BAD_REQUEST, ex.getMessage(), null);
    }

    // 401 - Credenciales invalidas en login
    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ErrorResponse> handleBadCredentials(BadCredentialsException ex) {
        return construir(HttpStatus.UNAUTHORIZED, "Credenciales invalidas", null);
    }

    // 401 - Token de acceso o refresh invalido/expirado/revocado
    @ExceptionHandler(TokenInvalidoException.class)
    public ResponseEntity<ErrorResponse> handleTokenInvalido(TokenInvalidoException ex) {
        return construir(HttpStatus.UNAUTHORIZED, ex.getMessage(), null);
    }

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ErrorResponse> handleAuthException(AuthenticationException ex) {
        return construir(HttpStatus.UNAUTHORIZED, "No autenticado", null);
    }

    // 403 - Rol insuficiente
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResponse> handleAccessDenied(AccessDeniedException ex) {
        return construir(HttpStatus.FORBIDDEN, "No tiene permisos para acceder a este recurso", null);
    }

    // 404 - Recurso no encontrado
    @ExceptionHandler(RecursoNoEncontradoException.class)
    public ResponseEntity<ErrorResponse> handleNoEncontrado(RecursoNoEncontradoException ex) {
        return construir(HttpStatus.NOT_FOUND, ex.getMessage(), null);
    }

    // 409 - Email ya registrado
    @ExceptionHandler(EmailYaRegistradoException.class)
    public ResponseEntity<ErrorResponse> handleEmailDuplicado(EmailYaRegistradoException ex) {
        return construir(HttpStatus.CONFLICT, ex.getMessage(), null);
    }

    // 500 - fallback
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGenerico(Exception ex) {
        return construir(HttpStatus.INTERNAL_SERVER_ERROR, "Error interno del servidor: " + ex.getMessage(), null);
    }

    private ResponseEntity<ErrorResponse> construir(HttpStatus status, String mensaje, List<String> detalles) {
        ErrorResponse body = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(status.value())
                .error(status.getReasonPhrase())
                .mensaje(mensaje)
                .detalles(detalles)
                .build();
        return ResponseEntity.status(status).body(body);
    }
}
