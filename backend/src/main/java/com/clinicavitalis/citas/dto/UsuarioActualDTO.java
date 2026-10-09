package com.clinicavitalis.citas.dto;

/** Datos del usuario autenticado, obtenidos del token JWT. */
public record UsuarioActualDTO(String username, String rol) {
}
