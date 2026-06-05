package com.proyecto.version1.Features.Anomalias.service.impl;

import com.proyecto.version1.Features.Anomalias.AnomaliasGravesAuditoria;
import com.proyecto.version1.Features.Anomalias.AnomaliasGravesAuditoriaRepository;
import com.proyecto.version1.Features.Anomalias.service.AlertaService;
import com.proyecto.version1.Features.Empleados.Empleado;
import com.proyecto.version1.Features.Notifications.service.NotificationService;
import com.proyecto.version1.Features.Geolocalizacion.UbicacionWebSocketHandler;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class AlertaServiceImpl implements AlertaService {

    private static final Logger log = LoggerFactory.getLogger(AlertaServiceImpl.class);

    private final AnomaliasGravesAuditoriaRepository anomaliasRepository;
    private final UbicacionWebSocketHandler webSocketHandler;
    private final NotificationService notificationService;

    @Async
    @Override
    @Transactional
    public void registrarYDispararAlerta(Empleado empleado, String tipoAnomalia, String detallesTecnicos) {
        log.info("Procesando alerta asíncrona de anomalía {} para empleado {}", tipoAnomalia, empleado.getNombreCompleto());

        // 1. Guardar en Base de Datos
        AnomaliasGravesAuditoria anomalia = AnomaliasGravesAuditoria.builder()
                .empleado(empleado)
                .empresaId(empleado.getEmpresaId())
                .tipoAnomalia(tipoAnomalia)
                .detallesTecnicos(detallesTecnicos)
                .notificadoViaSns(false)
                .build();

        try {
            notificationService.sendBusinessEvent(
                    "ALERTA DE SEGURIDAD - Anomalía Grave",
                    String.format("Se ha detectado una anomalía tipo [%s] para el empleado %s. Detalles: %s",
                            tipoAnomalia, empleado.getNombreCompleto(), detallesTecnicos),
                    "ANOMALIA_SEGURIDAD",
                    empleado.getEmpresaId()
            );
            anomalia.setNotificadoViaSns(true);
        } catch (Exception e) {
            log.error("Error al publicar evento en AWS SNS", e);
        }

        AnomaliasGravesAuditoria saved = anomaliasRepository.save(anomalia);

        // 2. Retransmitir al panel administrativo vía WebSocket
        try {
            Map<String, Object> alertaPayload = new HashMap<>();
            alertaPayload.put("event", "ANOMALIA_GRAVE");
            alertaPayload.put("id", saved.getId());
            alertaPayload.put("empleadoId", empleado.getId());
            alertaPayload.put("empleadoNombre", empleado.getNombreCompleto());
            alertaPayload.put("tipoAnomalia", tipoAnomalia);
            alertaPayload.put("detallesTecnicos", detallesTecnicos);
            alertaPayload.put("creadoEn", OffsetDateTime.now());

            webSocketHandler.broadcastMessage(alertaPayload);
            log.info("Alerta de anomalía transmitida exitosamente vía WebSockets a administradores");
        } catch (Exception e) {
            log.error("Error al transmitir alerta vía WebSockets", e);
        }
    }

    private void simularSnsPublish(Empleado empleado, String tipoAnomalia, String detallesTecnicos) {
        log.info("[AWS SNS MOCK PUBLISH] Publicando alerta en AWS SNS Topic... " +
                        "EmpresaId: {}, Empleado: {}, Tipo: {}, Detalles: {}",
                empleado.getEmpresaId(), empleado.getNombreCompleto(), tipoAnomalia, detallesTecnicos);
    }
}
