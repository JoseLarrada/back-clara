package com.proyecto.version1.Features.Calendario;

import com.proyecto.version1.Features.Calendario.dto.CalendarioHibridoLoteRequest;
import com.proyecto.version1.Features.Calendario.dto.CalendarioHibridoRequest;
import com.proyecto.version1.Features.Calendario.dto.CalendarioHibridoResponse;
import com.proyecto.version1.Features.Calendario.service.CalendarioHibridoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.format.annotation.DateTimeFormat;
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

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/admin/calendario-hibrido")
@PreAuthorize("hasAnyAuthority('ADMIN_RRHH','ROLE_ADMIN_RRHH')")
@RequiredArgsConstructor
public class AdminCalendarioHibridoController {

    private final CalendarioHibridoService calendarioHibridoService;

    @PostMapping
    public ResponseEntity<CalendarioHibridoResponse> crear(@Valid @RequestBody CalendarioHibridoRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(calendarioHibridoService.crearCalendario(request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<CalendarioHibridoResponse> actualizar(
            @PathVariable UUID id,
            @Valid @RequestBody CalendarioHibridoRequest request
    ) {
        return ResponseEntity.ok(calendarioHibridoService.actualizarCalendario(id, request));
    }

    @GetMapping
    public ResponseEntity<List<CalendarioHibridoResponse>> listar(
            @RequestParam(required = false) UUID empleadoId,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate desde,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate hasta
    ) {
        return ResponseEntity.ok(calendarioHibridoService.listarCalendarios(empleadoId, desde, hasta));
    }

    @PutMapping("/lote")
    public ResponseEntity<List<CalendarioHibridoResponse>> upsertLote(@Valid @RequestBody CalendarioHibridoLoteRequest request) {
        return ResponseEntity.ok(calendarioHibridoService.upsertCalendarioLote(request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable UUID id) {
        calendarioHibridoService.eliminarCalendario(id);
        return ResponseEntity.noContent().build();
    }
}


