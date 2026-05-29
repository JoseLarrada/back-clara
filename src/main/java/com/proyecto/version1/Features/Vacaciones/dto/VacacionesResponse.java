package com.proyecto.version1.Features.Vacaciones.dto;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.UUID;

public record VacacionesResponse(
        UUID id,
        UUID empleadoId,
        String empleadoNombre,
        LocalDate fechaInicio,
        LocalDate fechaFin,
        Integer diasSolicitados,
        String estadoSolicitud,
        Integer saldoVacacionesAntes,
        Integer saldoVacacionesDespues,
        OffsetDateTime creadoEn
) {}

