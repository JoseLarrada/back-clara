package com.proyecto.version1.Features.Backups_Incidencias.repository;

import com.proyecto.version1.Features.Backups_Incidencias.BackupIncidenciasJustificacione;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface BackupIncidenciasJustificacionesRepository extends JpaRepository<BackupIncidenciasJustificacione, UUID> {

    @Query("""
            select b
            from BackupIncidenciasJustificacione b
            where b.registroAsistencia.empresa.id = :empresaId
              and b.estadoSolicitud = :estadoSolicitud
            order by b.creadoEn desc
            """)
    Page<BackupIncidenciasJustificacione> findPendientesByEmpresaId(UUID empresaId, String estadoSolicitud, Pageable pageable);

    @Query("""
            select b
            from BackupIncidenciasJustificacione b
            where b.id = :id
              and b.registroAsistencia.empresa.id = :empresaId
            """)
    Optional<BackupIncidenciasJustificacione> findByIdAndEmpresaId(UUID id, UUID empresaId);
}



