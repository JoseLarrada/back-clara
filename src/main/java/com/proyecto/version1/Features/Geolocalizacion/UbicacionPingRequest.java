package com.proyecto.version1.Features.Geolocalizacion;

import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;

public record UbicacionPingRequest(
    List<PingItem> pings
) {
    public record PingItem(
        @NotNull BigDecimal latitud,
        @NotNull BigDecimal longitud,
        BigDecimal precisionGps,
        BigDecimal velocidad,
        BigDecimal direccion,
        @NotNull OffsetDateTime registradoEn
    ) {}
}
