package com.proyecto.version1.Features.GeocercasRemota;

import com.proyecto.version1.Features.GeocercasRemota.dto.GeocercaRemotaRequest;
import com.proyecto.version1.Features.GeocercasRemota.dto.GeocercaRemotaResponse;
import com.proyecto.version1.Features.GeocercasRemota.service.GeocercasRemotaService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/admin/geocercas")
@PreAuthorize("hasAnyAuthority('ADMIN_RRHH','ROLE_ADMIN_RRHH')")
@RequiredArgsConstructor
public class AdminGeocercasRemotaController {

    private final GeocercasRemotaService geocercasRemotaService;

    @PostMapping
    public ResponseEntity<GeocercaRemotaResponse> crear(@Valid @RequestBody GeocercaRemotaRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(geocercasRemotaService.crearGeocerca(request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<GeocercaRemotaResponse> actualizar(
            @PathVariable UUID id,
            @Valid @RequestBody GeocercaRemotaRequest request
    ) {
        return ResponseEntity.ok(geocercasRemotaService.actualizarGeocerca(id, request));
    }

    @GetMapping
    public ResponseEntity<List<GeocercaRemotaResponse>> listar(@RequestParam(required = false) UUID empleadoId) {
        return ResponseEntity.ok(geocercasRemotaService.listarGeocercas(empleadoId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<GeocercaRemotaResponse> obtener(@PathVariable UUID id) {
        return ResponseEntity.ok(geocercasRemotaService.obtenerGeocerca(id));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable UUID id) {
        geocercasRemotaService.eliminarGeocerca(id);
        return ResponseEntity.noContent().build();
    }
}


