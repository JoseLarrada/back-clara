package com.proyecto.version1.Features.Reportes_Prenomina.mapper;

import com.proyecto.version1.Features.Reportes_Prenomina.ReportesPrenominaMensual;
import com.proyecto.version1.Features.Reportes_Prenomina.dto.ReporteConsolidadoResponse;
import com.proyecto.version1.Features.Reportes_Prenomina.dto.ReportesPrenominaMensualResponse;
import org.springframework.stereotype.Component;

@Component
public class ReportesPrenominaMensualMapper {

    public ReportesPrenominaMensualResponse toResponse(ReportesPrenominaMensual entity) {
        return new ReportesPrenominaMensualResponse(
                entity.getId(),
                entity.getEmpresa().getId(),
                entity.getEmpleado().getId(),
                entity.getEmpleado().getNombreCompleto(),
                entity.getMesPeriodo(),
                entity.getAnioPeriodo(),
                entity.getDiasTrabajadosEfectivos(),
                entity.getDiasFaltaInjustificada(),
                entity.getHorasExtrasDiurnasTotales(),
                entity.getHorasExtrasNocturnasTotales(),
                entity.getMontoSalarioBaseProporcional(),
                entity.getMontoGananciaExtras(),
                entity.getMontoDeduccionesFaltas(),
                entity.getMontoNetoPagar(),
                entity.getEstadoReporte(),
                entity.getRequiereRecalculo(),
                entity.getGeneradoEl()
        );
    }

    public ReporteConsolidadoResponse toConsolidado(
            ReportesPrenominaMensual entity,
            String tipoContrato,
            String tipoMoneda,
            int diasVacaciones,
            int llegadasTardias,
            java.math.BigDecimal horasTrabajadasTotales,
            java.time.LocalDate fechaInicio,
            java.time.LocalDate fechaFin) {
        return new ReporteConsolidadoResponse(
                entity.getEmpleado().getId(),
                entity.getEmpleado().getNombreCompleto(),
                tipoContrato,
                tipoMoneda,
                entity.getMesPeriodo(),
                entity.getAnioPeriodo(),
                fechaInicio,
                fechaFin,
                entity.getDiasTrabajadosEfectivos(),
                entity.getDiasFaltaInjustificada(),
                diasVacaciones,
                llegadasTardias,
                horasTrabajadasTotales,
                entity.getHorasExtrasDiurnasTotales(),
                entity.getHorasExtrasNocturnasTotales(),
                entity.getMontoSalarioBaseProporcional(),
                entity.getMontoGananciaExtras(),
                entity.getMontoDeduccionesFaltas(),
                entity.getMontoNetoPagar()
        );
    }
}


