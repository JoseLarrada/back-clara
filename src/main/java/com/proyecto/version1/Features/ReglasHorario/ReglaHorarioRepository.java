package com.proyecto.version1.Features.ReglasHorario;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ReglaHorarioRepository extends JpaRepository<ReglaHorario, UUID> {
    List<ReglaHorario> findByEmpresaId(UUID empresaId);

    Page<ReglaHorario> findByEmpresaId(UUID empresaId, Pageable pageable);
}

