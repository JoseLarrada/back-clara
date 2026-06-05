package com.proyecto.version1.Features.Geolocalizacion;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

public interface HistorialUbicacionRepository extends JpaRepository<HistorialUbicacion, UUID> {

    @EntityGraph(attributePaths = {"empleado"})
    List<HistorialUbicacion> findByEmpleado_IdAndRegistradoEnBetweenOrderByRegistradoEnAsc(
            UUID empleadoId,
            OffsetDateTime inicio,
            OffsetDateTime fin
    );

    @EntityGraph(attributePaths = {"empleado"})
    List<HistorialUbicacion> findByEmpleado_IdOrderByRegistradoEnAsc(UUID empleadoId);
}
