package com.proyecto.version1.Features.Calendario.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;
import java.util.UUID;

public record CalendarioHibridoLoteRequest(
        @NotNull(message = "El empleado es obligatorio")
        UUID empleadoId,

        @NotEmpty(message = "La lista de asignaciones no puede estar vacia")
        List<CalendarioHibridoRequest> asignaciones
) {
}

