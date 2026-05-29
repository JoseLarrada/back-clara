package com.proyecto.version1.Features.Empresas;

import com.proyecto.version1.Features.Empresas.dto.*;
import com.proyecto.version1.Features.Empresas.service.SuperAdminEmpresaService;
import com.proyecto.version1.Features.Empresas.EstadoLicencia;
import com.proyecto.version1.Features.Empresas.exception.BadRequestException;
import com.proyecto.version1.Features.RecargosConfiguracion.service.ConfiguracionRecargosService;
import com.proyecto.version1.Features.RecargosConfiguracion.ConfiguracionRecargosEmpresaRepository;
import com.proyecto.version1.Features.RecargosConfiguracion.ConfiguracionRecargosEmpresa;
import com.proyecto.version1.Features.RecargosConfiguracion.dto.ConfiguracionRecargosCreateRequest;
import com.proyecto.version1.Features.RecargosConfiguracion.dto.ConfiguracionRecargosResponse;
import com.proyecto.version1.Features.RecargosConfiguracion.dto.ConfiguracionRecargosUpdateRequest;
import com.proyecto.version1.Features.ReglasHorario.service.ReglaHorarioService;
import com.proyecto.version1.Features.ReglasHorario.dto.ReglaHorarioCreateRequest;
import com.proyecto.version1.Features.ReglasHorario.dto.ReglaHorarioResponse;
import com.proyecto.version1.Features.ReglasHorario.dto.ReglaHorarioUpdateRequest;
import com.proyecto.version1.security.TenantContext;
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
@RequestMapping("/api/v1/superadmin/empresas")
@PreAuthorize("hasAuthority('SUPERADMIN')")
@RequiredArgsConstructor
public class EmpresaController {

    private final SuperAdminEmpresaService empresaService;
    private final ConfiguracionRecargosService configuracionRecargosService;
    private final ReglaHorarioService reglaHorarioService;
    private final ConfiguracionRecargosEmpresaRepository configuracionRecargosRepository;

    @PostMapping
    public ResponseEntity<?> crear(@Valid @RequestBody EmpresaCreateRequest request) {
        return empresaService.crearEmpresa(request);
    }

    @GetMapping
    public ResponseEntity<?> listar(
            @RequestParam(required = false) String nombre,
            @RequestParam(required = false) String nitRut,
            @RequestParam(required = false) String rubro,
            @RequestParam(required = false) String estadoLicencia,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "creadoEn,desc") String sort) {

        // Parsear el parámetro sort (formato: "campo,asc|desc")
        String[] sortParts = sort.split(",");
        String sortField = sortParts[0];
        Sort.Direction direction = sortParts.length > 1 && sortParts[1].equalsIgnoreCase("asc")
                ? Sort.Direction.ASC
                : Sort.Direction.DESC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortField));

        return empresaService.listarEmpresasConFiltros(nombre, nitRut, rubro, estadoLicencia, pageable);
    }

    @GetMapping("/dashboard")
    @PreAuthorize("hasAuthority('SUPERADMIN')")
    public ResponseEntity<?> dashboard() {
        return empresaService.obtenerMetricasGlobales();
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> obtener(@PathVariable UUID id) {
        return empresaService.obtenerPorId(id);
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> actualizar(@PathVariable UUID id, @Valid @RequestBody EmpresaUpdateRequest request) {
        return empresaService.actualizarEmpresa(id, request);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> eliminar(@PathVariable UUID id) {
        return empresaService.eliminarEmpresa(id);
    }

    @PatchMapping("/{id}/estado")
    public ResponseEntity<?> actualizarEstado(@PathVariable UUID id, @Valid @RequestBody EmpresaEstadoUpdateRequest request) {
        EstadoLicencia estado = EstadoLicencia.valueOf(request.estado());
        return empresaService.actualizarEstadoLicencia(id, estado);
    }

    // --- ENDPOINTS PARA CONFIGURACIÓN DE RECARGOS (SUPERADMIN DELEGADO) ---

    @PostMapping("/{empresaId}/recargos")
    public ResponseEntity<ConfiguracionRecargosResponse> crearRecargos(
            @PathVariable UUID empresaId,
            @Valid @RequestBody ConfiguracionRecargosCreateRequest request) {
        try {
            TenantContext.setCurrentTenant(empresaId);
            if (configuracionRecargosRepository.findByEmpresa_Id(empresaId).isPresent()) {
                throw new BadRequestException("Ya existe una configuración de recargos para la empresa especificada.");
            }
            ConfiguracionRecargosResponse created = configuracionRecargosService.crear(request);
            return ResponseEntity.created(URI.create("/api/v1/superadmin/empresas/" + empresaId + "/recargos")).body(created);
        } finally {
            TenantContext.clear();
        }
    }

    @GetMapping("/{empresaId}/recargos")
    public ResponseEntity<ConfiguracionRecargosResponse> obtenerRecargos(@PathVariable UUID empresaId) {
        try {
            TenantContext.setCurrentTenant(empresaId);
            ConfiguracionRecargosResponse response = configuracionRecargosService.obtenerPorEmpresa(empresaId);
            return ResponseEntity.ok(response);
        } finally {
            TenantContext.clear();
        }
    }

    @PutMapping("/{empresaId}/recargos")
    public ResponseEntity<ConfiguracionRecargosResponse> actualizarRecargos(
            @PathVariable UUID empresaId,
            @Valid @RequestBody ConfiguracionRecargosUpdateRequest request) {
        try {
            TenantContext.setCurrentTenant(empresaId);
            ConfiguracionRecargosEmpresa existing = configuracionRecargosRepository.findByEmpresa_Id(empresaId)
                    .orElseThrow(() -> new BadRequestException("No existe ninguna configuración de recargos para la empresa especificada. Debe crearla primero."));
            ConfiguracionRecargosResponse updated = configuracionRecargosService.actualizar(existing.getId(), request);
            return ResponseEntity.ok(updated);
        } finally {
            TenantContext.clear();
        }
    }

    @DeleteMapping("/{empresaId}/recargos")
    public ResponseEntity<Void> eliminarRecargos(@PathVariable UUID empresaId) {
        try {
            TenantContext.setCurrentTenant(empresaId);
            ConfiguracionRecargosEmpresa existing = configuracionRecargosRepository.findByEmpresa_Id(empresaId)
                    .orElseThrow(() -> new BadRequestException("No existe ninguna configuración de recargos para la empresa especificada."));
            configuracionRecargosService.eliminar(existing.getId());
            return ResponseEntity.noContent().build();
        } finally {
            TenantContext.clear();
        }
    }

    // --- ENDPOINTS PARA REGLAS DE HORARIO (SUPERADMIN DELEGADO) ---

    @PostMapping("/{empresaId}/reglas-horario")
    public ResponseEntity<ReglaHorarioResponse> crearReglaHorario(
            @PathVariable UUID empresaId,
            @Valid @RequestBody ReglaHorarioCreateRequest request) {
        try {
            TenantContext.setCurrentTenant(empresaId);
            request.setEmpresaId(empresaId);
            ReglaHorarioResponse created = reglaHorarioService.crear(request);
            return ResponseEntity.created(URI.create("/api/v1/superadmin/empresas/" + empresaId + "/reglas-horario/" + created.getId()))
                    .body(created);
        } finally {
            TenantContext.clear();
        }
    }

    @GetMapping("/{empresaId}/reglas-horario")
    public ResponseEntity<List<ReglaHorarioResponse>> listarReglasHorario(@PathVariable UUID empresaId) {
        try {
            TenantContext.setCurrentTenant(empresaId);
            List<ReglaHorarioResponse> reglas = reglaHorarioService.listarPorEmpresa(empresaId);
            return ResponseEntity.ok(reglas);
        } finally {
            TenantContext.clear();
        }
    }

    @GetMapping("/{empresaId}/reglas-horario/paginated")
    public ResponseEntity<PageResponse<ReglaHorarioResponse>> listarReglasHorarioPaginado(
            @PathVariable UUID empresaId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "descripcion,asc") String sort) {
        try {
            TenantContext.setCurrentTenant(empresaId);
            String[] sortParts = sort.split(",");
            String sortField = sortParts[0];
            Sort.Direction direction = (sortParts.length > 1 && "desc".equalsIgnoreCase(sortParts[1]))
                    ? Sort.Direction.DESC
                    : Sort.Direction.ASC;

            Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortField));
            PageResponse<ReglaHorarioResponse> response = reglaHorarioService.listarPorEmpresaConPaginacion(empresaId, pageable);
            return ResponseEntity.ok(response);
        } finally {
            TenantContext.clear();
        }
    }

    @GetMapping("/{empresaId}/reglas-horario/{id}")
    public ResponseEntity<ReglaHorarioResponse> obtenerReglaHorario(
            @PathVariable UUID empresaId,
            @PathVariable UUID id) {
        try {
            TenantContext.setCurrentTenant(empresaId);
            ReglaHorarioResponse regla = reglaHorarioService.obtener(id);
            return ResponseEntity.ok(regla);
        } finally {
            TenantContext.clear();
        }
    }

    @PutMapping("/{empresaId}/reglas-horario/{id}")
    public ResponseEntity<ReglaHorarioResponse> actualizarReglaHorario(
            @PathVariable UUID empresaId,
            @PathVariable UUID id,
            @Valid @RequestBody ReglaHorarioUpdateRequest request) {
        try {
            TenantContext.setCurrentTenant(empresaId);
            ReglaHorarioResponse updated = reglaHorarioService.actualizar(id, request);
            return ResponseEntity.ok(updated);
        } finally {
            TenantContext.clear();
        }
    }

    @DeleteMapping("/{empresaId}/reglas-horario/{id}")
    public ResponseEntity<Void> eliminarReglaHorario(
            @PathVariable UUID empresaId,
            @PathVariable UUID id) {
        try {
            TenantContext.setCurrentTenant(empresaId);
            reglaHorarioService.eliminar(id);
            return ResponseEntity.noContent().build();
        } finally {
            TenantContext.clear();
        }
    }
}

