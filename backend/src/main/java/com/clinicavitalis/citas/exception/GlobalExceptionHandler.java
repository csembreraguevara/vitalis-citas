package com.clinicavitalis.citas.exception;

import com.clinicavitalis.citas.dto.ErrorResponseDTO;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
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

    @ExceptionHandler(RecursoNoEncontradoException.class)
    public ResponseEntity<ErrorResponseDTO> noEncontrado(RecursoNoEncontradoException ex) {
        return respuesta(HttpStatus.NOT_FOUND, ex.getMessage(), List.of());
    }

    @ExceptionHandler(ReglaNegocioException.class)
    public ResponseEntity<ErrorResponseDTO> reglaNegocio(ReglaNegocioException ex) {
        return respuesta(HttpStatus.valueOf(422), ex.getMessage(), List.of());
    }

    @ExceptionHandler(HorarioOcupadoException.class)
    public ResponseEntity<ErrorResponseDTO> horarioOcupado(HorarioOcupadoException ex) {
        return respuesta(HttpStatus.CONFLICT, ex.getMessage(), List.of());
    }

    /** Dos personas modificaron la misma cita a la vez (@Version). */
    @ExceptionHandler(ObjectOptimisticLockingFailureException.class)
    public ResponseEntity<ErrorResponseDTO> conflictoConcurrencia(ObjectOptimisticLockingFailureException ex) {
        return respuesta(HttpStatus.CONFLICT, "La cita cambió mientras la editabas. Vuelve a cargarla.", List.of());
    }

    @ExceptionHandler({MissingServletRequestParameterException.class, MethodArgumentTypeMismatchException.class,
            HttpMessageNotReadableException.class})
    public ResponseEntity<ErrorResponseDTO> peticionMalFormada(Exception ex) {
        return respuesta(HttpStatus.BAD_REQUEST, "La petición tiene parámetros faltantes o con formato incorrecto",
                List.of());
    }

    private ResponseEntity<ErrorResponseDTO> respuesta(HttpStatus status, String mensaje, List<String> detalles) {
        ErrorResponseDTO cuerpo = new ErrorResponseDTO(
                status.value(), status.getReasonPhrase(), mensaje, detalles, LocalDateTime.now().toString());
        return ResponseEntity.status(status).body(cuerpo);
    }
}