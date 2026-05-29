package com.proyecto.version1.Features.Backups_Incidencias.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.UUID;

public record JustificacionCreateRequest(
        @NotNull(message = "El ID del registro de asistencia es requerido") UUID registroAsistenciaId,
        @NotBlank(message = "El motivo del empleado es requerido")
        @Size(min = 1, max = 4000, message = "El motivo debe tener entre 1 y 4000 caracteres") String motivoEmpleado,
        @NotBlank(message = "La URL del comprobante es requerida")
        @Size(max = 500, message = "La URL del comprobante no puede superar 500 caracteres") String urlComprobanteS3
) {}

