package com.proyecto.version1.Features.ContratosEmpleados.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDate;

public record ContratoExtendRequest(
        @NotNull(message = "La nueva fecha de vencimiento es requerida") LocalDate nuevaFechaRetiro,
        @DecimalMin(value = "0.01", message = "El salario debe ser mayor a cero si se modifica") BigDecimal nuevoSalarioBase
) {}
