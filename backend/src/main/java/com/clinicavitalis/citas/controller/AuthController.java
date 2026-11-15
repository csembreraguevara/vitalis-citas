package com.clinicavitalis.citas.controller;

import com.clinicavitalis.citas.dto.LoginRequestDTO;
import com.clinicavitalis.citas.dto.TokenResponseDTO;
import com.clinicavitalis.citas.dto.UsuarioActualDTO;
import com.clinicavitalis.citas.service.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Endpoints de autenticación (RF002). El controlador solo traduce
 * HTTP ⇄ DTO y delega la lógica en {@link AuthService} (MVC).
 */
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    /** Inicia sesión y devuelve el token JWT. Ruta pública. */
    @PostMapping("/login")
    public TokenResponseDTO login(@Valid @RequestBody LoginRequestDTO solicitud, HttpServletRequest request) {
        return authService.login(solicitud, request.getRemoteAddr());
    }

    /** Devuelve el usuario dueño del token. Requiere un token válido. */
    @GetMapping("/me")
    public UsuarioActualDTO usuarioActual(Authentication autenticacion) {
        String rol = autenticacion.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .map(autoridad -> autoridad.replaceFirst("^ROLE_", ""))
                .findFirst()
                .orElse("");
        return new UsuarioActualDTO(autenticacion.getName(), rol);
    }
}
