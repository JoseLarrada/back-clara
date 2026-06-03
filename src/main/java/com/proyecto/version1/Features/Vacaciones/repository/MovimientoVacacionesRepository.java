package com.proyecto.version1.Features.Vacaciones.repository;

import com.proyecto.version1.Features.Vacaciones.MovimientoVacaciones;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface MovimientoVacacionesRepository extends JpaRepository<MovimientoVacaciones, UUID> {

    @Query("SELECT COALESCE(SUM(m.cantidadDias), 0) FROM MovimientoVacaciones m WHERE m.empleado.id = :empleadoId")
    int getSaldoVacaciones(@Param("empleadoId") UUID empleadoId);
}
