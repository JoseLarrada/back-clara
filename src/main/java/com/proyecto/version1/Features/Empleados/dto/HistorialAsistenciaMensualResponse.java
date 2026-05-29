package com.proyecto.version1.Features.Empleados.dto;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

public record HistorialAsistenciaMensualResponse(
        UUID empleadoId,
        String empleadoNombre,
        int anio,
        int mes,
        int totalDiasMes,
        int totalAsistencias,
        int totalRetardos,
        int totalFaltas,
        int totalFaltasInjustificadas,
        long totalHorasTrabajadasSegundos,
        long totalHorasNetasSegundos,
        List<DiaAsistenciaItem> calendario
) {
    public record DiaAsistenciaItem(
            LocalDate fecha,
            String estadoDia,
            String modalidadAplicada,
            String estadoEntrada,
            OffsetDateTime horaEntrada,
            OffsetDateTime horaAlmuerzoInicio,
            OffsetDateTime horaAlmuerzoFin,
            OffsetDateTime horaSalida,
            long horasTrabajadasSegundos,
            long horasAlmuerzoSegundos,
            long horasNetasSegundos
    ) {}
}

