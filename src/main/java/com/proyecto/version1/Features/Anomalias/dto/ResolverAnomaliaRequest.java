package com.proyecto.version1.Features.Anomalias.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ResolverAnomaliaRequest(
    @NotBlank(message = "El comentario es requerido")
    @Size(max = 2000, message = "El comentario no puede superar los 2000 caracteres")
    String comentario,
    
    @NotBlank(message = "El estado es requerido")
    String estado
) {}
