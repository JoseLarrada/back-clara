package com.proyecto.version1.Features.Reportes_Prenomina;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ReportesPrenominaMensualRepository extends JpaRepository<ReportesPrenominaMensual, UUID> {

    List<ReportesPrenominaMensual> findByEmpresa_IdAndAnioPeriodoAndMesPeriodo(UUID empresaId, Integer anioPeriodo, Integer mesPeriodo);

    Page<ReportesPrenominaMensual> findByEmpresa_IdAndEstadoReporte(UUID empresaId, String estadoReporte, Pageable pageable);

    Page<ReportesPrenominaMensual> findByEmpresa_IdAndAnioPeriodoBetweenAndMesPeriodoBetween(UUID empresaId, Integer anioInicio, Integer anioFin, Integer mesInicio, Integer mesFin, Pageable pageable);

    Optional<ReportesPrenominaMensual> findByIdAndEmpresa_Id(UUID id, UUID empresaId);

    @Query("""
            select r
            from ReportesPrenominaMensual r
            where r.empresa.id = :empresaId
              and (
                (r.anioPeriodo > :anioInicio or (r.anioPeriodo = :anioInicio and r.mesPeriodo >= :mesInicio))
                and
                (r.anioPeriodo < :anioFin or (r.anioPeriodo = :anioFin and r.mesPeriodo <= :mesFin))
              )
            order by r.anioPeriodo desc, r.mesPeriodo desc, r.empleado.nombreCompleto asc
            """)
    List<ReportesPrenominaMensual> findConsolidadoPorRango(UUID empresaId, Integer anioInicio, Integer mesInicio, Integer anioFin, Integer mesFin);
}

