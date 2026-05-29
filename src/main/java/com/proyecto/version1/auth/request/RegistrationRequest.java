package com.proyecto.version1.auth.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;

import java.time.LocalDate;

public record RegistrationRequest(
        @NotBlank(message = "El nombre no puede estar vacio, por favor ingrese un valor valido")
        @Size(
                min = 1,
                max = 50,
                message = "Nombre fuera del rango valido, por favor ingrese un nombre entre 1 y 50 caracteres"
        )
        @Pattern(
                regexp = "^[\\p{L} '-]+$",
                message = "No debemos tener numeros ni puntos en nuestros nombres"
        )
        @Schema(example = "Jose Larrada")
        String nombreCompleto,

        @NotBlank(message = "El correo no puede estar en blanco, ingrese un valor")
        @Email(message = "Formato Invalido debe colocar el formato correcto")
        @Schema(example = "jose@mail.com")
        String email,

        @NotBlank(message = "La contrasena no puede estar en blanco, ingrese un valor")
        @Size(min = 8,
                max = 72,
                message = "Contrasena fuera del rango valido, por favor ingrese una contrasena entre 8 y 72 caracteres"
        )
        @Pattern(regexp = "^(?=.*[A-Z])(?=.*[a-z])(?=.*\\d)(?=.*\\W).*$",
                message = "La contraseña debe cumplir con los estandares"
        )
        @Schema(example = "pAssword1!_")
        String passwordHash,

        @NotBlank(message = "La contrasena no puede estar en blanco, ingrese un valor")
        @Size(min = 8,
                max = 72,
                message = "Contrasena fuera del rango valido, por favor ingrese una contrasena entre 8 y 72 caracteres"
        )
        @Schema(example = "pAssword1!_")
        String confirmPassword,

        @NotBlank(message = "Debe haber un rol predefinido")
        @Schema(example = "Administrador")
        String rol,

        @NotBlank(message = "Debe haber una modalidad predefinido")
        @Schema(example = "Hibrido")
        String modalidadPerfil,

        @NotBlank(message = "Debe crearle una foto al empleado")
        @Schema(example = "foto")
        String fotourl,

        String empresa_id,

        @Schema(example = "0")
        Integer saldoVacaciones
) {
}
