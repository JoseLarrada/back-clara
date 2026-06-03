package com.proyecto.version1.Features.Empleados.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.jspecify.annotations.Nullable;

import java.math.BigDecimal;

public record RegistrarAsistenciaRequest(
        @NotNull(message = "El tipo de marcación es requerido") TipoMarcacionAsistencia tipoMarcacion,
        @NotNull(message = "El origen de la marcación es requerido") OrigenMarcacionAsistencia origenMarcacion,
        @Size(max = 500, message = "El token QR no puede superar 500 caracteres")
        @Nullable String tokenQr,
        @Nullable Boolean esFacialVerificado,
        @Nullable BigDecimal precisionGpsAccuracy,
        @Nullable BigDecimal latitud,
        @Nullable BigDecimal longitud,
        @Nullable Boolean esMockLocation,
        @Nullable String fotoCapturaUrl,
        @Nullable Double scoreFacialCoincidencia
) {}

