package com.proyecto.version1.Features.Backups_Incidencias.dto;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.UUID;

public record JustificacionResponse(
        UUID id,
        UUID registroAsistenciaId,
        UUID empleadoId,
        String empleadoNombre,
        LocalDate fecha,
        String motivoEmpleado,
        String urlComprobanteS3,
        String estadoSolicitud,
        String comentariosAdministrador,
        OffsetDateTime procesadoEn,
        OffsetDateTime creadoEn
) {}

