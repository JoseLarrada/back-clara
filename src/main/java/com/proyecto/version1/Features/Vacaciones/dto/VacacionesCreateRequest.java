package com.proyecto.version1.Features.Vacaciones.dto;

import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.util.UUID;

public record VacacionesCreateRequest(
        @NotNull(message = "El ID del empleado es requerido") UUID empleadoId,
        @NotNull(message = "La fecha de inicio es requerida") LocalDate fechaInicio,
        @NotNull(message = "La fecha de fin es requerida") LocalDate fechaFin
) {}

