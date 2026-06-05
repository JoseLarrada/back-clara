package com.proyecto.version1.Features.GeocercasRemota.dto;

import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public record ReubicarGeocercaRequest(
    @NotNull(message = "La latitud es requerida") BigDecimal latitud,
    @NotNull(message = "La longitud es requerida") BigDecimal longitud
) {}
