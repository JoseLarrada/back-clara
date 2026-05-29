package com.proyecto.version1.Features.Calendario.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.util.UUID;

public record CalendarioHibridoRequest(
        @NotNull(message = "El empleado es obligatorio")
        UUID empleadoId,

        @NotNull(message = "La fecha es obligatoria")
        LocalDate fecha,

        @NotBlank(message = "El caracter del dia es obligatorio")
        String caracterDia
) {
}

