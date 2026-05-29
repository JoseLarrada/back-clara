package com.proyecto.version1.Features.ReglasHorario;

import com.proyecto.version1.Features.ReglasHorario.dto.ReglaHorarioCreateRequest;
import com.proyecto.version1.Features.ReglasHorario.dto.ReglaHorarioResponse;
import com.proyecto.version1.Features.ReglasHorario.dto.ReglaHorarioUpdateRequest;
import com.proyecto.version1.Features.ReglasHorario.service.ReglaHorarioService;
import com.proyecto.version1.Features.Empresas.dto.PageResponse;
import com.proyecto.version1.security.TenantContext;
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
@RequestMapping("/api/v1/admin/reglas-horario")
@PreAuthorize("hasAnyAuthority('ADMIN_RRHH', 'ROLE_ADMIN_RRHH', 'SUPERADMIN')")
@RequiredArgsConstructor
@Tag(name = "Reglas de Horario", description = "Gestión de reglas de negocio para horarios, tolerancia de retardos y políticas de ausencia")
public class ReglaHorarioController {

    private final ReglaHorarioService reglaHorarioService;

    @PostMapping
    @Operation(summary = "Crear nueva regla de horario", description = "Permite crear una nueva regla de horario para la empresa autenticada")
    public ResponseEntity<ReglaHorarioResponse> crear(
            @Valid @RequestBody ReglaHorarioCreateRequest request) {
        ReglaHorarioResponse created = reglaHorarioService.crear(request);
        return ResponseEntity.created(URI.create("/api/v1/admin/reglas-horario/" + created.getId()))
                .body(created);
    }

    @GetMapping
    @Operation(summary = "Listar reglas de horario", description = "Obtiene todas las reglas de horario de la empresa autenticada")
    public ResponseEntity<List<ReglaHorarioResponse>> listar() {
        UUID empresaId = TenantContext.getCurrentTenant();
        List<ReglaHorarioResponse> reglas = reglaHorarioService.listarPorEmpresa(empresaId);
        return ResponseEntity.ok(reglas);
    }

    @GetMapping("/paginated")
    @Operation(summary = "Listar reglas de horario con paginación", description = "Obtiene las reglas de horario con soporte para paginación y ordenamiento")
    public ResponseEntity<PageResponse<ReglaHorarioResponse>> listarPaginado(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "descripcion,asc") String sort) {
        
        String[] sortParts = sort.split(",");
        String sortField = sortParts[0];
        Sort.Direction direction = (sortParts.length > 1 && "desc".equalsIgnoreCase(sortParts[1]))
                ? Sort.Direction.DESC
                : Sort.Direction.ASC;

        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortField));
        UUID empresaId = TenantContext.getCurrentTenant();
        
        PageResponse<ReglaHorarioResponse> response = reglaHorarioService
                .listarPorEmpresaConPaginacion(empresaId, pageable);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener regla de horario por ID", description = "Obtiene los detalles de una regla de horario específica")
    public ResponseEntity<ReglaHorarioResponse> obtener(@PathVariable UUID id) {
        ReglaHorarioResponse regla = reglaHorarioService.obtener(id);
        return ResponseEntity.ok(regla);
    }

    @GetMapping("/empresa/primaria")
    @Operation(summary = "Obtener regla primaria de la empresa", description = "Obtiene la regla de horario principal configurada para la empresa autenticada")
    public ResponseEntity<ReglaHorarioResponse> obtenerPorEmpresa() {
        UUID empresaId = TenantContext.getCurrentTenant();
        ReglaHorarioResponse regla = reglaHorarioService.obtenerPorEmpresa(empresaId);
        return ResponseEntity.ok(regla);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Actualizar regla de horario", description = "Permite actualizar los parámetros de una regla de horario existente")
    public ResponseEntity<ReglaHorarioResponse> actualizar(
            @PathVariable UUID id,
            @Valid @RequestBody ReglaHorarioUpdateRequest request) {
        ReglaHorarioResponse updated = reglaHorarioService.actualizar(id, request);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Eliminar regla de horario", description = "Elimina una regla de horario de la empresa autenticada")
    public ResponseEntity<Void> eliminar(@PathVariable UUID id) {
        reglaHorarioService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}

