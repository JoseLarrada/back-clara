package com.proyecto.version1.security;

import com.proyecto.version1.Features.Empleados.Empleado;
import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class JwtFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private static final Logger log = LoggerFactory.getLogger(JwtFilter.class);

    @Override
    protected void doFilterInternal(
            @NonNull final HttpServletRequest request,
            @NonNull final HttpServletResponse response,
            @NonNull final FilterChain filterChain) throws ServletException, IOException {

        if (request.getServletPath().contains("/api/v1/auth")) {
            filterChain.doFilter(request, response);
            return;
        }

        final String authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        final String jwt = authHeader.substring(7);

        try {
            try {
                // 1. Extraemos los Claims de la firma asimétrica (Valida llave pública)
                final Claims claims = this.jwtService.extractClaims(jwt);
                final String username = claims.getSubject();

                if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {

                    // 2. EXTRAER E INYECTAR EL TENANT ID (Aislamiento de Datos)
                    final String tenantIdStr = claims.get(JwtService.TENANT_ID_CLAIM, String.class);
                    if (tenantIdStr != null) {
                        TenantContext.setCurrentTenant(UUID.fromString(tenantIdStr));
                    }

                    // 3. AUTENTICACIÓN DIRECTA DESDE EL JWT (Sin tocar la BD)
                    @SuppressWarnings("unchecked")
                    final List<String> roles = claims.get(JwtService.ROLES_CLAIM, List.class);
                    final List<SimpleGrantedAuthority> authorities = roles != null ? roles.stream()
                            .map(SimpleGrantedAuthority::new)
                            .collect(Collectors.toList()) : List.of();

                    final String userIdStr = claims.get(JwtService.USER_ID_CLAIM, String.class);
                    // Construimos el principal mínimo: un objeto Empleado ligero para evitar ClassCastException en auditoría
                    Empleado principal = Empleado.builder()
                            .id(userIdStr != null ? UUID.fromString(userIdStr) : null)
                            .email(username)
                            .rol(roles != null && !roles.isEmpty() ? roles.get(0) : "EMPLEADO")
                            .empresaId(tenantIdStr != null ? UUID.fromString(tenantIdStr) : null)
                            .activo(true)
                            .build();

                    final UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                            principal,
                            null,
                            authorities);

                    authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authToken);
                }
            } catch (final Exception e) {
                // Log seguro: no imprimir el token entero. Registramos el tipo y mensaje para diagnóstico.
                try {
                    String method = request.getMethod();
                    String path = request.getRequestURI();
                    log.warn("JWT validation failed for request {} {}: {} - {}", method, path, e.getClass().getSimpleName(), e.getMessage());
                } catch (Exception ignored) {
                    // no-op
                }

                // Si el token es inválido o expiró, denegamos el acceso limpiamente
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.getWriter().write("Acceso denegado: Token invalido o expirado.");
                return;
            }

            filterChain.doFilter(request, response);

        } finally {
            // 4. CRÍTICO: Limpiar el ThreadLocal para evitar fugas de memoria en el servidor
            TenantContext.clear();
        }
    }
}
