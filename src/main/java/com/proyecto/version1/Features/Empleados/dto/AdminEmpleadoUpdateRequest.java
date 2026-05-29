package com.proyecto.version1.Features.Empleados.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

public record AdminEmpleadoUpdateRequest(
        @NotBlank(message = "El nombre completo es obligatorio")
        @Size(max = 250)
        String nombreCompleto,

        @NotBlank(message = "El email es obligatorio")
        @Email(message = "El email no tiene un formato valido")
        @Size(max = 150)
        String email,

        @NotBlank(message = "El rol es obligatorio")
        String rol,

        @NotBlank(message = "La modalidad de perfil es obligatoria")
        String modalidadPerfil,

        @NotNull(message = "El saldo de vacaciones es obligatorio")
        @PositiveOrZero(message = "El saldo de vacaciones no puede ser negativo")
        Integer saldoVacaciones,

        @NotNull(message = "El estado activo es obligatorio")
        Boolean activo
) {
}

