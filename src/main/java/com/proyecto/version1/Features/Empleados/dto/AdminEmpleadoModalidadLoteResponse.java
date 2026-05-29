package com.proyecto.version1.Features.Empleados.dto;

import java.util.List;
import java.util.UUID;

public record AdminEmpleadoModalidadLoteResponse(
        String modalidadPerfilAplicada,
        int totalSolicitados,
        int totalActualizados,
        List<UUID> empleadosNoEncontrados
) {
}

