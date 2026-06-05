package com.proyecto.version1.Features.Empleados;

import com.proyecto.version1.Features.Empleados.dto.AdminEmpleadoCreateRequest;
import com.proyecto.version1.Features.Empleados.dto.AdminEmpleadoFotoRequest;
import com.proyecto.version1.Features.Empleados.dto.AdminEmpleadoModalidadLoteRequest;
import com.proyecto.version1.Features.Empleados.dto.AdminEmpleadoModalidadLoteResponse;
import com.proyecto.version1.Features.Empleados.dto.AdminEmpleadoModalidadRequest;
import com.proyecto.version1.Features.Empleados.dto.AdminEmpleadoResponse;
import com.proyecto.version1.Features.Empleados.dto.AdminEmpleadoUpdateRequest;
import com.proyecto.version1.Features.Empleados.service.AdminEmpleadoService;
import com.proyecto.version1.Features.Empresas.dto.PageResponse;
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
@RequestMapping("/api/v1/admin/empleados")
@PreAuthorize("hasAnyAuthority('ADMIN_RRHH','ROLE_ADMIN_RRHH')")
@RequiredArgsConstructor
public class AdminEmpleadoController {

    private final AdminEmpleadoService adminEmpleadoService;

    @PostMapping
    public ResponseEntity<AdminEmpleadoResponse> crear(@Valid @RequestBody AdminEmpleadoCreateRequest request) {
        AdminEmpleadoResponse created = adminEmpleadoService.crearEmpleado(request);
        return ResponseEntity.created(URI.create("/api/v1/admin/empleados/" + created.id())).body(created);
    }

    @GetMapping
    public ResponseEntity<PageResponse<AdminEmpleadoResponse>> listar(
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
        return ResponseEntity.ok(adminEmpleadoService.listarEmpleados(pageable));
    }

    @GetMapping("/{empleadoId:[0-9a-fA-F\\-]{36}}")
    public ResponseEntity<AdminEmpleadoResponse> obtener(@PathVariable UUID empleadoId) {
        return ResponseEntity.ok(adminEmpleadoService.obtenerEmpleado(empleadoId));
    }

    @PutMapping("/{empleadoId:[0-9a-fA-F\\-]{36}}")
    public ResponseEntity<AdminEmpleadoResponse> actualizar(
            @PathVariable UUID empleadoId,
            @Valid @RequestBody AdminEmpleadoUpdateRequest request
    ) {
        return ResponseEntity.ok(adminEmpleadoService.actualizarEmpleado(empleadoId, request));
    }

    @PatchMapping("/{empleadoId:[0-9a-fA-F\\-]{36}}/foto-patron")
    public ResponseEntity<AdminEmpleadoResponse> actualizarFotoPatron(
            @PathVariable UUID empleadoId,
            @Valid @RequestBody AdminEmpleadoFotoRequest request
    ) {
        return ResponseEntity.ok(adminEmpleadoService.actualizarFotoPatron(empleadoId, request));
    }

    @PatchMapping("/{empleadoId:[0-9a-fA-F\\-]{36}}/modalidad")
    public ResponseEntity<AdminEmpleadoResponse> actualizarModalidad(
            @PathVariable UUID empleadoId,
            @Valid @RequestBody AdminEmpleadoModalidadRequest request
    ) {
        return ResponseEntity.ok(adminEmpleadoService.actualizarModalidad(empleadoId, request));
    }

    @PatchMapping("/modalidad/lote")
    public ResponseEntity<AdminEmpleadoModalidadLoteResponse> actualizarModalidadLote(
            @Valid @RequestBody AdminEmpleadoModalidadLoteRequest request
    ) {
        return ResponseEntity.ok(adminEmpleadoService.actualizarModalidadLote(request));
    }

    @DeleteMapping("/{empleadoId:[0-9a-fA-F\\-]{36}}")
    public ResponseEntity<Void> eliminar(@PathVariable UUID empleadoId) {
        adminEmpleadoService.eliminarEmpleado(empleadoId);
        return ResponseEntity.noContent().build();
    }
}

