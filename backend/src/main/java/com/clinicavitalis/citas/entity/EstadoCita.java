package com.clinicavitalis.citas.entity;

/**
 * Estados por los que pasa una cita (columna {@code cita.estado}).
 * CANCELADA y EXPIRADA liberan el turno para otra reserva.
 */
public enum EstadoCita {
    PENDIENTE,
    CONFIRMADA,
    ATENDIDA,
    CANCELADA,
    NO_ASISTIO,
    EXPIRADA;

    /** Indica si la cita todavía ocupa el turno del médico. */
    public boolean ocupaTurno() {
        return this != CANCELADA && this != EXPIRADA;
    }
}
