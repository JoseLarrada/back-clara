package com.proyecto.version1.Features.Auditoria;

import java.util.UUID;

public interface AuditoriaService {
    void registrarLog(String accion, String tablaAfectada, UUID registroId, Object valorAnterior, Object valorNuevo);
}
