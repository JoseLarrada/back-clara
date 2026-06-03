# Resumen de Implementación: Fortalecimiento de Seguridad, Consistencia y Auditoría

Este documento resume las mejoras arquitectónicas, los cambios en la base de datos, el aislamiento multi-tenant y la tabla completa de endpoints (tanto de geolocalización en tiempo real como de administración de vacaciones y pre-nóminas) implementados en **CloudTime (`back-clara`)**.

---

## 1. Resumen de Endpoints del Sistema

A continuación se detallan los endpoints del sistema. Varios de ellos ahora cuentan con protección por **aislamiento automático multi-tenant** o realizan operaciones a través del **libro transaccional de movimientos** en lugar de mutar registros directos.

### Módulo de Geolocalización y Monitoreo (Nuevos Endpoints)
| Método | Endpoint | Roles Permitidos | Descripción / Comportamiento |
| :--- | :--- | :--- | :--- |
| **WS** | `/ws/ubicaciones` | Administrador | Canal WebSocket para recibir coordenadas y estados de conexión en vivo. |
| **POST** | `/api/v1/ubicaciones/ping` | Empleado | Recibe y persiste el ping GPS. Registra en `ultima_ubicacion` e `historial_ubicaciones`. |
| **GET** | `/api/v1/admin/empleados/monitoreo` | `ADMIN_RRHH`, `SUPERADMIN` | Obtiene el estado en tiempo real (Activo/Inactivo/Desconectado) de todo el personal. |
| **GET** | `/api/v1/admin/empleados/{id}/ruta` | `ADMIN_RRHH`, `SUPERADMIN` | Recupera el historial de posiciones de un colaborador para trazar su ruta en una fecha. |

### Módulo de Vacaciones (Comportamiento Seguro vía Ledger)
| Método | Endpoint | Roles Permitidos | Descripción / Comportamiento |
| :--- | :--- | :--- | :--- |
| **POST** | `/api/v1/admin/vacaciones` | `ADMIN_RRHH`, `SUPERADMIN` | Crea una solicitud de vacaciones. Valida fechas de forma estricta. |
| **GET** | `/api/v1/admin/vacaciones/pendientes` | `ADMIN_RRHH`, `SUPERADMIN` | Devuelve la bandeja de solicitudes pendientes de aprobación (filtrada por Tenant). |
| **PUT** | `/api/v1/admin/vacaciones/{solicitudId}/aprobar`| `ADMIN_RRHH`, `SUPERADMIN` | **[Actualizado]** Registra un movimiento negativo en `movimientos_vacaciones` y actualiza la caché del empleado. Evita condiciones de carrera. |
| **PUT** | `/api/v1/admin/vacaciones/{solicitudId}/rechazar`| `ADMIN_RRHH`, `SUPERADMIN` | Transiciona la solicitud al estado `RECHAZADO`. |
| **GET** | `/api/v1/admin/vacaciones/saldo/{empleadoId}` | `ADMIN_RRHH`, `SUPERADMIN` | **[Actualizado]** Consulta el saldo de vacaciones sumando dinámicamente los movimientos transaccionales en base de datos. |

### Módulo de Gestión de Contratos (Nuevos Endpoints)
| Método | Endpoint | Roles Permitidos | Descripción / Comportamiento |
| :--- | :--- | :--- | :--- |
| **POST** | `/api/v1/admin/contratos` | `ADMIN_RRHH`, `SUPERADMIN` | Crea un contrato para un empleado. Valida que no tenga otro activo. |
| **PUT** | `/api/v1/admin/contratos/{id}` | `ADMIN_RRHH`, `SUPERADMIN` | Actualización general de datos de un contrato existente. |
| **GET** | `/api/v1/admin/contratos/{id}` | `ADMIN_RRHH`, `SUPERADMIN` | Obtiene los detalles de un contrato específico por su ID. |
| **GET** | `/api/v1/admin/contratos/empleado/{empleadoId}`| `ADMIN_RRHH`, `SUPERADMIN` | Recupera la lista histórica de contratos (activos/inactivos) de un empleado. |
| **GET** | `/api/v1/admin/contratos` | `ADMIN_RRHH`, `SUPERADMIN` | Obtiene una bandeja paginada de todos los contratos de la empresa. |
| **PUT** | `/api/v1/admin/contratos/{id}/cerrar` | `ADMIN_RRHH`, `SUPERADMIN` | Termina un contrato activo indicando la fecha de retiro. |
| **PUT** | `/api/v1/admin/contratos/{id}/extender` | `ADMIN_RRHH`, `SUPERADMIN` | Extiende la fecha de retiro de un contrato y ajusta opcionalmente el salario. |
| **DELETE**| `/api/v1/admin/contratos/{id}` | `ADMIN_RRHH`, `SUPERADMIN` | Elimina físicamente un contrato del sistema. |

---

## 2. Resumen de Cambios Estructurales Realizados

### A. Aislamiento Multi-Tenant Robusto (Seguridad Fases 1)
*   **Hibernate `@TenantId`:** Se configuró el aislamiento nativo en Hibernate 6 para todas las entidades secundarias. Se añadieron las anotaciones a:
    *   `ContratosEmpleado`
    *   `GeocercasRemota`
    *   `SolicitudesVacacione`
    *   `UltimaUbicacion`
    *   `HistorialUbicacion`
    *   `AnomaliasGravesAuditoria`
    *   `MovimientoVacaciones`
    *   `RegistroMarcas`
    *   `LogsAuditoriaSistema`
*   **Corrección en el Resolver de Tenant (`TenantIdentifierResolver`):** Se modificó para retornar un UUID por defecto (`00000000-0000-0000-0000-000000000000`) en lugar de `null` al iniciar la aplicación. Esto previene que Hibernate 6 aborte el arranque de Spring Boot por validación de consultas en frío.
*   **Row-Level Security (RLS) en Postgres:** Se declararon las directivas `ALTER TABLE ... ENABLE ROW LEVEL SECURITY` y políticas de filtro de sesión (`app.current_tenant_id`) en `schema.sql`.

### B. Libro de Auditoría Administrativa (Audit Trail - Fase 2)
*   **Nueva Tabla `logs_auditoria_sistema`:** Registra el usuario ejecutor, acción, tabla afectada, ID del registro, valores anteriores y nuevos (en formato JSONB) e IP del administrador.
*   **Servicio de Auditoría:** Se integró `AuditoriaService` en el flujo de aprobación de vacaciones y está listo para ser inyectado en otros módulos (cambio de horarios, edición de recargos, etc.) para persistir el historial administrativo de manera transparente.

### C. Consistencia de Vacaciones (Ledger vs Saldo Mutable - Fase 3)
*   **Nueva Tabla `movimientos_vacaciones`:** Registra cada transacción (devengado por ley, días tomados, ajustes del administrador).
*   **Vista `vista_saldo_vacaciones`:** Suma dinámicamente el saldo real del empleado para evitar discrepancias por condiciones de carrera concurrentes.
*   **Trigger de Sincronización:** El trigger `trg_movimientos_vacaciones_cambio` se ejecuta tras cada inserción o edición en el libro de movimientos y actualiza de manera automatizada la caché de lectura rápida `saldo_vacaciones` en la tabla de `empleados`.

### D. Trazabilidad de Pre-nómina (Fase 4)
*   **Nueva columna `requiere_recalculo`:** Permite identificar si la pre-nómina consolidada del mes difiere de las marcas de asistencia reales.
*   **Trigger de Invalidación Reactiva:** El trigger `trg_asistencia_cambio_invalidar` marca automáticamente las pre-nóminas en estado `BORRADOR` con `requiere_recalculo = TRUE` si se detecta una justificación o cambio de asistencia retroactivo del mismo mes.

### E. Transición a Modelo de Marcas Ledger (Fase 5)
*   **Nueva Tabla `registro_marcas`:** Estructura un modelo de marcas individuales por evento (Entrada, Almuerzo, Salida) para dar soporte nativo a turnos partidos y jornadas nocturnas complejas en desarrollos futuros.

---

## 3. Nuevas Estructuras en Base de Datos (`schema.sql`)

### Tabla de Auditoría
```sql
CREATE TABLE logs_auditoria_sistema (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    empresa_id UUID NOT NULL REFERENCES empresas(id),
    usuario_id UUID NOT NULL,
    rol_usuario VARCHAR(30) NOT NULL,
    accion VARCHAR(100) NOT NULL,
    tabla_afectada VARCHAR(50) NOT NULL,
    registro_id UUID,
    valor_anterior JSONB,
    valor_nuevo JSONB,
    direccion_ip VARCHAR(45),
    creado_en TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);
```

### Libro de Vacaciones
```sql
CREATE TABLE movimientos_vacaciones (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    empresa_id UUID NOT NULL REFERENCES empresas(id) ON DELETE CASCADE,
    empleado_id UUID NOT NULL REFERENCES empleados(id) ON DELETE CASCADE,
    tipo_movimiento VARCHAR(20) NOT NULL, -- 'DEVENGADO_LEY', 'TOMADO_APROBADO', 'AJUSTE_ADMIN'
    cantidad_dias INT NOT NULL,
    solicitud_id UUID REFERENCES solicitudes_vacaciones(id) ON DELETE SET NULL,
    motivo_ajuste TEXT,
    creado_por UUID,
    creado_en TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT chk_movimientos_cantidad CHECK (cantidad_dias <> 0),
    CONSTRAINT chk_movimientos_tipo CHECK (tipo_movimiento IN ('DEVENGADO_LEY', 'TOMADO_APROBADO', 'AJUSTE_ADMIN'))
);
```

### Tabla de Eventos de Marcación
```sql
CREATE TABLE registro_marcas (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    empresa_id UUID NOT NULL REFERENCES empresas(id) ON DELETE CASCADE,
    empleado_id UUID NOT NULL REFERENCES empleados(id) ON DELETE CASCADE,
    tipo_marca VARCHAR(20) NOT NULL, -- 'ENTRADA', 'INICIO_ALMUERZO', 'FIN_ALMUERZO', 'SALIDA'
    fecha_hora TIMESTAMP WITH TIME ZONE NOT NULL,
    modalidad VARCHAR(20) NOT NULL, -- 'PRESENCIAL', 'REMOTO'
    foto_captura_url VARCHAR(500),
    score_facial_coincidencia NUMERIC(5,2),
    latitud NUMERIC(10, 8),
    longitud NUMERIC(11, 8),
    precision_gps_accuracy NUMERIC(10,2),
    es_mock_location BOOLEAN DEFAULT FALSE,
    creado_en TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT chk_marcas_tipo CHECK (tipo_marca IN ('ENTRADA', 'INICIO_ALMUERZO', 'FIN_ALMUERZO', 'SALIDA')),
    CONSTRAINT chk_marcas_modalidad CHECK (modalidad IN ('PRESENCIAL', 'REMOTO'))
);
```
