package com.proyecto.version1.config;

import com.proyecto.version1.security.TenantContext;
import org.hibernate.context.spi.CurrentTenantIdentifierResolver;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class TenantIdentifierResolver implements CurrentTenantIdentifierResolver<UUID> {

    private static final UUID SYSTEM_DEFAULT_TENANT = UUID.fromString("00000000-0000-0000-0000-000000000000");

    @Override
    public UUID resolveCurrentTenantIdentifier() {
        // Retorna el UUID directo del hilo HTTP (Capturado por tu JwtFilter)
        UUID currentTenant = TenantContext.getCurrentTenant();

        // Si no hay sesión (ej: arranque del sistema), devolvemos un default tenant
        // inofensivo para evitar excepciones de inicialización de Hibernate
        if (currentTenant == null) {
            return SYSTEM_DEFAULT_TENANT;
        }
        return currentTenant;
    }

    @Override
    public boolean validateExistingCurrentSessions() {
        return true;
    }
}
