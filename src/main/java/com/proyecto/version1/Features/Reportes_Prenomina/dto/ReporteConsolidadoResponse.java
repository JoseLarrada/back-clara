package com.proyecto.version1.Features.Reportes_Prenomina.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record ReporteConsolidadoResponse(
        UUID empleadoId,
        String empleadoNombre,
        String tipoContrato,
        String tipoMoneda,
        Integer mesPeriodo,
        Integer anioPeriodo,
        LocalDate fechaInicio,
        LocalDate fechaFin,
        Integer diasTrabajadosEfectivos,
        Integer diasFaltaInjustificada,
        Integer diasVacacionesAprobadas,
        Integer llegadasTardias,
        BigDecimal horasTrabajadasTotales,
        BigDecimal horasExtrasDiurnasTotales,
        BigDecimal horasExtrasNocturnasTotales,
        BigDecimal montoSalarioBaseProporcional,
        BigDecimal montoGananciaExtras,
        BigDecimal montoDeduccionesFaltas,
        BigDecimal montoNetoPagar
) {}

