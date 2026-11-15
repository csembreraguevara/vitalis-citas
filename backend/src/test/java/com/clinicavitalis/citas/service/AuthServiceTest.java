package com.clinicavitalis.citas.service;

import com.clinicavitalis.citas.dto.LoginRequestDTO;
import com.clinicavitalis.citas.dto.TokenResponseDTO;
import com.clinicavitalis.citas.entity.Auditoria;
import com.clinicavitalis.citas.entity.Rol;
import com.clinicavitalis.citas.entity.Usuario;
import com.clinicavitalis.citas.exception.CredencialesInvalidasException;
import com.clinicavitalis.citas.exception.CuentaBloqueadaException;
import com.clinicavitalis.citas.repository.AuditoriaRepository;
import com.clinicavitalis.citas.repository.UsuarioRepository;
import com.clinicavitalis.citas.security.JwtService;
import com.clinicavitalis.citas.service.impl.AuthServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Pruebas unitarias del inicio de sesión (RF002, CU001), escritas antes de
 * la implementación siguiendo TDD. El reloj está fijo para que el bloqueo
 * de 15 minutos sea verificable.
 */
@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    private static final ZoneId LIMA = ZoneId.of("America/Lima");
    private static final Clock RELOJ = Clock.fixed(Instant.parse("2026-10-09T15:00:00Z"), LIMA);
    private static final LocalDateTime AHORA = LocalDateTime.now(RELOJ);
    private static final String SECRETO = "dml0YWxpcy1jaXRhcy1jbGF2ZS1kZS1wcnVlYmFzLXVuaXRhcmlhcy0yMDI2LTEyMzQ=";
    private static final String CLAVE = "Vitalis2026";
    private static final String IP = "127.0.0.1";

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private AuditoriaRepository auditoriaRepository;

    private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder(4);
    private JwtService jwtService;
    private AuthService authService;
    private Usuario recepcionista;

    @BeforeEach
    void setUp() {
        jwtService = new JwtService(SECRETO, 60, RELOJ);
        authService = new AuthServiceImpl(usuarioRepository, auditoriaRepository, passwordEncoder,
                jwtService, RELOJ, 5, 15);
        recepcionista = new Usuario("recepcion01", passwordEncoder.encode(CLAVE), Rol.RECEPCIONISTA);
    }

    @Test
    @DisplayName("Credenciales correctas: emite un JWT con el rol y registra el acceso en auditoría")
    void loginCorrectoEmiteTokenYRegistraAuditoria() {
        when(usuarioRepository.findByUsername("recepcion01")).thenReturn(Optional.of(recepcionista));

        TokenResponseDTO respuesta = authService.login(new LoginRequestDTO("recepcion01", CLAVE), IP);

        assertThat(respuesta.token()).isNotBlank();
        assertThat(respuesta.tipo()).isEqualTo("Bearer");
        assertThat(respuesta.rol()).isEqualTo("RECEPCIONISTA");
        assertThat(respuesta.expiraEnSegundos()).isEqualTo(3600);
        assertThat(jwtService.validar(respuesta.token()))
                .hasValueSatisfying(datos -> {
                    assertThat(datos.username()).isEqualTo("recepcion01");
                    assertThat(datos.rol()).isEqualTo("RECEPCIONISTA");
                });

        ArgumentCaptor<Auditoria> auditoria = ArgumentCaptor.forClass(Auditoria.class);
        verify(auditoriaRepository).save(auditoria.capture());
        assertThat(auditoria.getValue().getAccion()).isEqualTo("LOGIN");
        assertThat(auditoria.getValue().getIp()).isEqualTo(IP);
        assertThat(recepcionista.getUltimoAcceso()).isEqualTo(AHORA);
    }

    @Test
    @DisplayName("Credenciales correctas: reinicia el contador de intentos fallidos")
    void loginCorrectoReiniciaIntentos() {
        recepcionista.registrarIntentoFallido(5, 15, AHORA);
        recepcionista.registrarIntentoFallido(5, 15, AHORA);
        when(usuarioRepository.findByUsername("recepcion01")).thenReturn(Optional.of(recepcionista));

        authService.login(new LoginRequestDTO("recepcion01", CLAVE), IP);

        assertThat(recepcionista.getIntentosFallidos()).isZero();
    }

    @Test
    @DisplayName("Contraseña incorrecta: suma un intento y responde «Usuario o contraseña incorrectos»")
    void contrasenaIncorrectaSumaIntento() {
        when(usuarioRepository.findByUsername("recepcion01")).thenReturn(Optional.of(recepcionista));

        assertThatThrownBy(() -> authService.login(new LoginRequestDTO("recepcion01", "otraClave"), IP))
                .isInstanceOf(CredencialesInvalidasException.class)
                .hasMessage("Usuario o contraseña incorrectos");

        assertThat(recepcionista.getIntentosFallidos()).isEqualTo(1);
        verify(usuarioRepository).save(recepcionista);
        verify(auditoriaRepository, never()).save(any());
    }

    @Test
    @DisplayName("Quinto intento fallido: bloquea la cuenta durante 15 minutos")
    void quintoIntentoFallidoBloqueaQuinceMinutos() {
        when(usuarioRepository.findByUsername("recepcion01")).thenReturn(Optional.of(recepcionista));
        LoginRequestDTO incorrecto = new LoginRequestDTO("recepcion01", "otraClave");

        for (int intento = 1; intento <= 4; intento++) {
            assertThatThrownBy(() -> authService.login(incorrecto, IP))
                    .isInstanceOf(CredencialesInvalidasException.class);
        }
        assertThatThrownBy(() -> authService.login(incorrecto, IP))
                .isInstanceOf(CuentaBloqueadaException.class);

        assertThat(recepcionista.getBloqueadoHasta()).isEqualTo(AHORA.plusMinutes(15));
        assertThat(recepcionista.estaBloqueado(AHORA)).isTrue();
    }

    @Test
    @DisplayName("Cuenta bloqueada: rechaza el ingreso aunque la contraseña sea correcta")
    void cuentaBloqueadaRechazaContrasenaCorrecta() {
        for (int intento = 1; intento <= 5; intento++) {
            recepcionista.registrarIntentoFallido(5, 15, AHORA);
        }
        when(usuarioRepository.findByUsername("recepcion01")).thenReturn(Optional.of(recepcionista));

        assertThatThrownBy(() -> authService.login(new LoginRequestDTO("recepcion01", CLAVE), IP))
                .isInstanceOf(CuentaBloqueadaException.class);
    }

    @Test
    @DisplayName("Usuario inexistente: responde el mismo mensaje genérico")
    void usuarioInexistente() {
        when(usuarioRepository.findByUsername("noexiste")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> authService.login(new LoginRequestDTO("noexiste", CLAVE), IP))
                .isInstanceOf(CredencialesInvalidasException.class)
                .hasMessage("Usuario o contraseña incorrectos");
    }

    @Test
    @DisplayName("Usuario inactivo: no puede iniciar sesión")
    void usuarioInactivo() {
        recepcionista.setActivo(false);
        when(usuarioRepository.findByUsername("recepcion01")).thenReturn(Optional.of(recepcionista));

        assertThatThrownBy(() -> authService.login(new LoginRequestDTO("recepcion01", CLAVE), IP))
                .isInstanceOf(CredencialesInvalidasException.class);
    }
}
