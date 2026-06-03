package com.proyecto.version1.Features.ContratosEmpleados.dto;

import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public record ContratoCloseRequest(
        @NotNull(message = "La fecha de terminación efectiva es requerida") LocalDate fechaRetiro
) {}
