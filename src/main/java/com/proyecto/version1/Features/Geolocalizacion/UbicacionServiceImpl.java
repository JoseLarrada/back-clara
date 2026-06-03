package com.proyecto.version1.Features.Geolocalizacion;

import com.proyecto.version1.Features.Anomalias.AnomaliasGravesAuditoria;
import com.proyecto.version1.Features.Anomalias.AnomaliasGravesAuditoriaRepository;
import com.proyecto.version1.Features.Empleados.Empleado;
import com.proyecto.version1.Features.Empleados.repository.EmpleadosRepository;
import com.proyecto.version1.Features.GeocercasRemota.GeocercasRemota;
import com.proyecto.version1.Features.GeocercasRemota.repository.GeocercasRemotaRepository;
import com.proyecto.version1.Features.RegistrosAsistencias.RegistroAsistencia;
import com.proyecto.version1.Features.RegistrosAsistencias.repository.RegistroAsistenciaRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class UbicacionServiceImpl implements UbicacionService {

    private static final Logger log = LoggerFactory.getLogger(UbicacionServiceImpl.class);

    private final UltimaUbicacionRepository ultimaUbicacionRepository;
    private final HistorialUbicacionRepository historialUbicacionRepository;
    private final UbicacionWebSocketHandler webSocketHandler;
    private final EmpleadosRepository empleadosRepository;
    private final RegistroAsistenciaRepository registroAsistenciaRepository;
    private final GeocercasRemotaRepository geocercasRemotaRepository;
    private final AnomaliasGravesAuditoriaRepository anomaliasRepository;

    @Override
    public void registrarUbicaciones(Empleado empleado, UbicacionPingRequest request) {
        if (request.pings() == null || request.pings().isEmpty()) {
            return;
        }

        // 1. Guardar todos los puntos en el historial
        List<HistorialUbicacion> historial = request.pings().stream()
                .map(ping -> HistorialUbicacion.builder()
                        .empleado(empleado)
                        .empresaId(empleado.getEmpresaId())
                        .latitud(ping.latitud())
                        .longitud(ping.longitud())
                        .precisionGps(ping.precisionGps())
                        .velocidad(ping.velocidad())
                        .direccion(ping.direccion())
                        .registradoEn(ping.registradoEn())
                        .build())
                .collect(Collectors.toList());

        historialUbicacionRepository.saveAll(historial);

        // 2. Encontrar el ping más reciente para actualizar la última ubicación
        UbicacionPingRequest.PingItem masReciente = request.pings().stream()
                .max(Comparator.comparing(UbicacionPingRequest.PingItem::registradoEn))
                .orElseThrow();

        UltimaUbicacion ultima = ultimaUbicacionRepository.findById(empleado.getId())
                .orElseGet(() -> UltimaUbicacion.builder()
                        .empleado(empleado)
                        .empresaId(empleado.getEmpresaId())
                        .build());

        ultima.setLatitud(masReciente.latitud());
        ultima.setLongitud(masReciente.longitud());
        ultima.setPrecisionGps(masReciente.precisionGps());
        ultima.setVelocidad(masReciente.velocidad());
        ultima.setDireccion(masReciente.direccion());
        ultima.setEstadoConexion("ACTIVO");
        ultima.setUltimaActualizacion(OffsetDateTime.now());

        ultimaUbicacionRepository.save(ultima);

        // 3. Verificar geofencing para empleados REMOTO o HIBRIDO
        Boolean fueraDeGeocerca = evaluarGeofencing(empleado, masReciente);

        // 4. Retransmitir en tiempo real a los administradores vía WebSockets
        UbicacionResponse response = new UbicacionResponse(
                empleado.getId(),
                empleado.getNombreCompleto(),
                ultima.getLatitud(),
                ultima.getLongitud(),
                ultima.getPrecisionGps(),
                ultima.getVelocidad(),
                ultima.getDireccion(),
                ultima.getEstadoConexion(),
                masReciente.registradoEn(),
                fueraDeGeocerca
        );

        webSocketHandler.broadcastUbicacion(response);
    }

    /**
     * Evalúa si el empleado está fuera de su geocerca asignada.
     * Si está fuera, registra una anomalía FUERA_DE_GEOCERCA.
     * Solo aplica para empleados con modalidad REMOTO o HIBRIDO que tengan geocerca configurada.
     */
    private Boolean evaluarGeofencing(Empleado empleado, UbicacionPingRequest.PingItem ping) {
        String modalidad = empleado.getModalidadPerfil();
        if (!"REMOTO".equals(modalidad) && !"HIBRIDO".equals(modalidad)) {
            return null; // No aplica para presenciales
        }

        // Buscar la primera geocerca del empleado (la principal)
        List<GeocercasRemota> geocercas = geocercasRemotaRepository
                .findByEmpleado_IdAndEmpleado_EmpresaId(empleado.getId(), empleado.getEmpresaId());

        if (geocercas.isEmpty()) {
            return null; // Sin geocerca configurada
        }

        GeocercasRemota geocerca = geocercas.get(0);
        double distancia = GeoUtils.calcularDistanciaMetros(
                ping.latitud(), ping.longitud(),
                geocerca.getLatitud(), geocerca.getLongitud()
        );

        boolean fuera = distancia > geocerca.getRadioToleranciaMetros();

        if (fuera) {
            log.warn("ALERTA GEOFENCING: Empleado {} está a {:.0f}m de su geocerca (radio permitido: {}m)",
                    empleado.getNombreCompleto(), distancia, geocerca.getRadioToleranciaMetros());

            // Registrar anomalía
            AnomaliasGravesAuditoria anomalia = AnomaliasGravesAuditoria.builder()
                    .empleado(empleado)
                    .empresaId(empleado.getEmpresaId())
                    .tipoAnomalia("FUERA_DE_GEOCERCA")
                    .detallesTecnicos(String.format(
                            "{\"lat\":%.8f,\"lon\":%.8f,\"distancia_metros\":%.2f,\"radio_permitido\":%d,\"geocerca\":\"%s\"}",
                            ping.latitud().doubleValue(), ping.longitud().doubleValue(),
                            distancia, geocerca.getRadioToleranciaMetros(), geocerca.getDescripcion()
                    ))
                    .build();

            anomaliasRepository.save(anomalia);
        }

        return fuera;
    }

    @Override
    @Transactional(readOnly = true)
    public List<UbicacionResponse> obtenerUltimasUbicaciones(UUID empresaId) {
        return ultimaUbicacionRepository.findByEmpleadoEmpresaId(empresaId).stream()
                .map(u -> new UbicacionResponse(
                        u.getEmpleadoId(),
                        u.getEmpleado().getNombreCompleto(),
                        u.getLatitud(),
                        u.getLongitud(),
                        u.getPrecisionGps(),
                        u.getVelocidad(),
                        u.getDireccion(),
                        u.getEstadoConexion(),
                        u.getUltimaActualizacion(),
                        null // No se evalúa geofencing en este endpoint
                ))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<UbicacionResponse> obtenerRutaHistorial(UUID empleadoId, LocalDate fecha) {
        OffsetDateTime inicio = fecha.atStartOfDay().atZone(ZoneId.systemDefault()).toOffsetDateTime();
        OffsetDateTime fin = fecha.plusDays(1).atStartOfDay().atZone(ZoneId.systemDefault()).toOffsetDateTime().minusNanos(1);

        return historialUbicacionRepository.findByEmpleadoIdAndRegistradoEnBetween(empleadoId, inicio, fin).stream()
                .map(h -> new UbicacionResponse(
                        h.getEmpleado().getId(),
                        h.getEmpleado().getNombreCompleto(),
                        h.getLatitud(),
                        h.getLongitud(),
                        h.getPrecisionGps(),
                        h.getVelocidad(),
                        h.getDireccion(),
                        "ACTIVO",
                        h.getRegistradoEn(),
                        null
                ))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<MonitoreoEmpleadoResponse> obtenerMonitoreoEmpleados(UUID empresaId) {
        // 1. Obtener empleados activos
        List<Empleado> empleados = empleadosRepository.findByEmpresaIdAndActivoTrue(empresaId);

        // 2. Obtener marcas de asistencia de hoy
        LocalDate hoy = LocalDate.now();
        List<RegistroAsistencia> asistencias = registroAsistenciaRepository.findByEmpresa_IdAndFechaBetween(empresaId, hoy, hoy);
        Map<UUID, RegistroAsistencia> asistenciaMap = asistencias.stream()
                .collect(Collectors.toMap(r -> r.getEmpleado().getId(), r -> r, (r1, r2) -> r1));

        // 3. Obtener últimas ubicaciones registradas
        List<UltimaUbicacion> ubicaciones = ultimaUbicacionRepository.findByEmpleadoEmpresaId(empresaId);
        Map<UUID, UltimaUbicacion> ubicacionMap = ubicaciones.stream()
                .collect(Collectors.toMap(UltimaUbicacion::getEmpleadoId, u -> u, (u1, u2) -> u1));

        // 4. Obtener todas las geocercas de la empresa
        List<GeocercasRemota> geocercas = geocercasRemotaRepository.findByEmpleado_EmpresaId(empresaId);
        Map<UUID, GeocercasRemota> geocercaMap = geocercas.stream()
                .collect(Collectors.toMap(g -> g.getEmpleado().getId(), g -> g, (g1, g2) -> g1));

        // 5. Rango de hoy para contar anomalías
        OffsetDateTime inicioHoy = hoy.atStartOfDay().atZone(ZoneId.systemDefault()).toOffsetDateTime();
        OffsetDateTime finHoy = hoy.plusDays(1).atStartOfDay().atZone(ZoneId.systemDefault()).toOffsetDateTime().minusNanos(1);

        // 6. Mapear a DTO enriquecido
        return empleados.stream().map(emp -> {
            RegistroAsistencia asistencia = asistenciaMap.get(emp.getId());
            UltimaUbicacion ubi = ubicacionMap.get(emp.getId());
            GeocercasRemota geocerca = geocercaMap.get(emp.getId());

            // Estado de jornada
            String jornadaEstado = "NO_INICIADA";
            if (asistencia != null) {
                if (asistencia.getHoraSalida() != null) {
                    jornadaEstado = "FINALIZADA";
                } else if (asistencia.getHoraAlmuerzoInicio() != null && asistencia.getHoraAlmuerzoFin() == null) {
                    jornadaEstado = "ALMUERZO";
                } else {
                    jornadaEstado = "EN_JORNADA";
                }
            }

            // Cálculo de geofencing
            Boolean fueraDeGeocerca = null;
            Double distanciaMetros = null;

            if (geocerca != null && ubi != null && ubi.getLatitud() != null && ubi.getLongitud() != null) {
                double dist = GeoUtils.calcularDistanciaMetros(
                        ubi.getLatitud(), ubi.getLongitud(),
                        geocerca.getLatitud(), geocerca.getLongitud()
                );
                distanciaMetros = Math.round(dist * 100.0) / 100.0; // Redondear a 2 decimales
                fueraDeGeocerca = dist > geocerca.getRadioToleranciaMetros();
            }

            // Conteo de anomalías del día
            long countAnomalias = anomaliasRepository.countByEmpleadoIdAndCreadoEnBetween(emp.getId(), inicioHoy, finHoy);

            return new MonitoreoEmpleadoResponse(
                    emp.getId(),
                    emp.getNombreCompleto(),
                    emp.getEmail(),
                    emp.getModalidadPerfil(),
                    jornadaEstado,
                    ubi != null ? ubi.getLatitud() : null,
                    ubi != null ? ubi.getLongitud() : null,
                    ubi != null ? ubi.getEstadoConexion() : "DESCONECTADO",
                    ubi != null ? ubi.getUltimaActualizacion() : null,
                    ubi != null ? ubi.getPrecisionGps() : null,
                    ubi != null ? ubi.getVelocidad() : null,
                    geocerca != null ? geocerca.getLatitud() : null,
                    geocerca != null ? geocerca.getLongitud() : null,
                    geocerca != null ? geocerca.getRadioToleranciaMetros() : null,
                    geocerca != null ? geocerca.getDescripcion() : null,
                    fueraDeGeocerca,
                    distanciaMetros,
                    (int) countAnomalias
            );
        }).collect(Collectors.toList());
    }

    @Override
    public void auditarConexiones() {
        OffsetDateTime ahora = OffsetDateTime.now();
        List<UltimaUbicacion> activas = ultimaUbicacionRepository.findAll();

        for (UltimaUbicacion u : activas) {
            long segundosTranscurridos = java.time.Duration.between(u.getUltimaActualizacion(), ahora).toSeconds();
            String nuevoEstado = null;

            if (segundosTranscurridos > 300) { // 5 minutos sin pings
                if (!"DESCONECTADO".equals(u.getEstadoConexion())) {
                    nuevoEstado = "DESCONECTADO";
                }
            } else if (segundosTranscurridos > 120) { // 2 minutos sin pings
                if ("ACTIVO".equals(u.getEstadoConexion())) {
                    nuevoEstado = "INACTIVO";
                }
            }

            if (nuevoEstado != null) {
                u.setEstadoConexion(nuevoEstado);
                ultimaUbicacionRepository.save(u);

                // Notificar por WebSocket el cambio de estado
                UbicacionResponse response = new UbicacionResponse(
                        u.getEmpleadoId(),
                        u.getEmpleado().getNombreCompleto(),
                        u.getLatitud(),
                        u.getLongitud(),
                        u.getPrecisionGps(),
                        u.getVelocidad(),
                        u.getDireccion(),
                        u.getEstadoConexion(),
                        u.getUltimaActualizacion(),
                        null
                );
                webSocketHandler.broadcastUbicacion(response);
            }
        }
    }
}
