package com.proyecto.version1.Features.Empleados.service;

import com.proyecto.version1.Features.Empleados.dto.AdminDashboardTiempoRealResponse;

import java.time.LocalDate;

public interface AdminEmpresaDashboardService {
    AdminDashboardTiempoRealResponse obtenerDashboardTiempoReal(LocalDate fecha);
}

