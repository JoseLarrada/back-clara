package com.proyecto.version1.security;

import com.proyecto.version1.Features.Empleados.Empleado;
import com.proyecto.version1.Features.Empleados.repository.EmpleadosRepository;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class JwtService {

    public static final String TOKEN_TYPE = "token_type";
    private final PrivateKey privateKey;
    private final PublicKey publicKey;
    @Value("${app.security.jwt.access-token-expiration}")
    private long accessTokenExpiration;
    @Value("${app.security.jwt.refresh-token-expiration}")
    private long refreshTokenExpiration;

    // Constantes para mapear de forma segura las llaves del JWT
    public static final String TENANT_ID_CLAIM = "tenant_id";
    public static final String USER_ID_CLAIM = "user_id";
    public static final String ROLES_CLAIM = "roles";
    public static final String CONTEXT_CLAIM = "context";

    public JwtService() throws Exception {
        this.privateKey = KeyUtils.loadPrivateKey("keys/local-only/private_key.pem");
        this.publicKey = KeyUtils.loadPublicKey("keys/local-only/public_key.pem");
    }

    public String generateAccessToken(final Empleado empleado) {
        final Map<String, Object> claims = new HashMap<>();
        claims.put(TOKEN_TYPE, "ACCESS_TOKEN");

        // 1. Núcleo Multi-tenant e Identificación (Mapeando tus UUIDs a String de forma segura)
        claims.put(TENANT_ID_CLAIM, empleado.getEmpresaId() != null ? empleado.getEmpresaId().toString() : null);
        claims.put(USER_ID_CLAIM, empleado.getId() != null ? empleado.getId().toString() : null);

        // 2. Control de Acceso leyendo tus Authorities mapeadas con tu campo "rol"
        if (empleado.getAuthorities() != null) {
            claims.put(ROLES_CLAIM, empleado.getAuthorities().stream()
                    .map(auth -> auth.getAuthority())
                    .collect(Collectors.toList()));
        }

        // 3. Objeto Contexto mapeado a tu campo exacto "nombreCompleto"
        final Map<String, Object> context = new HashMap<>();
        context.put("nombre_completo", empleado.getNombreCompleto());
        context.put("modalidad_perfil", empleado.getModalidadPerfil());

        claims.put(CONTEXT_CLAIM, context);

        return buildToken(empleado.getEmail(), claims, this.accessTokenExpiration);
    }

    public String generateRefreshToken(final String username) {
        final Map<String, Object> claims = Map.of(TOKEN_TYPE, "REFRESH_TOKEN");
        return buildToken(username, claims, this.refreshTokenExpiration);
    }

    public String buildToken(final String username, final Map<String, Object> claims, final long expiration) {
        return Jwts.builder()
                .claims(claims)
                .subject(username)
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + expiration))
                .signWith(this.privateKey)
                .compact();
    }

    public boolean isTokenValid(final String token, final String expectedUsername) {
        final String username = extractUsername(token);
        return username.equals(expectedUsername) && !isTokenExpired(token);
    }

    public String extractUsername(final String token) {
        return extractClaims(token).getSubject();
    }

    private boolean isTokenExpired(final String token) {
        return extractClaims(token).getExpiration()
                .before(new Date());
    }

    public Claims extractClaims(final String token) {
        try {
            return Jwts.parser()
                    .verifyWith(this.publicKey)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
        } catch (final JwtException e) {
            throw new RuntimeException("Invalid token", e);
        }
    }

    public String refreshAccessToken(final String refreshToken, final Empleado empleado) {
        // 1. Extrae los claims usando tu llave pública RSA/EC
        final Claims claims = extractClaims(refreshToken);

        // 2. Valida que el token presentado sea estrictamente un REFRESH_TOKEN
        if (!"REFRESH_TOKEN".equals(claims.get(TOKEN_TYPE, String.class))) {
            throw new RuntimeException("Tipo de token invalido para esta operacion");
        }

        // 3. Valida que el Refresh Token no haya expirado en el tiempo
        if (claims.getExpiration().before(new Date())) {
            throw new RuntimeException("El Refresh Token ha expirado. Inicie sesion nuevamente");
        }

        // 4. CONTROL DE SEGURIDAD: Verifica que el dueño del token coincida con el empleado solicitado
        final String usernameStr = claims.getSubject();
        if (!usernameStr.equals(empleado.getEmail())) {
            throw new RuntimeException("El token no pertenece al usuario autenticado");
        }

        // 5. Éxito: Construye un Access Token fresco con todos sus claims multi-tenant actualizados
        return generateAccessToken(empleado);
    }
}
