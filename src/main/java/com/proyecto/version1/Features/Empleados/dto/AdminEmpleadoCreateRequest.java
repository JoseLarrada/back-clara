package com.proyecto.version1.Features.Empleados.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public record AdminEmpleadoCreateRequest(
        @NotBlank(message = "El nombre completo es obligatorio")
        @Size(max = 250)
        String nombreCompleto,

        @NotBlank(message = "El email es obligatorio")
        @Email(message = "El email no tiene un formato valido")
        @Size(max = 150)
        String email,

        @NotBlank(message = "La clave temporal es obligatoria")
        @Size(min = 8, max = 255)
        String password,

        @NotBlank(message = "El rol es obligatorio")
        String rol,

        @NotBlank(message = "La modalidad de perfil es obligatoria")
        String modalidadPerfil,

        @Size(max = 500)
        String fotoPatronUrl,

        @NotNull(message = "El saldo de vacaciones es obligatorio")
        @Positive(message = "El saldo de vacaciones debe ser mayor que 0")
        Integer saldoVacaciones,

        Boolean activo
) {
}

