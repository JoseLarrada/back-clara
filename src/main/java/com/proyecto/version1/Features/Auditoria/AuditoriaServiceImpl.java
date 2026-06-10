package com.proyecto.version1.Features.Auditoria;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.proyecto.version1.Features.Empleados.Empleado;
import com.proyecto.version1.security.TenantContext;
import jakarta.annotation.PostConstruct;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class AuditoriaServiceImpl implements AuditoriaService {

    private static final Logger log = LoggerFactory.getLogger(AuditoriaServiceImpl.class);

    private final LogsAuditoriaSistemaRepository logsRepository;
    private final ObjectMapper objectMapper;

    private ObjectMapper auditMapper;

    @PostConstruct
    void init() {
        // Copia del ObjectMapper principal con configuración tolerante a proxies Hibernate
        auditMapper = objectMapper.copy()
                .disable(SerializationFeature.FAIL_ON_EMPTY_BEANS);
    }

    private String safeSerialize(Object value) {
        if (value == null) return null;
        try {
            return auditMapper.writeValueAsString(value);
        } catch (Exception e) {
            // Fallback: usar toString() si la serialización falla
            log.warn("No se pudo serializar objeto para auditoría ({}), usando toString()", e.getMessage());
            return value.toString();
        }
    }

    @Override
    public void registrarLog(String accion, String tablaAfectada, UUID registroId, Object valorAnterior, Object valorNuevo) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication == null || !authentication.isAuthenticated()) {
                return; // No registrado si no hay usuario autenticado
            }

            Empleado principal = null;
            if (authentication.getPrincipal() instanceof Empleado emp) {
                principal = emp;
            }

            if (principal == null) {
                return;
            }

            UUID tenantId = TenantContext.getCurrentTenant();
            if (tenantId == null) {
                tenantId = principal.getEmpresaId();
            }

            String valAnteriorJson = safeSerialize(valorAnterior);
            String valNuevoJson = safeSerialize(valorNuevo);

            String ip = null;
            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attributes != null) {
                HttpServletRequest request = attributes.getRequest();
                ip = request.getHeader("X-Forwarded-For");
                if (ip == null || ip.isBlank() || "unknown".equalsIgnoreCase(ip)) {
                    ip = request.getRemoteAddr();
                }
            }

            LogsAuditoriaSistema logEntity = LogsAuditoriaSistema.builder()
                    .empresaId(tenantId)
                    .usuarioId(principal.getId())
                    .rolUsuario(principal.getRol())
                    .accion(accion)
                    .tablaAfectada(tablaAfectada)
                    .registroId(registroId)
                    .valorAnterior(valAnteriorJson)
                    .valorNuevo(valNuevoJson)
                    .direccionIp(ip)
                    .build();

            logsRepository.save(logEntity);
        } catch (Exception e) {
            log.error("Error al registrar log de auditoría", e);
        }
    }
}
