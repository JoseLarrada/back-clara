package com.proyecto.version1.Features.Empleados.service;

import com.proyecto.version1.Features.Empleados.dto.AdminEmpleadoCreateRequest;
import com.proyecto.version1.Features.Empleados.dto.AdminEmpleadoFotoRequest;
import com.proyecto.version1.Features.Empleados.dto.AdminEmpleadoModalidadLoteRequest;
import com.proyecto.version1.Features.Empleados.dto.AdminEmpleadoModalidadLoteResponse;
import com.proyecto.version1.Features.Empleados.dto.AdminEmpleadoModalidadRequest;
import com.proyecto.version1.Features.Empleados.dto.AdminEmpleadoResponse;
import com.proyecto.version1.Features.Empleados.dto.AdminEmpleadoUpdateRequest;
import com.proyecto.version1.Features.Empresas.dto.PageResponse;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface AdminEmpleadoService {
    AdminEmpleadoResponse crearEmpleado(AdminEmpleadoCreateRequest request);

    PageResponse<AdminEmpleadoResponse> listarEmpleados(Pageable pageable);

    AdminEmpleadoResponse obtenerEmpleado(UUID empleadoId);

    AdminEmpleadoResponse actualizarEmpleado(UUID empleadoId, AdminEmpleadoUpdateRequest request);

    AdminEmpleadoResponse actualizarFotoPatron(UUID empleadoId, AdminEmpleadoFotoRequest request);

    AdminEmpleadoResponse actualizarModalidad(UUID empleadoId, AdminEmpleadoModalidadRequest request);

    AdminEmpleadoModalidadLoteResponse actualizarModalidadLote(AdminEmpleadoModalidadLoteRequest request);

    void eliminarEmpleado(UUID empleadoId);
}

