package com.proyecto.version1.Features.Geolocalizacion;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class MonitoreoConexionTask {

    private static final Logger log = LoggerFactory.getLogger(MonitoreoConexionTask.class);
    private final UbicacionService ubicacionService;

    @Scheduled(fixedRate = 60000)
    public void auditarConexionesEmpleados() {
        log.info("Iniciando auditoria programada de conexion de empleados...");
        try {
            ubicacionService.auditarConexiones();
        } catch (Exception e) {
            log.error("Error durante la auditoria de conexion de empleados", e);
        }
    }
}
