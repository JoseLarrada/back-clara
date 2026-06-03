package com.proyecto.version1.Features.Geolocalizacion;

import com.proyecto.version1.Features.Empleados.Empleado;
import com.proyecto.version1.Features.Empleados.repository.EmpleadosRepository;
import com.proyecto.version1.Features.Empresas.exception.ResourceNotFoundException;
import com.proyecto.version1.security.TenantContext;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@Tag(name = "Geolocalización y Telemetría", description = "Endpoints para el registro de rutas de empleados y visualización de mapas en tiempo real")
public class UbicacionController {

    private final UbicacionService ubicacionService;
    private final EmpleadosRepository empleadosRepository;

    @PostMapping("/api/v1/empleado/panel/ubicaciones")
    @PreAuthorize("hasAnyAuthority('EMPLEADO', 'ROLE_EMPLEADO')")
    @Operation(summary = "Registrar ubicación(es)", description = "Permite al empleado reportar sus coordenadas. Soporta envío en lote para sincronización offline.")
    public ResponseEntity<Void> registrarUbicaciones(@Valid @RequestBody UbicacionPingRequest request) {
        Empleado current = getAuthenticatedEmployee();
        ubicacionService.registrarUbicaciones(current, request);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/api/v1/admin/empleados/ultimas-ubicaciones")
    @PreAuthorize("hasAnyAuthority('ADMIN_RRHH', 'ROLE_ADMIN_RRHH')")
    @Operation(summary = "Obtener últimas ubicaciones", description = "Devuelve el estado y ubicación actual de todos los empleados de la empresa del administrador.")
    public ResponseEntity<List<UbicacionResponse>> obtenerUltimasUbicaciones() {
        UUID tenantId = TenantContext.getCurrentTenant();
        if (tenantId == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Tenant no identificado en el token JWT.");
        }
        return ResponseEntity.ok(ubicacionService.obtenerUltimasUbicaciones(tenantId));
    }

    @GetMapping("/api/v1/admin/empleados/monitoreo")
    @PreAuthorize("hasAnyAuthority('ADMIN_RRHH', 'ROLE_ADMIN_RRHH')")
    @Operation(summary = "Obtener monitoreo completo de empleados", description = "Devuelve el estado diario de jornada (no iniciado, activo, almuerzo, cerrado) y la última geolocalización de todos los empleados.")
    public ResponseEntity<List<MonitoreoEmpleadoResponse>> obtenerMonitoreoEmpleados() {
        UUID tenantId = TenantContext.getCurrentTenant();
        if (tenantId == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Tenant no identificado en el token JWT.");
        }
        return ResponseEntity.ok(ubicacionService.obtenerMonitoreoEmpleados(tenantId));
    }

    @GetMapping("/api/v1/admin/empleados/{empleadoId}/ruta")
    @PreAuthorize("hasAnyAuthority('ADMIN_RRHH', 'ROLE_ADMIN_RRHH')")
    @Operation(summary = "Obtener historial de ruta", description = "Devuelve la lista de coordenadas registradas para un empleado en un día específico.")
    public ResponseEntity<List<UbicacionResponse>> obtenerRutaHistorial(
            @PathVariable UUID empleadoId,
            @RequestParam(required = false) String fecha
    ) {
        LocalDate fechaConsulta = fecha != null ? LocalDate.parse(fecha) : LocalDate.now();
        return ResponseEntity.ok(ubicacionService.obtenerRutaHistorial(empleadoId, fechaConsulta));
    }

    private Empleado getAuthenticatedEmployee() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof Empleado principal)) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Usuario no autenticado.");
        }
        return empleadosRepository.findById(principal.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Empleado autenticado no encontrado."));
    }
}
