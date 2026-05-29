package com.proyecto.version1.Features.RecargosConfiguracion;
 
import com.proyecto.version1.Features.RecargosConfiguracion.dto.ConfiguracionRecargosCreateRequest;
import com.proyecto.version1.Features.RecargosConfiguracion.dto.ConfiguracionRecargosResponse;
import com.proyecto.version1.Features.RecargosConfiguracion.dto.ConfiguracionRecargosUpdateRequest;
import com.proyecto.version1.Features.RecargosConfiguracion.service.ConfiguracionRecargosService;
import com.proyecto.version1.Features.Empresas.exception.BadRequestException;
import com.proyecto.version1.security.TenantContext;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/admin/recargos")
@PreAuthorize("hasAnyAuthority('ADMIN_RRHH', 'ROLE_ADMIN_RRHH', 'SUPERADMIN')")
@RequiredArgsConstructor
@Tag(name = "Recargos de Empresa", description = "Gestión de los factores de recargos por horas extras, dominicales y multas por retardo")
public class AdminConfiguracionRecargosController {

    private final ConfiguracionRecargosService configuracionRecargosService;
    private final ConfiguracionRecargosEmpresaRepository repository;

    @PostMapping
    @Operation(summary = "Crear configuración de recargos", description = "Crea la parametrización de recargos para la empresa del administrador de RRHH actual")
    public ResponseEntity<ConfiguracionRecargosResponse> crear(@Valid @RequestBody ConfiguracionRecargosCreateRequest request) {
        UUID tenantId = TenantContext.getCurrentTenant();
        if (tenantId == null) {
            throw new BadRequestException("Tenant no identificado en token JWT.");
        }

        if (repository.findByEmpresa_Id(tenantId).isPresent()) {
            throw new BadRequestException("Ya existe una configuración de recargos para la empresa actual. Utilice el método PUT para actualizarla.");
        }

        ConfiguracionRecargosResponse created = configuracionRecargosService.crear(request);
        return ResponseEntity.created(URI.create("/api/v1/admin/recargos")).body(created);
    }

    @GetMapping
    @Operation(summary = "Obtener configuración de recargos", description = "Obtiene la parametrización de recargos configurada para la empresa actual")
    public ResponseEntity<ConfiguracionRecargosResponse> obtener() {
        UUID tenantId = TenantContext.getCurrentTenant();
        if (tenantId == null) {
            throw new BadRequestException("Tenant no identificado en token JWT.");
        }

        ConfiguracionRecargosResponse response = configuracionRecargosService.obtenerPorEmpresa(tenantId);
        return ResponseEntity.ok(response);
    }

    @PutMapping
    @Operation(summary = "Actualizar configuración de recargos", description = "Actualiza los factores de recargo para la empresa actual")
    public ResponseEntity<ConfiguracionRecargosResponse> actualizar(@Valid @RequestBody ConfiguracionRecargosUpdateRequest request) {
        UUID tenantId = TenantContext.getCurrentTenant();
        if (tenantId == null) {
            throw new BadRequestException("Tenant no identificado en token JWT.");
        }

        ConfiguracionRecargosEmpresa existing = repository.findByEmpresa_Id(tenantId)
                .orElseThrow(() -> new BadRequestException("No existe ninguna configuración de recargos para la empresa actual. Debe crearla primero."));

        ConfiguracionRecargosResponse updated = configuracionRecargosService.actualizar(existing.getId(), request);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping
    @Operation(summary = "Eliminar configuración de recargos", description = "Elimina la parametrización de recargos para la empresa actual")
    public ResponseEntity<Void> eliminar() {
        UUID tenantId = TenantContext.getCurrentTenant();
        if (tenantId == null) {
            throw new BadRequestException("Tenant no identificado en token JWT.");
        }

        ConfiguracionRecargosEmpresa existing = repository.findByEmpresa_Id(tenantId)
                .orElseThrow(() -> new BadRequestException("No existe ninguna configuración de recargos para la empresa actual."));

        configuracionRecargosService.eliminar(existing.getId());
        return ResponseEntity.noContent().build();
    }
}
