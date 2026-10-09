package com.clinicavitalis.citas.service.impl;

import com.clinicavitalis.citas.dto.LoginRequestDTO;
import com.clinicavitalis.citas.dto.TokenResponseDTO;
import com.clinicavitalis.citas.entity.Auditoria;
import com.clinicavitalis.citas.entity.Usuario;
import com.clinicavitalis.citas.exception.CredencialesInvalidasException;
import com.clinicavitalis.citas.exception.CuentaBloqueadaException;
import com.clinicavitalis.citas.repository.AuditoriaRepository;
import com.clinicavitalis.citas.repository.UsuarioRepository;
import com.clinicavitalis.citas.security.JwtService;
import com.clinicavitalis.citas.service.AuthService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDateTime;

/**
 * Implementación del inicio de sesión (RF002, CU001):
 * <ol>
 *   <li>Busca el usuario; si no existe o está inactivo: credenciales inválidas.</li>
 *   <li>Si la cuenta está bloqueada: rechaza aunque la contraseña sea correcta.</li>
 *   <li>Compara la contraseña con el hash BCrypt.</li>
 *   <li>Contraseña incorrecta: suma un intento; al quinto bloquea 15 minutos.</li>
 *   <li>Contraseña correcta: reinicia los intentos, registra el acceso en
 *       auditoría y emite el JWT.</li>
 * </ol>
 */
@Service
public class AuthServiceImpl implements AuthService {

    private static final Logger log = LoggerFactory.getLogger(AuthServiceImpl.class);

    private final UsuarioRepository usuarioRepository;
    private final AuditoriaRepository auditoriaRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final Clock clock;
    private final int maxIntentos;
    private final int minutosBloqueo;

    public AuthServiceImpl(UsuarioRepository usuarioRepository,
                           AuditoriaRepository auditoriaRepository,
                           PasswordEncoder passwordEncoder,
                           JwtService jwtService,
                           Clock clock,
                           @Value("${app.login.max-intentos}") int maxIntentos,
                           @Value("${app.login.minutos-bloqueo}") int minutosBloqueo) {
        this.usuarioRepository = usuarioRepository;
        this.auditoriaRepository = auditoriaRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.clock = clock;
        this.maxIntentos = maxIntentos;
        this.minutosBloqueo = minutosBloqueo;
    }

    // noRollbackFor: el intento fallido o el bloqueo deben guardarse aunque
    // el método termine lanzando la excepción.
    @Override
    @Transactional(noRollbackFor = {CredencialesInvalidasException.class, CuentaBloqueadaException.class})
    public TokenResponseDTO login(LoginRequestDTO solicitud, String ip) {
        LocalDateTime ahora = LocalDateTime.now(clock);
        String username = solicitud.username().trim();

        Usuario usuario = usuarioRepository.findByUsername(username)
                .filter(Usuario::isActivo)
                .orElseThrow(() -> {
                    log.warn("Intento de inicio de sesión con usuario inexistente o inactivo");
                    return new CredencialesInvalidasException();
                });

        if (usuario.estaBloqueado(ahora)) {
            throw new CuentaBloqueadaException(usuario.getBloqueadoHasta());
        }

        if (!passwordEncoder.matches(solicitud.password(), usuario.getPasswordHash())) {
            usuario.registrarIntentoFallido(maxIntentos, minutosBloqueo, ahora);
            usuarioRepository.save(usuario);
            if (usuario.estaBloqueado(ahora)) {
                auditoriaRepository.save(new Auditoria(usuario.getId(), "BLOQUEO_CUENTA", "usuario",
                        usuario.getId(), "Bloqueo por " + maxIntentos + " intentos fallidos", ip));
                log.warn("Cuenta {} bloqueada por intentos fallidos", usuario.getId());
                throw new CuentaBloqueadaException(usuario.getBloqueadoHasta());
            }
            throw new CredencialesInvalidasException();
        }

        usuario.registrarAccesoExitoso(ahora);
        usuarioRepository.save(usuario);
        auditoriaRepository.save(new Auditoria(usuario.getId(), "LOGIN", "usuario",
                usuario.getId(), "Inicio de sesión exitoso", ip));
        log.info("Inicio de sesión exitoso del usuario {}", usuario.getId());

        String rol = usuario.getRol().name();
        String token = jwtService.generarToken(usuario.getUsername(), rol);
        return new TokenResponseDTO(token, "Bearer", jwtService.getVigenciaEnSegundos(), usuario.getUsername(), rol);
    }
}
