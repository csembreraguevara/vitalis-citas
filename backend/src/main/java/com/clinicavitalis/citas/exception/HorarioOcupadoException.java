package com.clinicavitalis.citas.exception;

/** El turno ya lo tiene otra cita activa (RN01). Se responde 409. */
public class HorarioOcupadoException extends RuntimeException {

    public HorarioOcupadoException() {
        super("Ese horario acaba de ser reservado. Elige otro turno.");
    }
}
