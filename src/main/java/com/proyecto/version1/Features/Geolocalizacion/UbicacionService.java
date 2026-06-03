package com.proyecto.version1.Features.Geolocalizacion;

import com.proyecto.version1.Features.Empleados.Empleado;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public interface UbicacionService {
    void registrarUbicaciones(Empleado empleado, UbicacionPingRequest request);
    List<UbicacionResponse> obtenerUltimasUbicaciones(UUID empresaId);
    List<UbicacionResponse> obtenerRutaHistorial(UUID empleadoId, LocalDate fecha);
    List<MonitoreoEmpleadoResponse> obtenerMonitoreoEmpleados(UUID empresaId);
    void auditarConexiones();
}
