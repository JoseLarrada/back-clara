package com.proyecto.version1.Features.Geolocalizacion;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.UUID;

public interface UltimaUbicacionRepository extends JpaRepository<UltimaUbicacion, UUID> {

    @EntityGraph(attributePaths = {"empleado"})
    List<UltimaUbicacion> findByEmpleado_EmpresaId(UUID empresaId);
}
