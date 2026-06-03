package com.proyecto.version1.Features.Anomalias.service;

import com.proyecto.version1.Features.Empleados.Empleado;

public interface AlertaService {
    void registrarYDispararAlerta(Empleado empleado, String tipoAnomalia, String detallesTecnicos);
}
