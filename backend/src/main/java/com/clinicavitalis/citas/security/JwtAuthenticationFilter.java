package com.clinicavitalis.citas.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

/**
 * Lee la cabecera {@code Authorization: Bearer <token>}, valida el token y,
 * si es correcto, registra al usuario y su rol en el SecurityContext.
 * Si no hay token o no es válido, la petición sigue sin autenticar y
 * Spring Security responde 401 en las rutas protegidas.
 *
 * <p>No se declara como {@code @Component} para que solo se ejecute dentro
 * de la cadena de filtros de Spring Security (ver {@link SecurityConfig}).</p>
 */
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final String PREFIJO = "Bearer ";

    private final JwtService jwtService;

    public JwtAuthenticationFilter(JwtService jwtService) {
        this.jwtService = jwtService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain chain) throws ServletException, IOException {
        String cabecera = request.getHeader(HttpHeaders.AUTHORIZATION);

        if (cabecera != null && cabecera.startsWith(PREFIJO)
                && SecurityContextHolder.getContext().getAuthentication() == null) {
            String token = cabecera.substring(PREFIJO.length()).trim();
            jwtService.validar(token).ifPresent(datos -> {
                var autoridades = List.of(new SimpleGrantedAuthority("ROLE_" + datos.rol()));
                var autenticacion = new UsernamePasswordAuthenticationToken(datos.username(), null, autoridades);
                SecurityContextHolder.getContext().setAuthentication(autenticacion);
            });
        }

        chain.doFilter(request, response);
    }
}
