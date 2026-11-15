package com.clinicavitalis.citas.service;

import com.clinicavitalis.citas.dto.LoginRequestDTO;
import com.clinicavitalis.citas.dto.TokenResponseDTO;

/** Autenticación de usuarios (RF002, CU001). */
public interface AuthService {

    /**
     * Valida las credenciales y emite un token JWT.
     *
     * @param solicitud usuario y contraseña
     * @param ip        dirección IP del cliente, para la auditoría
     * @return el token y los datos básicos del usuario
     * @throws com.clinicavitalis.citas.exception.CredencialesInvalidasException
     *         si el usuario no existe, está inactivo o la contraseña es incorrecta
     * @throws com.clinicavitalis.citas.exception.CuentaBloqueadaException
     *         si la cuenta está bloqueada por intentos fallidos
     */
    TokenResponseDTO login(LoginRequestDTO solicitud, String ip);
}
