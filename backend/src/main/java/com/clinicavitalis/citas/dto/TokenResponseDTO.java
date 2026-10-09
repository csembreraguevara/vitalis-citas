package com.clinicavitalis.citas.dto;

/**
 * Respuesta de un inicio de sesión exitoso. Nunca incluye el hash de la
 * contraseña ni otros datos de la entidad {@code Usuario}.
 */
public record TokenResponseDTO(
        String token,
        String tipo,
        long expiraEnSegundos,
        String username,
        String rol) {
}
