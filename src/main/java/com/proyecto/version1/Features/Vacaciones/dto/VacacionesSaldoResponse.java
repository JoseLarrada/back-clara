package com.proyecto.version1.Features.Vacaciones.dto;

import java.util.UUID;

public record VacacionesSaldoResponse(
        UUID empleadoId,
        String empleadoNombre,
        Integer saldoVacaciones
) {}

