package com.proyecto.version1.Features.Empresas.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record EmpresaEstadoUpdateRequest(
        @NotBlank(message = "El estado es obligatorio")
        @Pattern(regexp = "ACTIVO|SUSPENDIDO", message = "Estado permitido: ACTIVO o SUSPENDIDO")
        String estado
) {}

