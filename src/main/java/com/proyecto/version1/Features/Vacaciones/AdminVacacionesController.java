package com.proyecto.version1.Features.Vacaciones;

import com.proyecto.version1.Features.Empresas.dto.PageResponse;
import com.proyecto.version1.Features.Vacaciones.dto.VacacionesCreateRequest;
import com.proyecto.version1.Features.Vacaciones.dto.VacacionesResponse;
import com.proyecto.version1.Features.Vacaciones.dto.VacacionesSaldoResponse;
import com.proyecto.version1.Features.Vacaciones.service.VacacionesService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
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
@RequestMapping("/api/v1/admin/vacaciones")
@PreAuthorize("hasAnyAuthority('ADMIN_RRHH', 'ROLE_ADMIN_RRHH', 'SUPERADMIN')")
@RequiredArgsConstructor
@Tag(name = "Vacaciones", description = "Gestión de solicitudes de vacaciones y aprobación con descuento automático de saldo")
public class AdminVacacionesController {

    private final VacacionesService vacacionesService;

    @PostMapping
    @Operation(summary = "Crear solicitud de vacaciones", description = "Crea una nueva solicitud de vacaciones para un empleado de la empresa autenticada")
    public ResponseEntity<VacacionesResponse> crear(@Valid @RequestBody VacacionesCreateRequest request) {
        VacacionesResponse created = vacacionesService.crearSolicitud(request);
        return ResponseEntity.created(URI.create("/api/v1/admin/vacaciones/" + created.id())).body(created);
    }

    @GetMapping("/pendientes")
    @Operation(summary = "Listar solicitudes pendientes", description = "Devuelve la bandeja de solicitudes de vacaciones pendientes de aprobación")
    public ResponseEntity<PageResponse<VacacionesResponse>> listarPendientes(
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
        return ResponseEntity.ok(vacacionesService.listarPendientes(pageable));
    }

    @PutMapping("/{solicitudId}/aprobar")
    @Operation(summary = "Aprobar solicitud de vacaciones", description = "Aprueba la solicitud y descuenta automáticamente el saldo de vacaciones")
    public ResponseEntity<VacacionesResponse> aprobar(@PathVariable UUID solicitudId) {
        return ResponseEntity.ok(vacacionesService.aprobarSolicitud(solicitudId));
    }

    @PutMapping("/{solicitudId}/rechazar")
    @Operation(summary = "Rechazar solicitud de vacaciones", description = "Rechaza la solicitud de vacaciones sin modificar el saldo del empleado")
    public ResponseEntity<VacacionesResponse> rechazar(@PathVariable UUID solicitudId) {
        return ResponseEntity.ok(vacacionesService.rechazarSolicitud(solicitudId));
    }

    @GetMapping("/saldo/{empleadoId}")
    @Operation(summary = "Consultar saldo de vacaciones", description = "Obtiene el saldo de vacaciones disponible de un empleado")
    public ResponseEntity<VacacionesSaldoResponse> consultarSaldo(@PathVariable UUID empleadoId) {
        return ResponseEntity.ok(vacacionesService.consultarSaldo(empleadoId));
    }
}

