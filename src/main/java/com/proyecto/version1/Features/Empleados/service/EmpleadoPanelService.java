package com.proyecto.version1.Features.Empleados.service;

import com.proyecto.version1.Features.Empleados.dto.EstadoPanelEmpleadoResponse;
import com.proyecto.version1.Features.Empleados.dto.HistorialAsistenciaMensualResponse;
import com.proyecto.version1.Features.Empleados.dto.RegistrarAsistenciaRequest;
import com.proyecto.version1.Features.Empleados.dto.RegistroAsistenciaResponse;

public interface EmpleadoPanelService {

    EstadoPanelEmpleadoResponse obtenerPanelEmpleado();

    RegistroAsistenciaResponse registrarAsistencia(RegistrarAsistenciaRequest request);

    HistorialAsistenciaMensualResponse obtenerHistorialMensual(int anio, int mes);
}

