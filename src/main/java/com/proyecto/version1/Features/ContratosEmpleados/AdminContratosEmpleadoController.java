package com.proyecto.version1.Features.ContratosEmpleados;

import com.proyecto.version1.Features.ContratosEmpleados.dto.*;
import com.proyecto.version1.Features.ContratosEmpleados.service.ContratosEmpleadoService;
import com.proyecto.version1.Features.Empresas.dto.PageResponse;
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
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/admin/contratos")
@PreAuthorize("hasAnyAuthority('ADMIN_RRHH', 'ROLE_ADMIN_RRHH', 'SUPERADMIN')")
@RequiredArgsConstructor
@Tag(name = "Contratos", description = "Gestión administrativa de los contratos laborales de los empleados")
public class AdminContratosEmpleadoController {

    private final ContratosEmpleadoService contratosEmpleadoService;

    @PostMapping
    @Operation(summary = "Crear contrato", description = "Registra un nuevo contrato para un empleado. Valida que no exista otro contrato activo al mismo tiempo.")
    public ResponseEntity<ContratoResponse> crear(@Valid @RequestBody ContratoCreateRequest request) {
        ContratoResponse created = contratosEmpleadoService.crearContrato(request);
        return ResponseEntity.created(URI.create("/api/v1/admin/contratos/" + created.id())).body(created);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Actualizar contrato", description = "Actualización general de los datos de un contrato existente.")
    public ResponseEntity<ContratoResponse> actualizar(
            @PathVariable UUID id,
            @Valid @RequestBody ContratoUpdateRequest request
    ) {
        return ResponseEntity.ok(contratosEmpleadoService.actualizarContrato(id, request));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener contrato por ID", description = "Recupera los detalles de un contrato específico.")
    public ResponseEntity<ContratoResponse> obtener(@PathVariable UUID id) {
        return ResponseEntity.ok(contratosEmpleadoService.obtenerContrato(id));
    }

    @GetMapping("/empleado/{empleadoId}")
    @Operation(summary = "Listar contratos por empleado", description = "Recupera la lista histórica de todos los contratos (activos e inactivos) de un empleado.")
    public ResponseEntity<List<ContratoResponse>> listarPorEmpleado(@PathVariable UUID empleadoId) {
        return ResponseEntity.ok(contratosEmpleadoService.listarPorEmpleado(empleadoId));
    }

    @GetMapping
    @Operation(summary = "Listar contratos paginados", description = "Recupera una bandeja paginada de todos los contratos de la empresa.")
    public ResponseEntity<PageResponse<ContratoResponse>> listar(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "fechaIngreso,desc") String sort
    ) {
        String[] sortParts = sort.split(",");
        String sortField = sortParts[0];
        Sort.Direction direction = (sortParts.length > 1 && "asc".equalsIgnoreCase(sortParts[1]))
                ? Sort.Direction.ASC
                : Sort.Direction.DESC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortField));
        return ResponseEntity.ok(contratosEmpleadoService.listarContratos(pageable));
    }

    @PutMapping("/{id}/cerrar")
    @Operation(summary = "Cerrar contrato", description = "Finaliza la vigencia de un contrato activo, estableciendo la fecha de retiro e inhabilitándolo.")
    public ResponseEntity<ContratoResponse> cerrar(
            @PathVariable UUID id,
            @Valid @RequestBody ContratoCloseRequest request
    ) {
        return ResponseEntity.ok(contratosEmpleadoService.cerrarContrato(id, request));
    }

    @PutMapping("/{id}/extender")
    @Operation(summary = "Extender o prorrogar contrato", description = "Extiende la fecha de retiro de un contrato de término fijo activo, permitiendo ajustar su salario base.")
    public ResponseEntity<ContratoResponse> extender(
            @PathVariable UUID id,
            @Valid @RequestBody ContratoExtendRequest request
    ) {
        return ResponseEntity.ok(contratosEmpleadoService.extenderContrato(id, request));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Eliminar contrato", description = "Elimina físicamente un contrato del sistema.")
    public ResponseEntity<Void> eliminar(@PathVariable UUID id) {
        contratosEmpleadoService.eliminarContrato(id);
        return ResponseEntity.noContent().build();
    }
}
