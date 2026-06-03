package com.proyecto.version1.Features.Geolocalizacion;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

public interface HistorialUbicacionRepository extends JpaRepository<HistorialUbicacion, UUID> {

    @Query("SELECT h FROM HistorialUbicacion h WHERE h.empleado.id = :empleadoId AND h.registradoEn BETWEEN :inicio AND :fin ORDER BY h.registradoEn ASC")
    List<HistorialUbicacion> findByEmpleadoIdAndRegistradoEnBetween(
            @Param("empleadoId") UUID empleadoId,
            @Param("inicio") OffsetDateTime inicio,
            @Param("fin") OffsetDateTime fin
    );
}
