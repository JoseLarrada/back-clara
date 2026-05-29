package com.proyecto.version1.Features.Vacaciones.service;

import com.proyecto.version1.Features.Empresas.dto.PageResponse;
import com.proyecto.version1.Features.Vacaciones.dto.VacacionesCreateRequest;
import com.proyecto.version1.Features.Vacaciones.dto.VacacionesEmpleadoCreateRequest;
import com.proyecto.version1.Features.Vacaciones.dto.VacacionesResponse;
import com.proyecto.version1.Features.Vacaciones.dto.VacacionesSaldoResponse;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface VacacionesService {

    VacacionesResponse crearSolicitud(VacacionesCreateRequest request);

    PageResponse<VacacionesResponse> listarPendientes(Pageable pageable);

    VacacionesResponse aprobarSolicitud(UUID solicitudId);

    VacacionesResponse rechazarSolicitud(UUID solicitudId);

    VacacionesSaldoResponse consultarSaldo(UUID empleadoId);

    VacacionesSaldoResponse consultarMiSaldo();

    VacacionesResponse crearMiSolicitud(VacacionesEmpleadoCreateRequest request);
}

