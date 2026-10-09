package com.clinicavitalis.citas.dto;

import java.util.List;

/** Formato uniforme de los errores que devuelve la API (RNF010). */
public record ErrorResponseDTO(
        int status,
        String error,
        String mensaje,
        List<String> detalles,
        String fecha) {
}
