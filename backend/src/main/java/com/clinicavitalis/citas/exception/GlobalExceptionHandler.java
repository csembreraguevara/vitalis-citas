package com.clinicavitalis.citas.exception;

import com.clinicavitalis.citas.dto.ErrorResponseDTO;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.List;

/** Manejo centralizado de errores de la API (RNF010). */
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(CredencialesInvalidasException.class)
    public ResponseEntity<ErrorResponseDTO> credencialesInvalidas(CredencialesInvalidasException ex) {
        return respuesta(HttpStatus.UNAUTHORIZED, ex.getMessage(), List.of());
    }

    @ExceptionHandler(CuentaBloqueadaException.class)
    public ResponseEntity<ErrorResponseDTO> cuentaBloqueada(CuentaBloqueadaException ex) {
        return respuesta(HttpStatus.LOCKED, ex.getMessage(), List.of());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponseDTO> datosInvalidos(MethodArgumentNotValidException ex) {
        List<String> detalles = ex.getBindingResult().getFieldErrors().stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .toList();
        return respuesta(HttpStatus.BAD_REQUEST, "Datos de entrada inválidos", detalles);
    }

    private ResponseEntity<ErrorResponseDTO> respuesta(HttpStatus status, String mensaje, List<String> detalles) {
        ErrorResponseDTO cuerpo = new ErrorResponseDTO(
                status.value(), status.getReasonPhrase(), mensaje, detalles, LocalDateTime.now().toString());
        return ResponseEntity.status(status).body(cuerpo);
    }
}
