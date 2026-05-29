package com.proyecto.version1.Features.Empresas.repository;

import com.proyecto.version1.Features.Empresas.Empresa;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface EmpresaRepository extends JpaRepository<Empresa, UUID>, JpaSpecificationExecutor<Empresa> {
    Optional<Empresa> findByNitRut(String nitRut);
    boolean existsByNitRut(String nitRut);
    // Conteo por estado de licencia a nivel global (native query para evitar filtros de tenant si necesario)
    @org.springframework.data.jpa.repository.Query(value = "SELECT COUNT(*) FROM empresas e WHERE e.estado_licencia = :estado", nativeQuery = true)
    long countByEstadoLicencia(String estado);
}

