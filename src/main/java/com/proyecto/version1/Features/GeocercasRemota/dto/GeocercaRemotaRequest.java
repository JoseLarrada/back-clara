package com.proyecto.version1.Features.GeocercasRemota.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.util.UUID;

public record GeocercaRemotaRequest(
        @NotNull(message = "El empleado es obligatorio")
        UUID empleadoId,

        @NotBlank(message = "La descripcion es obligatoria")
        @Size(max = 100)
        String descripcion,

        @NotNull(message = "La latitud es obligatoria")
        @DecimalMin(value = "-90.00000000")
        @DecimalMax(value = "90.00000000")
        BigDecimal latitud,

        @NotNull(message = "La longitud es obligatoria")
        @DecimalMin(value = "-180.00000000")
        @DecimalMax(value = "180.00000000")
        BigDecimal longitud,

        @NotNull(message = "El radio de tolerancia es obligatorio")
        @Positive(message = "El radio de tolerancia debe ser mayor que 0")
        Integer radioToleranciaMetros
) {
}

