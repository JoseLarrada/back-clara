package com.proyecto.version1.auth.service;

import com.proyecto.version1.Features.Empleados.Empleado;
import com.proyecto.version1.Features.Empleados.EmpleadoMapper;
import com.proyecto.version1.Features.Empleados.repository.EmpleadosRepository;
import com.proyecto.version1.auth.request.AuthenticationRequest;
import com.proyecto.version1.auth.request.RefreshRequest;
import com.proyecto.version1.auth.request.RegistrationRequest;
import com.proyecto.version1.auth.response.AuthenticationResponse;
import com.proyecto.version1.security.JwtService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthenticationServiceImpl implements AuthenticationService {

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final EmpleadosRepository empleadoRepository;
    private final EmpleadoMapper empleadoMapper;

    @Override
    public ResponseEntity<AuthenticationResponse> login(AuthenticationRequest request) {
        final Authentication auth = this.authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.email(),
                        request.password()
                )
        );

        final Empleado empleado = (Empleado) auth.getPrincipal();

        assert empleado != null;
        final String token = this.jwtService.generateAccessToken(empleado);
        final String refreshToken = this.jwtService.generateRefreshToken(empleado.getEmail());
        final String tokenType = "Bearer";
        return ResponseEntity.ok(AuthenticationResponse.builder()
                .accessToken(token)
                .refreshToken(refreshToken)
                .tokenType(tokenType)
                .build());
    }

    @Override
    @Transactional
    public void register(RegistrationRequest request) {
        checkUserEmail(request.email());
        checkPasswords(request.passwordHash(), request.confirmPassword());

        final Empleado user = this.empleadoMapper.toEmpleado(request);

        this.empleadoRepository.save(user);
    }

    @Override
    public AuthenticationResponse refreshToken(RefreshRequest req) {
        // 1. Extraemos el email del subject del Refresh Token de forma segura
        final String email = this.jwtService.extractUsername(req.refreshToken());

        // 2. Buscamos al empleado de forma global en la tabla compartida
        final Empleado empleado = this.empleadoRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado en el sistema"));

        // 3. Invocamos el JwtService pasándole ambos parámetros (¡Adiós error de compilación!)
        final String newAccessToken = this.jwtService.refreshAccessToken(req.refreshToken(), empleado);
        final String tokenType = "Bearer";

        return AuthenticationResponse.builder()
                .accessToken(newAccessToken)
                .refreshToken(req.refreshToken())
                .tokenType(tokenType)
                .build();
    }

    private void checkUserEmail(final String email) {
        final boolean emailExists = this.empleadoRepository.existsByEmailIgnoreCase(email);
        if (emailExists) {
            throw new RuntimeException("El email ya existe en el sistema");
        }
    }

    private void checkPasswords(final String password,
                                final String confirmPassword) {
        if (password == null || !password.equals(confirmPassword)) {
            throw new RuntimeException("Las ocntraseñas no coinciden");
        }
    }
}
