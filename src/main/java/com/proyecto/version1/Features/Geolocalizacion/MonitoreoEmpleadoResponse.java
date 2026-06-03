package com.proyecto.version1.Features.Geolocalizacion;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

public record MonitoreoEmpleadoResponse(
    UUID empleadoId,
    String empleadoNombre,
    String email,
    String modalidad,
    String jornadaEstado,
    // Ubicación actual
    BigDecimal latitud,
    BigDecimal longitud,
    String estadoConexion,
    OffsetDateTime ultimaActualizacion,
    BigDecimal precisionGps,
    BigDecimal velocidad,
    // Geocerca asignada
    BigDecimal geocercaLatitud,
    BigDecimal geocercaLongitud,
    Integer geocercaRadioMetros,
    String geocercaDescripcion,
    // Métricas de cumplimiento geográfico
    Boolean fueraDeGeocerca,
    Double distanciaGeocercaMetros,
    // Anomalías del día
    Integer anomaliasHoy
) {}
