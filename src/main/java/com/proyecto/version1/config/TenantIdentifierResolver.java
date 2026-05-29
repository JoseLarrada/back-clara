package com.proyecto.version1.config;

import com.proyecto.version1.security.TenantContext;
import org.hibernate.context.spi.CurrentTenantIdentifierResolver;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class TenantIdentifierResolver implements CurrentTenantIdentifierResolver<UUID> {

    @Override
    public UUID resolveCurrentTenantIdentifier() {
        // Retorna el UUID directo del hilo HTTP (Capturado por tu JwtFilter)
        UUID currentTenant = TenantContext.getCurrentTenant();

        // Si no hay sesión (ej: endpoint /auth/login), devolvemos null
        // para que Hibernate sepa que es una consulta global no filtrada
        return currentTenant;
    }

    @Override
    public boolean validateExistingCurrentSessions() {
        return true;
    }
}
