package com.proyecto.version1.Features.Calendario.dto;

import java.time.LocalDate;
import java.util.UUID;

public record CalendarioHibridoResponse(
        UUID id,
        UUID empleadoId,
        LocalDate fecha,
        String caracterDia
) {
}

