package com.proyecto.version1.Features.Empleados;

import com.proyecto.version1.auth.request.RegistrationRequest;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class EmpleadoMapper {
    private final PasswordEncoder passwordEncoder;

    public Empleado toEmpleado(final RegistrationRequest request) {
        return Empleado.builder()
                .nombreCompleto(request.nombreCompleto())
                .email(request.email())
                .passwordHash(this.passwordEncoder.encode(request.confirmPassword()))
                .rol(request.rol())
                .modalidadPerfil(request.modalidadPerfil())
                .fotoPatronUrl(request.fotourl())
                .saldoVacaciones(request.saldoVacaciones())
                .activo(true)
                .empresaId(UUID.fromString(request.empresa_id()))
                .build();
    }
}
