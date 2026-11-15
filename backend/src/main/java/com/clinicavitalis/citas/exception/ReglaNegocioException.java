package com.clinicavitalis.citas.exception;

/** La operación incumple una regla de negocio (RN03–RN06). Se responde 422. */
public class ReglaNegocioException extends RuntimeException {

    public ReglaNegocioException(String mensaje) {
        super(mensaje);
    }
}
