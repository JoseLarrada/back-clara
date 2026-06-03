package com.proyecto.version1.Features.Auditoria;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface LogsAuditoriaSistemaRepository extends JpaRepository<LogsAuditoriaSistema, UUID> {
}
