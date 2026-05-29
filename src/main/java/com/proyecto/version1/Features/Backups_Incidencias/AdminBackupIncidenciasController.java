package com.proyecto.version1.Features.Backups_Incidencias;

import com.proyecto.version1.Features.Backups_Incidencias.dto.JustificacionCreateRequest;
import com.proyecto.version1.Features.Backups_Incidencias.dto.JustificacionResponse;
import com.proyecto.version1.Features.Backups_Incidencias.dto.JustificacionRevisionRequest;
import com.proyecto.version1.Features.Backups_Incidencias.service.BackupIncidenciasService;
import com.proyecto.version1.Features.Empresas.dto.PageResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.jspecify.annotations.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/admin/justificaciones")
@PreAuthorize("hasAnyAuthority('ADMIN_RRHH', 'ROLE_ADMIN_RRHH', 'SUPERADMIN')")
@RequiredArgsConstructor
@Tag(name = "Justificaciones", description = "Gestión de justificaciones y aprobación/rechazo en bandeja de incidencias")
public class AdminBackupIncidenciasController {

    private final BackupIncidenciasService backupIncidenciasService;

    @PostMapping
    @Operation(summary = "Crear justificación", description = "Crea una justificación para una incidencia de asistencia")
    public ResponseEntity<@NonNull JustificacionResponse> crear(@Valid @RequestBody JustificacionCreateRequest request) {
        JustificacionResponse created = backupIncidenciasService.crearJustificacion(request);
        return ResponseEntity.created(URI.create("/api/v1/admin/justificaciones/" + created.id())).body(created);
    }

    @GetMapping("/pendientes")
    @Operation(summary = "Listar justificaciones pendientes", description = "Devuelve la bandeja de justificaciones pendientes de aprobación")
    public ResponseEntity<PageResponse<@NonNull JustificacionResponse>> listarPendientes(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "creadoEn,desc") String sort
    ) {
        String[] sortParts = sort.split(",");
        String sortField = sortParts[0];
        Sort.Direction direction = (sortParts.length > 1 && "asc".equalsIgnoreCase(sortParts[1]))
                ? Sort.Direction.ASC
                : Sort.Direction.DESC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortField));
        return ResponseEntity.ok(backupIncidenciasService.listarPendientes(pageable));
    }

    @PutMapping("/{justificacionId}/aprobar")
    @Operation(summary = "Aprobar justificación", description = "Aprueba la justificación y marca el registro de asistencia como falta justificada")
    public ResponseEntity<@NonNull JustificacionResponse> aprobar(
            @PathVariable UUID justificacionId,
            @RequestBody(required = false) JustificacionRevisionRequest request
    ) {
        return ResponseEntity.ok(backupIncidenciasService.aprobarJustificacion(justificacionId, request));
    }

    @PutMapping("/{justificacionId}/rechazar")
    @Operation(summary = "Rechazar justificación", description = "Rechaza la justificación sin modificar el estado del registro de asistencia")
    public ResponseEntity<@NonNull JustificacionResponse> rechazar(
            @PathVariable UUID justificacionId,
            @RequestBody(required = false) JustificacionRevisionRequest request
    ) {
        return ResponseEntity.ok(backupIncidenciasService.rechazarJustificacion(justificacionId, request));
    }
}


