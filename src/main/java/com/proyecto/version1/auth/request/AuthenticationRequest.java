package com.proyecto.version1.auth.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record AuthenticationRequest(
        @NotBlank(message = "El correo no puede estar en blanco, ingrese un valor")
        @Email(message = "Formato Invalido debe colocar el formato correcto")
        @Schema(example = "jose@mail.com")
        String email,
        @NotBlank(message = "La contrasena no puede estar en blanco, ingrese un valor")
        @Schema(example = "pAssword1!_")
        String password
) {
}
