package com.proyecto.version1.Features.ContratosEmpleados.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record ContratoResponse(
        UUID id,
        UUID empleadoId,
        String empleadoNombre,
        BigDecimal salarioBaseMensual,
        String tipoMoneda,
        String tipoContrato,
        LocalDate fechaIngreso,
        LocalDate fechaRetiro,
        Boolean activo
) {}
