package com.proyecto.version1.Features.Anomalias;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.OffsetDateTime;
import java.util.UUID;

@Repository
public interface AnomaliasGravesAuditoriaRepository extends JpaRepository<AnomaliasGravesAuditoria, UUID> {

    long countByEmpleadoIdAndCreadoEnBetween(UUID empleadoId, OffsetDateTime start, OffsetDateTime end);
}
