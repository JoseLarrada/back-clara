package com.proyecto.version1.Features.Empleados;

import com.proyecto.version1.Features.Empleados.dto.AdminDashboardTiempoRealResponse;
import com.proyecto.version1.Features.Empleados.service.AdminEmpresaDashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/v1/admin/dashboard")
@PreAuthorize("hasAnyAuthority('ADMIN_RRHH','ROLE_ADMIN_RRHH')")
@RequiredArgsConstructor
public class AdminEmpresaDashboardController {

    private final AdminEmpresaDashboardService adminEmpresaDashboardService;

    @GetMapping("/tiempo-real")
    public ResponseEntity<AdminDashboardTiempoRealResponse> obtenerDashboardTiempoReal(
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate fecha
    ) {
        return ResponseEntity.ok(adminEmpresaDashboardService.obtenerDashboardTiempoReal(fecha));
    }
}

