package com.proyecto.version1.Features.Reportes_Prenomina.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;

import java.time.LocalDate;
import java.util.UUID;

public record ReporteGeneracionRequest(
        @NotNull(message = "La fecha de inicio es requerida") @PastOrPresent LocalDate fechaInicio,
        @NotNull(message = "La fecha de fin es requerida") @PastOrPresent LocalDate fechaFin,
        UUID empleadoId
) {}

