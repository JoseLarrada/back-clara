package com.proyecto.version1.Features.ContratosEmpleados.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDate;

public record ContratoUpdateRequest(
        @NotNull(message = "El salario base mensual es requerido") @DecimalMin(value = "0.01", message = "El salario debe ser mayor a cero") BigDecimal salarioBaseMensual,
        @NotBlank(message = "El tipo de moneda es requerido") @Size(min = 3, max = 3, message = "El código de moneda debe tener exactamente 3 caracteres") String tipoMoneda,
        @NotBlank(message = "El tipo de contrato es requerido") String tipoContrato,
        @NotNull(message = "La fecha de ingreso es requerida") LocalDate fechaIngreso,
        LocalDate fechaRetiro,
        @NotNull(message = "El estado activo es requerido") Boolean activo
) {}
