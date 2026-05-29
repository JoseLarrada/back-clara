package com.proyecto.version1.Features.Empleados.repository;

import com.proyecto.version1.Features.Empleados.Empleado;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;


import java.util.Optional;
import java.util.UUID;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
@Repository
public interface EmpleadosRepository extends JpaRepository<Empleado,UUID> {

    boolean existsByEmail(String email);

    Optional<Empleado> findByEmailIgnoreCase(String email);

    boolean existsByEmailIgnoreCase(String email);

    Optional<Empleado> findByEmail(String email);

    Optional<Empleado> findByIdAndEmpresaId(UUID id, UUID empresaId);

    Page<Empleado> findByEmpresaId(UUID empresaId, Pageable pageable);

    Page<Empleado> findByEmpresaIdAndRol(UUID empresaId, String rol, Pageable pageable);

    List<Empleado> findByEmpresaIdAndActivoTrue(UUID empresaId);

    List<Empleado> findByEmpresaIdAndIdIn(UUID empresaId, List<UUID> ids);

    long countByEmpresaIdAndActivoTrue(UUID empresaId);

    // Consulta rápida global de total de empleados (native para evitar posibles filtros tenant)
    @org.springframework.data.jpa.repository.Query(value = "SELECT COUNT(*) FROM empleados", nativeQuery = true)
    long countAllEmpleados();
}
