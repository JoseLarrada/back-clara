package com.proyecto.version1.Features.Vacaciones.dto;

import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public record VacacionesEmpleadoCreateRequest(
        @NotNull(message = "La fecha de inicio es requerida") LocalDate fechaInicio,
        @NotNull(message = "La fecha de fin es requerida") LocalDate fechaFin
) {}

