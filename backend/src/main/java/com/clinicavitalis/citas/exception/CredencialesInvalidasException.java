package com.clinicavitalis.citas.exception;

/**
 * Usuario inexistente, inactivo o contraseña incorrecta. El mensaje es
 * siempre el mismo para no revelar cuál de los datos falló (CU001, 3a).
 */
public class CredencialesInvalidasException extends RuntimeException {

    public CredencialesInvalidasException() {
        super("Usuario o contraseña incorrectos");
    }
}
