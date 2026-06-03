package com.proyecto.version1.Features.Reportes_Prenomina.dto;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

public record ReportesPrenominaMensualResponse(
        UUID id,
        UUID empresaId,
        UUID empleadoId,
        String empleadoNombre,
        Integer mesPeriodo,
        Integer anioPeriodo,
        Integer diasTrabajadosEfectivos,
        Integer diasFaltaInjustificada,
        BigDecimal horasExtrasDiurnasTotales,
        BigDecimal horasExtrasNocturnasTotales,
        BigDecimal montoSalarioBaseProporcional,
        BigDecimal montoGananciaExtras,
        BigDecimal montoDeduccionesFaltas,
        BigDecimal montoNetoPagar,
        String estadoReporte,
        Boolean requiereRecalculo,
        OffsetDateTime generadoEl
) {}

