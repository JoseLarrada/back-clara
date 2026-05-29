package com.proyecto.version1.Features.Empleados.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record AdminEmpleadoFotoRequest(
        @NotBlank(message = "La URL de foto patron es obligatoria")
        @Size(max = 500)
        String fotoPatronUrl
) {
}

