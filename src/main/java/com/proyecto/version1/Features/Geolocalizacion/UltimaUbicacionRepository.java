package com.proyecto.version1.Features.Geolocalizacion;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;
import java.util.UUID;

public interface UltimaUbicacionRepository extends JpaRepository<UltimaUbicacion, UUID> {

    @Query("SELECT u FROM UltimaUbicacion u JOIN FETCH u.empleado e WHERE e.empresaId = :empresaId")
    List<UltimaUbicacion> findByEmpleadoEmpresaId(@Param("empresaId") UUID empresaId);
}
