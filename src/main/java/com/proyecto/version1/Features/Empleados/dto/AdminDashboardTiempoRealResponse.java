package com.proyecto.version1.Features.Empleados.dto;

import java.time.LocalDate;

public record AdminDashboardTiempoRealResponse(
        LocalDate fecha,
        long totalActivos,
        long totalPresentes,
        long totalAusentes,
        long totalTeletrabajo,
        long totalPresencial,
        long totalEnAlmuerzo,
        long totalRetardos,
        long totalFaltas
) {
}

