package com.proyecto.version1.Features.Empresas;

public enum EstadoLicencia {
    ACTIVO {
        @Override
        public boolean permiteAcceso() { return true; }
    },
    SUSPENDIDO {
        @Override
        public boolean permiteAcceso() { return false; }
    },
    VENCIDO {
        @Override
        public boolean permiteAcceso() { return false; }
    };

    public abstract boolean permiteAcceso();
}
