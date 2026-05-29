package com.proyecto.version1.Features.Backups_Incidencias.dto;

import jakarta.validation.constraints.Size;

public record JustificacionRevisionRequest(
        @Size(max = 2000, message = "Los comentarios no pueden superar 2000 caracteres") String comentariosAdministrador
) {}

