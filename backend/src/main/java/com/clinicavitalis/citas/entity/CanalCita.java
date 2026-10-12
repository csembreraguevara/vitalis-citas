package com.clinicavitalis.citas.entity;

/** Por dónde se registró la cita (columna {@code cita.canal}). */
public enum CanalCita {
    /** El paciente la reservó en el portal web. */
    PORTAL,
    /** La registró el personal de recepción. */
    RECEPCION
}
