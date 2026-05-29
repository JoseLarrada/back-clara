package com.proyecto.version1.Features.GeocercasRemota.repository;

import com.proyecto.version1.Features.GeocercasRemota.GeocercasRemota;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface GeocercasRemotaRepository extends JpaRepository<GeocercasRemota, UUID> {
    List<GeocercasRemota> findByEmpleado_IdAndEmpleado_EmpresaId(UUID empleadoId, UUID empresaId);
    List<GeocercasRemota> findByEmpleado_EmpresaId(UUID empresaId);
    Optional<GeocercasRemota> findByIdAndEmpleado_EmpresaId(UUID id, UUID empresaId);
}

