package com.proyecto.version1.Features.Empleados.dto;

import java.time.OffsetDateTime;
import java.util.UUID;

public record AdminEmpleadoResponse(
        UUID id,
        UUID empresaId,
        String nombreCompleto,
        String email,
        String rol,
        String modalidadPerfil,
        String fotoPatronUrl,
        Integer saldoVacaciones,
        Boolean activo,
        OffsetDateTime creadoEn
) {
}

