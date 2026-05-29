package com.proyecto.version1.Features.Empresas.repository;

import com.proyecto.version1.Features.Empresas.Empresa;
import org.springframework.data.jpa.domain.Specification;

public class EmpresaSpecifications {

    /**
     * Filtra empresas por nombre (case-insensitive LIKE).
     */
    public static Specification<Empresa> conNombreLike(String nombre) {
        return (root, query, cb) -> {
            if (nombre == null || nombre.isBlank()) {
                return cb.conjunction();
            }
            return cb.like(cb.lower(root.get("nombre")), "%" + nombre.toLowerCase() + "%");
        };
    }

    /**
     * Filtra empresas por NIT/RUT exacto.
     */
    public static Specification<Empresa> conNitRut(String nitRut) {
        return (root, query, cb) -> {
            if (nitRut == null || nitRut.isBlank()) {
                return cb.conjunction();
            }
            return cb.equal(root.get("nitRut"), nitRut);
        };
    }

    /**
     * Filtra empresas por Rubro exacto.
     */
    public static Specification<Empresa> conRubro(String rubro) {
        return (root, query, cb) -> {
            if (rubro == null || rubro.isBlank()) {
                return cb.conjunction();
            }
            return cb.equal(root.get("rubro"), rubro.toUpperCase());
        };
    }

    /**
     * Filtra empresas por Estado de Licencia exacto.
     */
    public static Specification<Empresa> conEstadoLicencia(String estado) {
        return (root, query, cb) -> {
            if (estado == null || estado.isBlank()) {
                return cb.conjunction();
            }
            return cb.equal(root.get("estadoLicencia"), estado.toUpperCase());
        };
    }
}

