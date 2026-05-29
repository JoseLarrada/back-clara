package com.proyecto.version1.Features.Backups_Incidencias.service;

import com.proyecto.version1.Features.Backups_Incidencias.dto.JustificacionCreateRequest;
import com.proyecto.version1.Features.Backups_Incidencias.dto.JustificacionResponse;
import com.proyecto.version1.Features.Backups_Incidencias.dto.JustificacionRevisionRequest;
import com.proyecto.version1.Features.Empresas.dto.PageResponse;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface BackupIncidenciasService {

    JustificacionResponse crearJustificacion(JustificacionCreateRequest request);

    JustificacionResponse crearJustificacionEmpleado(JustificacionCreateRequest request);

    PageResponse<JustificacionResponse> listarPendientes(Pageable pageable);

    JustificacionResponse aprobarJustificacion(UUID justificacionId, JustificacionRevisionRequest request);

    JustificacionResponse rechazarJustificacion(UUID justificacionId, JustificacionRevisionRequest request);
}

