package com.proyecto.version1.config;

import com.proyecto.version1.Features.Empleados.Empleado;
import org.springframework.data.domain.AuditorAware;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Optional;
import java.util.UUID;

public class ApplicationAuditorAware implements AuditorAware<UUID> {

    @Override
    public Optional<UUID> getCurrentAuditor() {
        final Authentication authentication = SecurityContextHolder.getContext()
                .getAuthentication();

        if (authentication == null || !authentication.isAuthenticated() || authentication instanceof AnonymousAuthenticationToken) {
            return Optional.empty();
        }

        final Object principal = authentication.getPrincipal();
        if (principal instanceof Empleado) {
            return Optional.ofNullable(((Empleado) principal).getId());
        }
        return Optional.empty();
    }
}
