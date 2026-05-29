package com.proyecto.version1.Features.Empresas.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record EmpresaCreateRequest(
        @NotBlank(message = "El nombre es obligatorio")
        @Size(max = 150)
        String nombre,

        @NotBlank(message = "El NIT/RUT es obligatorio")
        @Size(max = 20)
        String nitRut,

        @NotBlank(message = "El rubro es obligatorio")
        @Size(max = 50)
        String rubro,

        Integer limiteEmpleados,

        String estadoLicencia
) {}

