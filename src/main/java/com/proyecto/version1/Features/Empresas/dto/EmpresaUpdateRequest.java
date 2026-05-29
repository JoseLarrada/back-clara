package com.proyecto.version1.Features.Empresas.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record EmpresaUpdateRequest(
        @NotBlank(message = "El nombre es obligatorio")
        @Size(max = 150)
        String nombre,

        @NotBlank(message = "El rubro es obligatorio")
        @Size(max = 50)
        String rubro,

        Integer limiteEmpleados
) {}

