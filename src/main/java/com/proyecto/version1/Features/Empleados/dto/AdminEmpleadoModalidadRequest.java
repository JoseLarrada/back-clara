package com.proyecto.version1.Features.Empleados.dto;

import jakarta.validation.constraints.NotBlank;

public record AdminEmpleadoModalidadRequest(
        @NotBlank(message = "La modalidad de perfil es obligatoria")
        String modalidadPerfil
) {
}

