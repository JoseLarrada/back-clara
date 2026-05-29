package com.proyecto.version1.Features.GeocercasRemota.dto;

import java.math.BigDecimal;
import java.util.UUID;

public record GeocercaRemotaResponse(
        UUID id,
        UUID empleadoId,
        String descripcion,
        BigDecimal latitud,
        BigDecimal longitud,
        Integer radioToleranciaMetros
) {
}

