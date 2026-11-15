package com.clinicavitalis.citas.entity;

/**
 * Roles de acceso al sistema (RF003). Coinciden con el ENUM de la
 * columna {@code usuario.rol} de la base de datos.
 */
public enum Rol {
    PACIENTE,
    RECEPCIONISTA,
    MEDICO,
    ADMIN
}
