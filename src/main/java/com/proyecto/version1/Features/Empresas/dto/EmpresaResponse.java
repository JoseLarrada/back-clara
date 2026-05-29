package com.proyecto.version1.Features.Empresas.dto;

import java.time.OffsetDateTime;
import java.util.UUID;

public record EmpresaResponse(
        UUID id,
        String nombre,
        String nitRut,
        String rubro,
        Integer limiteEmpleados,
        String estadoLicencia,
        OffsetDateTime creadoEn,
        OffsetDateTime actualizadoEn
) {}

