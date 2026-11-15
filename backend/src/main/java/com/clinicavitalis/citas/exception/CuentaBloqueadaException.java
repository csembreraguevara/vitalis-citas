package com.clinicavitalis.citas.exception;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/** La cuenta está bloqueada temporalmente por intentos fallidos (CU001, 3b). */
public class CuentaBloqueadaException extends RuntimeException {

    private static final DateTimeFormatter HORA = DateTimeFormatter.ofPattern("HH:mm");

    public CuentaBloqueadaException(LocalDateTime bloqueadoHasta) {
        super("Cuenta bloqueada por intentos fallidos. Intente nuevamente después de las "
                + bloqueadoHasta.format(HORA));
    }
}
