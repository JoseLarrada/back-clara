# Documentación de la Feature: Módulo de Empleados (`back-clara`)

Este documento detalla la especificación de API, las reglas de negocio, los esquemas de transferencia de datos (DTOs) y los roles de seguridad asociados al **Módulo de Empleados** de CloudTime.

El módulo se compone de tres controladores principales:
1. **Administración de Empleados** (`AdminEmpleadoController`): Gestión general de colaboradores por parte de Recursos Humanos.
2. **Portal / Panel del Empleado** (`EmpleadoPanelController`): Autogestión del colaborador autenticado (marcación de asistencia, solicitudes, visualización de historial).
3. **Métricas en Tiempo Real** (`AdminEmpresaDashboardController`): KPI y conteo en caliente del personal para el dashboard de administración.

---

## 1. Resumen de Endpoints

### Endpoints Administrativos (`/api/v1/admin`)
*Protección:* Requiere roles `ADMIN_RRHH` o `SUPERADMIN`.
*Aislamiento:* Las operaciones están aisladas a nivel de base de datos por el `empresa_id` del administrador (multi-tenancy por discriminador).

| Método | Ruta | Descripción |
| :--- | :--- | :--- |
| **POST** | `/api/v1/admin/empleados` | Registrar un nuevo empleado. |
| **GET** | `/api/v1/admin/empleados` | Obtener listado de empleados (paginado). |
| **GET** | `/api/v1/admin/empleados/{empleadoId}` | Obtener los detalles de un empleado por ID. |
| **PUT** | `/api/v1/admin/empleados/{empleadoId}` | Actualizar datos generales de un empleado. |
| **PATCH** | `/api/v1/admin/empleados/{empleadoId}/foto-patron` | Actualizar URL de la foto de reconocimiento facial patrón. |
| **PATCH** | `/api/v1/admin/empleados/{empleadoId}/modalidad` | Cambiar modalidad de un empleado (`PRESENCIAL`, `REMOTO`, `HIBRIDO`). |
| **PATCH** | `/api/v1/admin/empleados/modalidad/lote` | Actualización masiva en lote de modalidades de empleados. |
| **DELETE** | `/api/v1/admin/empleados/{empleadoId}` | Desactivar / eliminar un empleado del sistema. |
| **GET** | `/api/v1/admin/dashboard/tiempo-real` | Obtener métricas en tiempo real del personal. |

### Endpoints del Portal del Empleado (`/api/v1/empleado/panel`)
*Protección:* Requiere rol `EMPLEADO`.
*Aislamiento:* Las consultas se resuelven exclusivamente para el usuario actualmente autenticado (a través del token JWT y el contexto de Spring Security).

| Método | Ruta | Descripción |
| :--- | :--- | :--- |
| **GET** | `/api/v1/empleado/panel` | Obtiene el estado dinámico actual del colaborador. |
| **POST** | `/api/v1/empleado/panel/asistencia` | Registrar entrada, inicio/fin almuerzo o salida (ponche). |
| **GET** | `/api/v1/empleado/panel/vacaciones/saldo` | Consultar saldo de vacaciones del empleado. |
| **POST** | `/api/v1/empleado/panel/vacaciones` | Solicitar un período de vacaciones. |
| **POST** | `/api/v1/empleado/panel/justificaciones` | Enviar una justificación (retardos/faltas) con soporte. |
| **GET** | `/api/v1/empleado/panel/historial-mensual` | Grid calendario mensual con asistencias y horas. |
| **GET** | `/api/v1/empleado/panel/ticker` | Ticker de estado y cronómetro del turno activo (Server-Sent Events). |

---

## 2. Detalle Técnico de Endpoints

### 2.1 Crear Empleado (Admin)
* **Método:** `POST`
* **Ruta:** `/api/v1/admin/empleados`
* **Cuerpo de la Petición (`AdminEmpleadoCreateRequest`):**
  ```json
  {
    "nombreCompleto": "Juan Pérez",
    "email": "juan.perez@empresa.com",
    "password": "claveTemporal123",
    "rol": "EMPLEADO", 
    "modalidadPerfil": "PRESENCIAL", 
    "fotoPatronUrl": "https://bucket-s3.amazonaws.com/fotos/perez.jpg", 
    "saldoVacaciones": 15,
    "activo": true
  }
  ```
  *Campos de Validación:*
  * `nombreCompleto`: No vacío, máximo 250 caracteres.
  * `email`: No vacío, formato válido, máximo 150 caracteres.
  * `password`: Mínimo 8 caracteres, máximo 255.
  * `rol`: Debe ser `EMPLEADO` o `ADMIN_RRHH`.
  * `modalidadPerfil`: Debe ser `PRESENCIAL`, `HIBRIDO` o `REMOTO`.
  * `saldoVacaciones`: Requerido, mayor a 0.
* **Respuesta (`AdminEmpleadoResponse`):** `201 Created`
  ```json
  {
    "id": "e4b2d35c-6b3a-4a61-9c8f-d12f3a8b4c9e",
    "empresaId": "8f6c3a1b-d49e-4a6f-bd1a-052bc723ff41",
    "nombreCompleto": "Juan Pérez",
    "email": "juan.perez@empresa.com",
    "rol": "EMPLEADO",
    "modalidadPerfil": "PRESENCIAL",
    "fotoPatronUrl": "https://bucket-s3.amazonaws.com/fotos/perez.jpg",
    "saldoVacaciones": 15,
    "activo": true,
    "creadoEn": "2026-06-01T10:00:00-05:00"
  }
  ```

### 2.2 Listar Empleados (Admin)
* **Método:** `GET`
* **Ruta:** `/api/v1/admin/empleados`
* **Parámetros Query:**
  * `page`: Número de página (default: `0`)
  * `size`: Registros por página (default: `10`)
  * `sort`: Campo de ordenamiento (default: `creadoEn,desc`)
* **Respuesta (`PageResponse<AdminEmpleadoResponse>`):** `200 OK`
  ```json
  {
    "content": [
      {
        "id": "e4b2d35c-6b3a-4a61-9c8f-d12f3a8b4c9e",
        "nombreCompleto": "Juan Pérez",
        "email": "juan.perez@empresa.com",
        "rol": "EMPLEADO",
        "modalidadPerfil": "PRESENCIAL",
        "fotoPatronUrl": "https://bucket-s3.amazonaws.com/fotos/perez.jpg",
        "saldoVacaciones": 15,
        "activo": true,
        "creadoEn": "2026-06-01T10:00:00-05:00"
      }
    ],
    "pageNumber": 0,
    "pageSize": 10,
    "totalElements": 1,
    "totalPages": 1
  }
  ```

### 2.3 Obtener/Actualizar Empleado (Admin)
* **Método:** `GET` / `PUT`
* **Ruta:** `/api/v1/admin/empleados/{empleadoId}`
* **Cuerpo de la Petición (`AdminEmpleadoUpdateRequest`):**
  ```json
  {
    "nombreCompleto": "Juan Pérez Actualizado",
    "email": "juan.perez.new@empresa.com",
    "rol": "EMPLEADO",
    "modalidadPerfil": "HIBRIDO",
    "activo": true
  }
  ```
* **Respuesta:** `200 OK` con el `AdminEmpleadoResponse` correspondiente.

### 2.4 Actualizar Foto Patrón (Admin)
* **Método:** `PATCH`
* **Ruta:** `/api/v1/admin/empleados/{empleadoId}/foto-patron`
* **Cuerpo de la Petición (`AdminEmpleadoFotoRequest`):**
  ```json
  {
    "fotoPatronUrl": "https://bucket-s3.amazonaws.com/fotos/patron-nuevo.jpg"
  }
  ```

### 2.5 Actualización de Modalidad Masiva (Admin)
* **Método:** `PATCH`
* **Ruta:** `/api/v1/admin/empleados/modalidad/lote`
* **Cuerpo de la Petición (`AdminEmpleadoModalidadLoteRequest`):**
  ```json
  {
    "empleadosIds": [
      "e4b2d35c-6b3a-4a61-9c8f-d12f3a8b4c9e",
      "a1b2c3d4-e5f6-7a8b-9c0d-1e2f3a4b5c6d"
    ],
    "nuevaModalidad": "REMOTO"
  }
  ```
* **Respuesta (`AdminEmpleadoModalidadLoteResponse`):** `200 OK`
  ```json
  {
    "procesadosCount": 2,
    "nuevaModalidad": "REMOTO"
  }
  ```

### 2.6 Registrar Asistencia (Empleado)
* **Método:** `POST`
* **Ruta:** `/api/v1/empleado/panel/asistencia`
* **Cuerpo de la Petición (`RegistrarAsistenciaRequest`):**
  ```json
  {
    "tipoMarcacion": "ENTRADA",
    "origenMarcacion": "REMOTO",
    "tokenQr": "hash-dinamico-qr-5seg",
    "esFacialVerificado": true,
    "precisionGpsAccuracy": 12.50
  }
  ```
  *Campos de Validación:*
  * `tipoMarcacion`: Enum (`ENTRADA`, `SALIDA`, `ALMUERZO`).
  * `origenMarcacion`: Enum (`PRESENCIAL_QR`, `REMOTO`).
  * `precisionGpsAccuracy`: Opcional, metadato de precisión GPS.
* **Respuesta (`RegistroAsistenciaResponse`):** `200 OK`
  ```json
  {
    "id": "a92c3d5e-6b8f-4c12-bd9c-d0123fa4bc5e",
    "empleadoId": "e4b2d35c-6b3a-4a61-9c8f-d12f3a8b4c9e",
    "empleadoNombre": "Juan Pérez",
    "fecha": "2026-06-01",
    "horaEntrada": "2026-06-01T08:02:15-05:00",
    "horaAlmuerzoInicio": null,
    "horaAlmuerzoFin": null,
    "horaSalida": null,
    "modalidadAplicada": "REMOTO",
    "estadoEntrada": "A_TIEMPO",
    "tipoRegistroPersistido": "ENTRADA",
    "instanteServidorUltimaMarcacion": "2026-06-01T08:02:16-05:00",
    "esFacialVerificado": true,
    "precisionGpsAccuracy": 12.50,
    "tokenQrUtilizado": "hash-dinamico-qr-5seg",
    "tipoMarcacionRegistrada": "ENTRADA",
    "origenMarcacion": "REMOTO",
    "mensaje": "Marcación de ENTRADA procesada de forma exitosa"
  }
  ```

### 2.7 Consultar Portal del Empleado (Empleado)
* **Método:** `GET`
* **Ruta:** `/api/v1/empleado/panel`
* **Respuesta (`EstadoPanelEmpleadoResponse`):** `200 OK`
  ```json
  {
    "empleadoId": "e4b2d35c-6b3a-4a61-9c8f-d12f3a8b4c9e",
    "nombreCompleto": "Juan Pérez",
    "modalidadPerfil": "REMOTO",
    "fotoPatronUrl": "https://bucket-s3.amazonaws.com/fotos/perez.jpg",
    "tieneJornadaIniciada": true,
    "tipoUltimoRegistro": "ENTRADA",
    "fechaHoy": "2026-06-01",
    "horaInicioJornada": "2026-06-01T08:02:15-05:00",
    "horasTrabajadasHoySegundos": 7200,
    "ultimoRegistroAsistencia": {
      "id": "a92c3d5e-6b8f-4c12-bd9c-d0123fa4bc5e",
      "fecha": "2026-06-01",
      "horaEntrada": "2026-06-01T08:02:15-05:00",
      "horaAlmuerzoInicio": null,
      "horaAlmuerzoFin": null,
      "horaSalida": null,
      "modalidadAplicada": "REMOTO",
      "estadoEntrada": "A_TIEMPO",
      "esFacialVerificado": true
    }
  }
  ```

### 2.8 Historial de Asistencia Mensual (Empleado)
* **Método:** `GET`
* **Ruta:** `/api/v1/empleado/panel/historial-mensual`
* **Parámetros Query:**
  * `anio`: Año a consultar (Opcional, por defecto el actual)
  * `mes`: Mes a consultar (Opcional, por defecto el actual, del 1 al 12)
* **Respuesta (`HistorialAsistenciaMensualResponse`):** `200 OK`
  ```json
  {
    "mes": 6,
    "anio": 2026,
    "asistenciasCount": 20,
    "retardosCount": 2,
    "faltasCount": 0,
    "horasTrabajadasAcumuladas": 160.5,
    "registros": [
      {
        "fecha": "2026-06-01",
        "horaEntrada": "2026-06-01T08:00:00-05:00",
        "horaSalida": "2026-06-01T17:00:00-05:00",
        "horasTrabajadasNetas": 8.0,
        "estado": "A_TIEMPO",
        "modalidad": "REMOTO"
      }
    ]
  }
  ```

### 2.9 Ticker de Estado Laboral SSE (Empleado)
* **Método:** `GET` (Protocolo Server-Sent Events / SSE)
* **Ruta:** `/api/v1/empleado/panel/ticker`
* **Parámetros Query:**
  * `intervaloMs`: Frecuencia de emisión de eventos en milisegundos (mínimo `500`, default: `1000`).
  * `duracionSegundos`: Tiempo total de vida del canal de streaming de SSE antes del completado (mínimo `30`, default: `300`).
* **Respuesta:** Flujo continuo `text/event-stream`. Cada mensaje contiene un payload idéntico a `EstadoPanelEmpleadoResponse`.

---

## 3. Modelo de Base de Datos: `empleados`

A nivel del motor de base de datos relacional (PostgreSQL 15), la entidad se define con las siguientes columnas y constraints:

```sql
CREATE TABLE empleados (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    empresa_id UUID NOT NULL, -- Discriminador para multi-tenant (SaaS)
    nombre_completo VARCHAR(250) NOT NULL,
    email VARCHAR(150) NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    rol VARCHAR(30) NOT NULL DEFAULT 'EMPLEADO', -- 'SUPERADMIN', 'ADMIN_RRHH', 'EMPLEADO'
    modalidad_perfil VARCHAR(20) NOT NULL, -- 'PRESENCIAL', 'HIBRIDO', 'REMOTO'
    foto_patron_url VARCHAR(500), -- Imagen patrón para verificación biométrica facial
    saldo_vacaciones INT NOT NULL DEFAULT 15, -- Bolsa de vacaciones (caché sincronizada)
    activo BOOLEAN NOT NULL DEFAULT TRUE,
    creado_en TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_empleados_empresa FOREIGN KEY (empresa_id) REFERENCES empresas(id) ON DELETE RESTRICT,
    CONSTRAINT uk_empleados_email UNIQUE (email),
    CONSTRAINT chk_empleados_rol CHECK (rol IN ('SUPERADMIN', 'ADMIN_RRHH', 'EMPLEADO')),
    CONSTRAINT chk_empleados_modalidad CHECK (modalidad_perfil IN ('PRESENCIAL', 'HIBRIDO', 'REMOTO')),
    CONSTRAINT chk_empleados_vacaciones CHECK (saldo_vacaciones >= 0)
);

-- Índices de consulta optimizada
CREATE INDEX idx_empleados_tenant ON empleados(empresa_id, id);
CREATE INDEX idx_empleados_email ON empleados(email);
```
