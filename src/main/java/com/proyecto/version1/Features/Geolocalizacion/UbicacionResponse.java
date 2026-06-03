package com.proyecto.version1.Features.Geolocalizacion;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

public record UbicacionResponse(
    UUID empleadoId,
    String empleadoNombre,
    BigDecimal latitud,
    BigDecimal longitud,
    BigDecimal precisionGps,
    BigDecimal velocidad,
    BigDecimal direccion,
    String estadoConexion,
    OffsetDateTime registradoEn,
    Boolean fueraDeGeocerca
) {}
