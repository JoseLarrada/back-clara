package com.proyecto.version1.Features.Empleados.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;
import java.util.UUID;

public record AdminEmpleadoModalidadLoteRequest(
        @NotEmpty(message = "La lista de empleados no puede estar vacia")
        List<UUID> empleadoIds,

        @NotBlank(message = "La modalidad de perfil es obligatoria")
        String modalidadPerfil
) {
}

