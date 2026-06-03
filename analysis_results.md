# Análisis de Arquitectura y Propuestas de Mejora: Módulo de RRHH y Base de Datos (CloudTime)

Este documento detalla el análisis del esquema de base de datos (`schema.sql`) y de los endpoints del módulo de administración de RRHH en el proyecto **CloudTime**. Se identifican los puntos de vulnerabilidad crítica, los riesgos de desfase (concurrencia y lógica de negocio) y se proponen soluciones de nivel empresarial con sus respectivos esquemas SQL para su implementación.

---

## 1. Mapeo de Endpoints vs. Tablas del Esquema

El frontend consume una serie de endpoints administrativos. Aquí se muestra su relación directa con las tablas de la base de datos:

| Operación / Endpoint | Tablas Afectadas | Tipo de Operación | Observación Crítica |
| :--- | :--- | :--- | :--- |
| `/api/v1/admin/empleados` | `empleados`, `contratos_empleados` | CRUD | Requiere aislamiento estricto por `empresa_id`. |
| `/api/v1/admin/geocercas` | `geocercas_remotas` | CRUD | Configura las coordenadas y radios por colaborador. |
| `/api/v1/admin/reglas-horario` | `reglas_negocio_horarios` | CRUD | Define la tolerancia de retardos y faltas. |
| `/api/v1/admin/recargos` | `configuracion_recargos_empresa`| Leer/Escribir | Configura multas y multiplicadores de horas extras. |
| `/api/v1/admin/justificaciones` | `backup_incidencias_justificaciones`, `registro_asistencia` | Leer/Aprobar/Rechazar | Modifica el estado de entrada de la asistencia. |
| `/api/v1/admin/vacaciones` | `solicitudes_vacaciones`, `empleados` | Leer/Aprobar/Rechazar | Afecta el `saldo_vacaciones` del empleado. |
| `/api/v1/admin/reportes-prenomina` | `reportes_prenomina_mensual` | Generar/Exportar | Consolida la contabilidad del mes por empleado. |
| `/api/v1/admin/empleados/monitoreo` | `ultima_ubicacion` | Leer | Muestra el estado del mapa en tiempo real. |
| `/api/v1/admin/empleados/{id}/ruta` | `historial_ubicaciones` | Leer | Retorna el trazado GPS para la fecha indicada. |

---

## 2. Diagnóstico: Puntos de Vulnerabilidad y Desfases

Tras analizar detenidamente el esquema de la base de datos y la funcionalidad de los servicios, se identifican las siguientes debilidades estructurales:

### A. Vulnerabilidad Multi-tenant (Aislamiento Lógico Frágil)
- **Problema:** El sistema utiliza una base de datos única y esquema compartido con discriminador (`empresa_id`). Sin embargo, tablas críticas como `solicitudes_vacaciones`, `contratos_empleados`, `geocercas_remotas`, `ultima_ubicacion` e `historial_ubicaciones` **no tienen la columna `empresa_id`**. Confían únicamente en el join con `empleados.id`.
- **Riesgo:** Si un desarrollador olvida hacer el JOIN adecuado con la tabla `empleados` al consultar estas tablas secundarias, o si hay un error en el binding de variables en el backend, un administrador de la Empresa A podría visualizar o modificar coordenadas de geocercas, contratos o el historial de ubicaciones de la Empresa B.

### B. Limitación del Registro de Asistencia (Riesgo de Desfase de Turnos)
- **Problema:** La tabla `registro_asistencia` tiene una restricción única rígida: `CONSTRAINT uk_asistencia_empleado_dia UNIQUE (empleado_id, fecha)`.
- **Riesgo:**
  1. **Turnos Partidos:** Si un empleado tiene permiso de salir a medio día para una cita médica y volver a ingresar en la tarde, el sistema no puede registrar su segundo ponche porque ya existe un registro único para ese día.
  2. **Turnos Nocturnos:** El uso de una columna de tipo `DATE` (`fecha DEFAULT CURRENT_DATE`) genera ambigüedad para jornadas que inician a las 10:00 PM del lunes y finalizan a las 6:00 AM del martes. ¿A qué día se le asigna la asistencia o las horas extras?

### C. Concurrencia y Consistencia de Saldos de Vacaciones (Riesgo de Saldo Negativo)
- **Problema:** El saldo se guarda como un entero mutable directamente en la tabla `empleados` (`saldo_vacaciones INT DEFAULT 15`).
- **Riesgo:** Si un empleado envía dos solicitudes de vacaciones paralelas rápidamente (o si dos administradores aprueban dos solicitudes distintas al mismo tiempo), se produce una **condición de carrera (race condition)**. El saldo podría descontarse doble sin validar los límites intermedios, resultando en saldos inconsistentes o negativos que no se reflejan en un libro contable auditable.

### D. Desfase en Pre-nómina por Modificaciones Retroactivas
- **Problema:** La tabla `reportes_prenomina_mensual` almacena el histórico consolidado del mes de forma inmutable. Sin embargo, no existe un mecanismo de alerta si un administrador aprueba una justificación retroactiva de asistencia de un mes ya cerrado.
- **Riesgo:** La pre-nómina aprobada y la asistencia real en la base de datos quedarán permanentemente desfasadas (desalineadas), lo que causa inconsistencias en las auditorías contables.

### E. Vacío Legal en Auditoría Biométrica (Evidencia de Suplantación)
- **Problema:** Existe una bandera booleana `es_facial_verificado` en `registro_asistencia`, pero no se guarda la foto tomada durante el ponche ni el score de coincidencia devuelto por el motor de reconocimiento.
- **Riesgo:** Si un empleado alega que un compañero lo suplantó usando una foto impresa en la tablet, la empresa no tiene cómo defender la veracidad del registro porque no guardó la foto capturada en el momento preciso de la marcación.

---

## 3. Propuestas de Mejora y Rediseño de Base de Datos

Para solucionar estos desfases y blindar el sistema, se recomiendan las siguientes mejoras:

### Propuesta 1: Activar Row Level Security (RLS) en PostgreSQL
Para evitar cualquier fuga de datos multi-tenant a nivel de base de datos (incluso ante errores de código en el backend), se debe activar RLS. Para ello, **todas** las tablas deben incluir la columna `empresa_id`.

```sql
-- 1. Agregar empresa_id a contratos_empleados
ALTER TABLE contratos_empleados ADD COLUMN empresa_id UUID REFERENCES empresas(id);
-- (Repetir para geocercas_remotas, solicitudes_vacaciones, ultima_ubicacion, etc.)

-- 2. Habilitar RLS en la tabla empleados
ALTER TABLE empleados ENABLE ROW LEVEL SECURITY;

-- 3. Crear política de seguridad basada en el Tenant actual del contexto de la sesión
CREATE POLICY tenant_isolation_policy ON empleados
    FOR ALL
    USING (empresa_id = NULLIF(current_setting('app.current_tenant_id', true), '')::UUID);
```

### Propuesta 2: Rediseñar Asistencia a Modelo "Ledger" (Libro de Marcas)
En lugar de una única fila diaria con columnas nullables para entrada y salida, se debe utilizar un libro de eventos de marcación. Esto resuelve turnos rotativos, nocturnos, partidos y horas extras de forma exacta.

```sql
CREATE TABLE registro_marcas (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    empresa_id UUID NOT NULL REFERENCES empresas(id),
    empleado_id UUID NOT NULL REFERENCES empleados(id),
    tipo_marca VARCHAR(20) NOT NULL, -- 'ENTRADA', 'INICIO_ALMUERZO', 'FIN_ALMUERZO', 'SALIDA'
    fecha_hora TIMESTAMP WITH TIME ZONE NOT NULL,
    modalidad VARCHAR(20) NOT NULL, -- 'PRESENCIAL', 'REMOTO'
    foto_captura_url VARCHAR(500), -- Almacena la foto del momento del ponche
    score_facial_coincidencia NUMERIC(5,2), -- Confianza de la biometría
    latitud NUMERIC(10, 8),
    longitud NUMERIC(11, 8),
    precision_gps_accuracy NUMERIC(10,2),
    es_mock_location BOOLEAN DEFAULT FALSE,
    creado_en TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_marcas_empleado_fecha ON registro_marcas(empleado_id, fecha_hora);
```

### Propuesta 3: Libro de Movimientos de Vacaciones (Anti-Concurrencia)
Evita guardar el saldo como una columna directa que se edita. En su lugar, el saldo debe ser la suma de los movimientos del empleado (días ganados vs. días tomados).

```sql
CREATE TABLE movimientos_vacaciones (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    empleado_id UUID NOT NULL REFERENCES empleados(id),
    tipo_movimiento VARCHAR(20) NOT NULL, -- 'DEVENGADO_LEY', 'TOMADO_APROBADO', 'AJUSTE_ADMIN'
    cantidad_dias INT NOT NULL, -- Valores positivos para acumular, negativos para restar
    solicitud_id UUID, -- Relación opcional con solicitudes_vacaciones
    motivo_ajuste TEXT,
    creado_por UUID, -- Administrador que ejecutó el ajuste
    creado_en TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- Vista optimizada para obtener el saldo real al instante
CREATE OR REPLACE VIEW vista_saldo_vacaciones AS
SELECT 
    empleado_id,
    COALESCE(SUM(cantidad_dias), 0) AS saldo_disponible
FROM movimientos_vacaciones
GROUP BY empleado_id;
```

### Propuesta 4: Trazabilidad y Control de Estado Contable de Pre-nómina
Para evitar el desfase entre las asistencias modificadas a posteriori y el reporte de pre-nómina ya generado, se debe crear un trigger en base de datos o flag de invalidación.

```sql
-- Agregar flag de validez a la pre-nómina
ALTER TABLE reportes_prenomina_mensual 
ADD COLUMN requiere_recalculo BOOLEAN DEFAULT FALSE;

-- Trigger: Si se aprueba una justificación o se cambia una marca del mes pasado,
-- marcar el reporte correspondiente como "requiere_recalculo = TRUE".
CREATE OR REPLACE FUNCTION fn_invalidar_prenomina_retroactiva()
RETURNS TRIGGER AS $$
BEGIN
    UPDATE reportes_prenomina_mensual
    SET requiere_recalculo = TRUE
    WHERE empleado_id = NEW.empleado_id
      AND mes_periodo = EXTRACT(MONTH FROM NEW.fecha)
      AND anio_periodo = EXTRACT(YEAR FROM NEW.fecha)
      AND estado_reporte = 'BORRADOR'; -- Solo si no ha sido pagado aún
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_asistencia_cambio_invalidar
AFTER UPDATE ON registro_asistencia
FOR EACH ROW
EXECUTE FUNCTION fn_invalidar_prenomina_retroactiva();
```

### Propuesta 5: Log de Auditoría Administrativa (Audit Trail)
Es indispensable para dar cumplimiento a auditorías de seguridad a nivel empresarial (saber exactamente quién hizo qué cambio).

```sql
CREATE TABLE logs_auditoria_sistema (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    empresa_id UUID NOT NULL REFERENCES empresas(id),
    usuario_id UUID NOT NULL, -- ID del Admin de RRHH o SuperAdmin
    rol_usuario VARCHAR(30) NOT NULL,
    accion VARCHAR(100) NOT NULL, -- 'CREAR_EMPLEADO', 'APROBAR_JUSTIFICACION', 'MODIFICAR_RECARGOS'
    tabla_afectada VARCHAR(50) NOT NULL,
    registro_id UUID,
    valor_anterior JSONB, -- Estado previo del registro en formato JSON
    valor_nuevo JSONB, -- Estado nuevo del registro en formato JSON
    direccion_ip VARCHAR(45),
    creado_en TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_logs_empresa_fecha ON logs_auditoria_sistema(empresa_id, creado_en);
```

---

## 4. Conclusiones y Hoja de Ruta Sugerida

Para lograr que el sistema sea seguro, no presente desfases y resulte escalable como un SaaS multi-tenant robusto, se sugiere abordar las mejoras en el siguiente orden:

1. **Corto Plazo (Seguridad inmediata):**
   - Implementar la tabla de `logs_auditoria_sistema` para registrar todas las acciones de los administradores de RRHH.
   - Añadir triggers de validación en la base de datos para evitar la aprobación de solicitudes de vacaciones que se traslapen en fechas.
2. **Mediano Plazo (Consistencia de datos):**
   - Transicionar de la columna mutable `saldo_vacaciones` a la tabla transaccional `movimientos_vacaciones` para eliminar condiciones de carrera y mantener un histórico de consumo de días.
   - Integrar el trigger de invalidación de pre-nómina (`requiere_recalculo`) para alertar al administrador de RRHH cuando las marcas históricas han sido modificadas.
3. **Largo Plazo (Rediseño estructural):**
   - Migrar la asistencia a la tabla transaccional `registro_marcas` (modelo Ledger) para dar soporte nativo y limpio a turnos partidos y jornadas nocturnas complejas sin restricciones de índice de fecha fija.
