package com.clinicavitalis.citas.exception;

/** El recurso pedido no existe o no está activo. Se responde 404. */
public class RecursoNoEncontradoException extends RuntimeException {

    public RecursoNoEncontradoException(String mensaje) {
        super(mensaje);
    }
}
