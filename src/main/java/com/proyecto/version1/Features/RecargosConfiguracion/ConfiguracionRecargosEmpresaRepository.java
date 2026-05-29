package com.proyecto.version1.Features.RecargosConfiguracion;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface ConfiguracionRecargosEmpresaRepository extends JpaRepository<ConfiguracionRecargosEmpresa, UUID> {
	Optional<ConfiguracionRecargosEmpresa> findByEmpresa_Id(UUID empresaId);
}

