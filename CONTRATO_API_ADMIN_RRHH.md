# CONTRATO DE INTEGRACIÓN REST: MÓDULO ADMINISTRADOR DE RRHH (ADMIN_RRHH)

Este documento define el contrato estricto de integración REST para el módulo de Administración de Recursos Humanos (RRHH). Está diseñado para servir como referencia técnica inmutable para el desarrollo del frontend (React / Agente de Frontend).

Base URL: `/api/v1/admin`

---

## 1. REGLAS GLOBALES Y SEGURIDAD

### 1.1. Autenticación y Autorización
- **Header Requerido:** Todas las peticiones deben incluir el header `Authorization: Bearer <JWT>`.
- **Rol Requerido:** El token JWT debe poseer la autoridad `ADMIN_RRHH` o `ROLE_ADMIN_RRHH` en su claim de roles.
  - Token inválido o ausente → `401 Unauthorized`
  - Token válido sin el rol requerido → `403 Forbidden`

### 1.2. Estructura del JWT
El token de acceso contiene los siguientes metadatos relevantes para el contexto:
- `sub`: Correo del usuario administrador.
- `tenant_id`: UUID de la empresa a la que pertenece el administrador.
- `roles`: `["ROLE_ADMIN_RRHH"]` o `["ADMIN_RRHH"]`.

### 1.3. Aislamiento Multi-tenant Implícito
El backend utiliza un filtro seguro ([JwtFilter](file:///d:/2026/cloud/proyecto-cloud/version1/src/main/java/com/proyecto/version1/security/JwtFilter.java)) que extrae de forma automática el `tenant_id` y lo vincula al hilo de ejecución actual a través del contexto de Spring Security.
> [!IMPORTANT]
> El frontend **NO** debe enviar el ID de la empresa (`empresaId` / `tenant_id`) en los cuerpos de las peticiones para los módulos administrativos del día a día, ya que el backend realiza la segregación de forma implícita y segura a nivel de base de datos. La única excepción son configuraciones iniciales que especifiquen lo contrario.

---

## 2. REFERENCIA DE ENDPOINTS: MÓDULO EMPLEADOS y DASHBOARD

Base path: `/api/v1/admin`

---

### POST /empleados — Crear Empleado
- **Ruta:** `POST /api/v1/admin/empleados`
- **Descripción:** Registra un nuevo empleado dentro de la empresa del administrador de RRHH actual.

**Request Body (JSON):**
```json
{
  "nombreCompleto": "Carlos Pérez González",
  "email": "carlos.perez@empresa.com",
  "password": "TemporalPassword123!",
  "rol": "EMPLEADO",
  "modalidadPerfil": "HIBRIDO",
  "fotoPatronUrl": "https://s3.amazonaws.com/cloudtime-buckets/comprobantes/carlos_patron.jpg",
  "saldoVacaciones": 15,
  "activo": true
}
```
*Restricciones:*
- `nombreCompleto`: Obligatorio, Máx 250 caracteres.
- `email`: Obligatorio, formato de correo válido, Máx 150 caracteres (Único en el sistema).
- `password`: Obligatorio, Mín 8, Máx 255 caracteres.
- `rol`: Obligatorio. Valores permitidos: `SUPERADMIN`, `ADMIN_RRHH`, `EMPLEADO`.
- `modalidadPerfil`: Obligatorio. Valores permitidos: `PRESENCIAL`, `HIBRIDO`, `REMOTO`.
- `fotoPatronUrl`: Opcional, Máx 500 caracteres.
- `saldoVacaciones`: Obligatorio, entero positivo (mayor que 0).
- `activo`: Opcional, por defecto `true`.

**Response (210 Created):**
```json
{
  "id": "7ac159a4-28b9-4672-911e-b8d438fc7bfd",
  "empresaId": "0757d941-2038-4abc-a0e3-fd1eaffd4bf3",
  "nombreCompleto": "Carlos Pérez González",
  "email": "carlos.perez@empresa.com",
  "rol": "EMPLEADO",
  "modalidadPerfil": "HIBRIDO",
  "fotoPatronUrl": "https://s3.amazonaws.com/cloudtime-buckets/comprobantes/carlos_patron.jpg",
  "saldoVacaciones": 15,
  "activo": true,
  "creadoEn": "2026-05-28T04:30:00.000Z"
}
```

---

### GET /empleados — Listar Empleados con Paginación
- **Ruta:** `GET /api/v1/admin/empleados`
- **Query Parameters:**
  - `page` (int, default: 0): Número de página (comienza en 0).
  - `size` (int, default: 10): Tamaño de página.
  - `sort` (String, default: "creadoEn,desc"): Formato `campo,dirección` (ej: `nombreCompleto,asc`).

**Response (200 OK):**
```json
{
  "content": [
    {
      "id": "7ac159a4-28b9-4672-911e-b8d438fc7bfd",
      "empresaId": "0757d941-2038-4abc-a0e3-fd1eaffd4bf3",
      "nombreCompleto": "Carlos Pérez González",
      "email": "carlos.perez@empresa.com",
      "rol": "EMPLEADO",
      "modalidadPerfil": "HIBRIDO",
      "fotoPatronUrl": "https://s3.amazonaws.com/cloudtime-buckets/comprobantes/carlos_patron.jpg",
      "saldoVacaciones": 15,
      "activo": true,
      "creadoEn": "2026-05-28T04:30:00.000Z"
    }
  ],
  "pageNumber": 0,
  "pageSize": 10,
  "totalElements": 1,
  "totalPages": 1
}
```

---

### GET /empleados/{empleadoId} — Obtener Detalle de Empleado
- **Ruta:** `GET /api/v1/admin/empleados/{empleadoId}`

**Response (200 OK):**
*(Igual que la respuesta de creación de empleado)*

---

### PUT /empleados/{empleadoId} — Actualizar Empleado
- **Ruta:** `PUT /api/v1/admin/empleados/{empleadoId}`
- **Descripción:** Actualiza los campos permitidos del perfil del empleado.

**Request Body (JSON):**
```json
{
  "nombreCompleto": "Carlos P. González",
  "email": "carlos.perez.new@empresa.com",
  "rol": "EMPLEADO",
  "modalidadPerfil": "REMOTO",
  "saldoVacaciones": 12,
  "activo": true
}
```
*Restricciones:*
- `nombreCompleto`: Obligatorio, Máx 250.
- `email`: Obligatorio, formato de correo válido, Máx 150.
- `rol`: Obligatorio. Valores: `SUPERADMIN`, `ADMIN_RRHH`, `EMPLEADO`.
- `modalidadPerfil`: Obligatorio. Valores: `PRESENCIAL`, `HIBRIDO`, `REMOTO`.
- `saldoVacaciones`: Obligatorio, entero mayor o igual a 0.
- `activo`: Obligatorio.

**Response (200 OK):**
*(Retorna el objeto `AdminEmpleadoResponse` actualizado)*

---

### PATCH /empleados/{empleadoId}/foto-patron — Actualizar Foto Patrón
- **Ruta:** `PATCH /api/v1/admin/empleados/{empleadoId}/foto-patron`
- **Descripción:** Actualiza de forma independiente la referencia a la foto patrón usada para la autenticación facial del empleado.

**Request Body (JSON):**
```json
{
  "fotoPatronUrl": "https://s3.amazonaws.com/cloudtime-buckets/patrones/nueva_foto_carlos.jpg"
}
```
*Restricciones:*
- `fotoPatronUrl`: Obligatorio, Máx 500 caracteres.

**Response (200 OK):**
*(Retorna el objeto `AdminEmpleadoResponse` con la nueva URL de la foto)*

---

### PATCH /empleados/{empleadoId}/modalidad — Cambiar Modalidad de Perfil
- **Ruta:** `PATCH /api/v1/admin/empleados/{empleadoId}/modalidad`
- **Descripción:** Modifica la modalidad por defecto asignada al perfil del empleado.

**Request Body (JSON):**
```json
{
  "modalidadPerfil": "REMOTO"
}
```
*Restricciones:*
- `modalidadPerfil`: Obligatorio. Valores: `PRESENCIAL`, `HIBRIDO`, `REMOTO`.

**Response (200 OK):**
*(Retorna el objeto `AdminEmpleadoResponse` con la modalidad actualizada)*

---

### PATCH /empleados/modalidad/lote — Cambiar Modalidad en Lote
- **Ruta:** `PATCH /api/v1/admin/empleados/modalidad/lote`
- **Descripción:** Actualiza la modalidad del perfil laboral de múltiples empleados simultáneamente.

**Request Body (JSON):**
```json
{
  "empleadoIds": [
    "7ac159a4-28b9-4672-911e-b8d438fc7bfd",
    "9d6a2d1f-829b-449e-b9b2-ea79038cf5a9"
  ],
  "modalidadPerfil": "PRESENCIAL"
}
```
*Restricciones:*
- `empleadoIds`: Lista no vacía de UUIDs.
- `modalidadPerfil`: Obligatorio. Valores: `PRESENCIAL`, `HIBRIDO`, `REMOTO`.

**Response (200 OK):**
```json
{
  "modalidadPerfilAplicada": "PRESENCIAL",
  "totalSolicitados": 2,
  "totalActualizados": 2,
  "empleadosNoEncontrados": []
}
```

---

### DELETE /empleados/{empleadoId} — Eliminar Empleado
- **Ruta:** `DELETE /api/v1/admin/empleados/{empleadoId}`
- **Response (204 No Content):** Sin cuerpo de respuesta.

---

### GET /dashboard/tiempo-real — Estadísticas del Dashboard en Tiempo Real
- **Ruta:** `GET /api/v1/admin/dashboard/tiempo-real`
- **Query Parameters:**
  - `fecha` (LocalDate, opcional, formato: `YYYY-MM-DD`): Fecha a consultar. Si no se provee, se toma la fecha actual del servidor.
- **Descripción:** Obtiene las métricas consolidadas sobre la asistencia del personal de la empresa del administrador de RRHH actual.

**Response (200 OK):**
```json
{
  "fecha": "2026-05-28",
  "totalActivos": 120,
  "totalPresentes": 85,
  "totalAusentes": 35,
  "totalTeletrabajo": 40,
  "totalPresencial": 45,
  "totalEnAlmuerzo": 12,
  "totalRetardos": 8,
  "totalFaltas": 2
}
```

---

## 3. REFERENCIA DE ENDPOINTS: MÓDULO REGLAS DE HORARIO

Base path: `/api/v1/admin/reglas-horario`

> [!WARNING]
> Las respuestas del módulo de reglas de horario utilizan un DTO mapeado a nivel de serialización JSON con formato **snake_case** (ej: `empresa_id` en lugar de `empresaId`). Preste especial atención a las llaves de respuesta descritas a continuación.

---

### POST / — Crear Nueva Regla de Horario
- **Ruta:** `POST /api/v1/admin/reglas-horario`
- **Descripción:** Crea una nueva jornada horaria asignable a los empleados para validar retardos y ausencias.

**Request Body (JSON - camelCase):**
```json
{
  "empresaId": "0757d941-2038-4abc-a0e3-fd1eaffd4bf3",
  "descripcion": "Jornada Administrativa General",
  "horaEntradaOficial": "08:00:00",
  "horaSalidaOficial": "17:00:00",
  "minutosToleranciaRetardo": 15,
  "tiempoLimiteFaltaMinutos": 120
}
```
*Restricciones:*
- `empresaId`: Obligatorio, UUID.
- `descripcion`: Obligatorio, Mín 1, Máx 100 caracteres.
- `horaEntradaOficial`: Obligatorio, formato de tiempo `HH:mm:ss`.
- `horaSalidaOficial`: Obligatorio, formato de tiempo `HH:mm:ss`.
- `minutosToleranciaRetardo`: Obligatorio, entero entre 0 y 480 (máx 8 horas).
- `tiempoLimiteFaltaMinutos`: Obligatorio, entero entre 1 y 1440 (máx 24 horas). Representa el tiempo máximo tras el cual la inasistencia se marca automáticamente como falta.

**Response (201 Created - snake_case):**
```json
{
  "id": "c138d82f-2d7c-473d-9be2-4411fb16a1b2",
  "empresa_id": "0757d941-2038-4abc-a0e3-fd1eaffd4bf3",
  "descripcion": "Jornada Administrativa General",
  "hora_entrada_oficial": "08:00:00",
  "hora_salida_oficial": "17:00:00",
  "minutos_tolerancia_retardo": 15,
  "tiempo_limite_falta_minutos": 120
}
```

---

### GET / — Listar Reglas de Horario de la Empresa
- **Ruta:** `GET /api/v1/admin/reglas-horario`

**Response (200 OK):**
```json
[
  {
    "id": "c138d82f-2d7c-473d-9be2-4411fb16a1b2",
    "empresa_id": "0757d941-2038-4abc-a0e3-fd1eaffd4bf3",
    "descripcion": "Jornada Administrativa General",
    "hora_entrada_oficial": "08:00:00",
    "hora_salida_oficial": "17:00:00",
    "minutos_tolerancia_retardo": 15,
    "tiempo_limite_falta_minutos": 120
  }
]
```

---

### GET /paginated — Listar Reglas con Paginación
- **Ruta:** `GET /api/v1/admin/reglas-horario/paginated`
- **Query Parameters:**
  - `page` (int, default: 0): Página.
  - `size` (int, default: 10): Cantidad.
  - `sort` (String, default: "descripcion,asc"): Ordenamiento (ej: `descripcion,desc`).

**Response (200 OK):**
```json
{
  "content": [
    {
      "id": "c138d82f-2d7c-473d-9be2-4411fb16a1b2",
      "empresa_id": "0757d941-2038-4abc-a0e3-fd1eaffd4bf3",
      "descripcion": "Jornada Administrativa General",
      "hora_entrada_oficial": "08:00:00",
      "hora_salida_oficial": "17:00:00",
      "minutos_tolerancia_retardo": 15,
      "tiempo_limite_falta_minutos": 120
    }
  ],
  "pageNumber": 0,
  "pageSize": 10,
  "totalElements": 1,
  "totalPages": 1
}
```

---

### GET /{id} — Obtener Regla por ID
- **Ruta:** `GET /api/v1/admin/reglas-horario/{id}`

**Response (200 OK):**
*(Igual que la respuesta de creación de regla)*

---

### GET /empresa/primaria — Obtener Regla Primaria
- **Ruta:** `GET /api/v1/admin/reglas-horario/empresa/primaria`
- **Descripción:** Devuelve la regla de horario primaria (por defecto) asignada a la empresa del administrador de RRHH actual.

**Response (200 OK):**
*(Igual que la respuesta de creación de regla)*

---

### PUT /{id} — Actualizar Regla de Horario
- **Ruta:** `PUT /api/v1/admin/reglas-horario/{id}`

**Request Body (JSON - camelCase):**
```json
{
  "descripcion": "Jornada Administrativa Flexible",
  "horaEntradaOficial": "08:30:00",
  "horaSalidaOficial": "17:30:00",
  "minutosToleranciaRetardo": 30,
  "tiempoLimiteFaltaMinutos": 180
}
```

**Response (200 OK):**
*(Retorna la regla modificada en formato snake_case)*

---

### DELETE /{id} — Eliminar Regla de Horario
- **Ruta:** `DELETE /api/v1/admin/reglas-horario/{id}`
- **Response (204 No Content):** Sin cuerpo.

---

## 4. REFERENCIA DE ENDPOINTS: MÓDULO CALENDARIO HÍBRIDO

Base path: `/api/v1/admin/calendario-hibrido`

---

### POST / — Asignar Carácter de Día Laboral
- **Ruta:** `POST /api/v1/admin/calendario-hibrido`
- **Descripción:** Configura para un día específico si el empleado flexible trabaja de forma remota o presencial.

**Request Body (JSON):**
```json
{
  "empleadoId": "7ac159a4-28b9-4672-911e-b8d438fc7bfd",
  "fecha": "2026-05-28",
  "caracterDia": "REMOTO"
}
```
*Restricciones:*
- `empleadoId`: Obligatorio, UUID.
- `fecha`: Obligatorio, formato `YYYY-MM-DD`.
- `caracterDia`: Obligatorio. Valores: `PRESENCIAL`, `REMOTO`.

**Response (201 Created):**
```json
{
  "id": "a9018bc3-0e8d-4bd9-9403-d6c1dfef3811",
  "empleadoId": "7ac159a4-28b9-4672-911e-b8d438fc7bfd",
  "fecha": "2026-05-28",
  "caracterDia": "REMOTO"
}
```

---

### PUT /{id} — Actualizar Carácter del Día
- **Ruta:** `PUT /api/v1/admin/calendario-hibrido/{id}`

**Request Body (JSON):**
*(Misma estructura que la creación)*

**Response (200 OK):**
*(Retorna el objeto `CalendarioHibridoResponse` actualizado)*

---

### GET / — Listar Calendario de Empleado
- **Ruta:** `GET /api/v1/admin/calendario-hibrido`
- **Query Parameters (Opcionales):**
  - `empleadoId` (UUID): ID del empleado.
  - `desde` (LocalDate, formato `YYYY-MM-DD`): Rango inicial.
  - `hasta` (LocalDate, formato `YYYY-MM-DD`): Rango final.

**Response (200 OK):**
```json
[
  {
    "id": "a9018bc3-0e8d-4bd9-9403-d6c1dfef3811",
    "empleadoId": "7ac159a4-28b9-4672-911e-b8d438fc7bfd",
    "fecha": "2026-05-28",
    "caracterDia": "REMOTO"
  }
]
```

---

### PUT /lote — Registro Masivo de Calendario
- **Ruta:** `PUT /api/v1/admin/calendario-hibrido/lote`
- **Descripción:** Crea o actualiza en bloque las asignaciones de días presenciales/remotos para un empleado.

**Request Body (JSON):**
```json
{
  "empleadoId": "7ac159a4-28b9-4672-911e-b8d438fc7bfd",
  "asignaciones": [
    {
      "empleadoId": "7ac159a4-28b9-4672-911e-b8d438fc7bfd",
      "fecha": "2026-06-01",
      "caracterDia": "PRESENCIAL"
    },
    {
      "empleadoId": "7ac159a4-28b9-4672-911e-b8d438fc7bfd",
      "fecha": "2026-06-02",
      "caracterDia": "REMOTO"
    }
  ]
}
```

**Response (200 OK):**
*(Retorna una lista conteniendo todos los objetos `CalendarioHibridoResponse` registrados/actualizados)*

---

### DELETE /{id} — Eliminar Asignación
- **Ruta:** `DELETE /api/v1/admin/calendario-hibrido/{id}`
- **Response (204 No Content):** Sin cuerpo.

---

## 5. REFERENCIA DE ENDPOINTS: MÓDULO GEOCERCAS REMOTAS

Base path: `/api/v1/admin/geocercas`

---

### POST / — Crear Geocerca para Marcación Remota
- **Ruta:** `POST /api/v1/admin/geocercas`
- **Descripción:** Establece el área perimetral permitida para que un empleado pueda marcar asistencia desde el celular/navegador en modalidad remota.

**Request Body (JSON):**
```json
{
  "empleadoId": "7ac159a4-28b9-4672-911e-b8d438fc7bfd",
  "descripcion": "Casa Empleado - Bogotá",
  "latitud": 4.60971,
  "longitud": -74.08175,
  "radioToleranciaMetros": 100
}
```
*Restricciones:*
- `empleadoId`: Obligatorio, UUID.
- `descripcion`: Obligatorio, Máx 100 caracteres.
- `latitud`: Obligatorio, decimal entre -90.00 y 90.00.
- `longitud`: Obligatorio, decimal entre -180.00 y 180.00.
- `radioToleranciaMetros`: Obligatorio, entero mayor que 0.

**Response (201 Created):**
```json
{
  "id": "e67b2d5a-19df-4e2a-89a1-7786dfaf15bb",
  "empleadoId": "7ac159a4-28b9-4672-911e-b8d438fc7bfd",
  "descripcion": "Casa Empleado - Bogotá",
  "latitud": 4.60971,
  "longitud": -74.08175,
  "radioToleranciaMetros": 100
}
```

---

### PUT /{id} — Actualizar Geocerca
- **Ruta:** `PUT /api/v1/admin/geocercas/{id}`

**Request Body (JSON):**
*(Misma estructura que la creación)*

**Response (200 OK):**
*(Retorna el objeto `GeocercaRemotaResponse` actualizado)*

---

### GET / — Listar Geocercas
- **Ruta:** `GET /api/v1/admin/geocercas`
- **Query Parameters (Opcional):**
  - `empleadoId` (UUID): Filtra las geocercas asociadas a un empleado.

**Response (200 OK):**
```json
[
  {
    "id": "e67b2d5a-19df-4e2a-89a1-7786dfaf15bb",
    "empleadoId": "7ac159a4-28b9-4672-911e-b8d438fc7bfd",
    "descripcion": "Casa Empleado - Bogotá",
    "latitud": 4.60971,
    "longitud": -74.08175,
    "radioToleranciaMetros": 100
  }
]
```

---

---

### GET /{id} — Obtener Geocerca por ID
- **Ruta:** `GET /api/v1/admin/geocercas/{id}`

**Response (200 OK):**
*(Igual que la respuesta de creación)*

---

### DELETE /{id} — Eliminar Geocerca
- **Ruta:** `DELETE /api/v1/admin/geocercas/{id}`
- **Response (204 No Content):** Sin cuerpo.

---

## 6. REFERENCIA DE ENDPOINTS: MÓDULO JUSTIFICACIONES E INCIDENCIAS

Base path: `/api/v1/admin/justificaciones`

---

### POST / — Crear Justificación Administrativa
- **Ruta:** `POST /api/v1/admin/justificaciones`
- **Descripción:** Crea un registro de justificación para una incidencia (retardo/falta) de un empleado.

**Request Body (JSON):**
```json
{
  "registroAsistenciaId": "bc108cf2-29da-411a-a002-c6cb92f98ccf",
  "motivoEmpleado": "Cita médica programada de control en EPS.",
  "urlComprobanteS3": "https://s3.amazonaws.com/cloudtime-buckets/justificaciones/comprobante_cita_carlos.pdf"
}
```
*Restricciones:*
- `registroAsistenciaId`: Obligatorio, UUID.
- `motivoEmpleado`: Obligatorio, Mín 1, Máx 4000 caracteres.
- `urlComprobanteS3`: Obligatorio, URL válida del comprobante en S3, Máx 500 caracteres.

**Response (201 Created):**
```json
{
  "id": "1fa02bd8-e65c-44bc-87bd-ea8a2879cf10",
  "registroAsistenciaId": "bc108cf2-29da-411a-a002-c6cb92f98ccf",
  "empleadoId": "7ac159a4-28b9-4672-911e-b8d438fc7bfd",
  "empleadoNombre": "Carlos Pérez González",
  "fecha": "2026-05-28",
  "motivoEmpleado": "Cita médica programada de control en EPS.",
  "urlComprobanteS3": "https://s3.amazonaws.com/cloudtime-buckets/justificaciones/comprobante_cita_carlos.pdf",
  "estadoSolicitud": "PENDIENTE",
  "comentariosAdministrador": null,
  "procesadoEn": null,
  "creadoEn": "2026-05-28T04:35:00.000Z"
}
```

---

### GET /pendientes — Listar Justificaciones Pendientes
- **Ruta:** `GET /api/v1/admin/justificaciones/pendientes`
- **Descripción:** Obtiene la bandeja de solicitudes de justificación pendientes de revisión por parte del administrador de RRHH.
- **Query Parameters:**
  - `page` (int, default: 0): Índice de la página.
  - `size` (int, default: 10): Cantidad por página.
  - `sort` (String, default: "creadoEn,desc"): Campo y dirección de ordenación.

**Response (200 OK):**
```json
{
  "content": [
    {
      "id": "1fa02bd8-e65c-44bc-87bd-ea8a2879cf10",
      "registroAsistenciaId": "bc108cf2-29da-411a-a002-c6cb92f98ccf",
      "empleadoId": "7ac159a4-28b9-4672-911e-b8d438fc7bfd",
      "empleadoNombre": "Carlos Pérez González",
      "fecha": "2026-05-28",
      "motivoEmpleado": "Cita médica programada de control en EPS.",
      "urlComprobanteS3": "https://s3.amazonaws.com/cloudtime-buckets/justificaciones/comprobante_cita_carlos.pdf",
      "estadoSolicitud": "PENDIENTE",
      "comentariosAdministrador": null,
      "procesadoEn": null,
      "creadoEn": "2026-05-28T04:35:00.000Z"
    }
  ],
  "pageNumber": 0,
  "pageSize": 10,
  "totalElements": 1,
  "totalPages": 1
}
```

---

### PUT /{justificacionId}/aprobar — Aprobar Justificación
- **Ruta:** `PUT /api/v1/admin/justificaciones/{justificacionId}/aprobar`
- **Descripción:** Aprueba la justificación e inyecta el cambio de estado en el registro de asistencia original a `FALTA_JUSTIFICADA` (o retardo justificado).

**Request Body (JSON - Opcional):**
```json
{
  "comentariosAdministrador": "Soporte médico válido y verificado."
}
```
*Restricciones:*
- `comentariosAdministrador`: Opcional, Máx 2000 caracteres.

**Response (200 OK):**
```json
{
  "id": "1fa02bd8-e65c-44bc-87bd-ea8a2879cf10",
  "registroAsistenciaId": "bc108cf2-29da-411a-a002-c6cb92f98ccf",
  "empleadoId": "7ac159a4-28b9-4672-911e-b8d438fc7bfd",
  "empleadoNombre": "Carlos Pérez González",
  "fecha": "2026-05-28",
  "motivoEmpleado": "Cita médica programada de control en EPS.",
  "urlComprobanteS3": "https://s3.amazonaws.com/cloudtime-buckets/justificaciones/comprobante_cita_carlos.pdf",
  "estadoSolicitud": "APROBADO",
  "comentariosAdministrador": "Soporte médico válido y verificado.",
  "procesadoEn": "2026-05-28T04:40:00.000Z",
  "creadoEn": "2026-05-28T04:35:00.000Z"
}
```

---

### PUT /{justificacionId}/rechazar — Rechazar Justificación
- **Ruta:** `PUT /api/v1/admin/justificaciones/{justificacionId}/rechazar`
- **Descripción:** Rechaza la solicitud de justificación sin modificar el estado de la asistencia del empleado.

**Request Body (JSON - Opcional):**
```json
{
  "comentariosAdministrador": "El comprobante no corresponde al día de la falta."
}
```

**Response (200 OK):**
*(Retorna el objeto `JustificacionResponse` con `estadoSolicitud` = `RECHAZADO`)*

---

## 7. REFERENCIA DE ENDPOINTS: MÓDULO VACACIONES

Base path: `/api/v1/admin/vacaciones`

---

### POST / — Registrar Solicitud de Vacaciones (RRHH)
- **Ruta:** `POST /api/v1/admin/vacaciones`
- **Descripción:** Permite a RRHH ingresar una solicitud de vacaciones en nombre de un empleado.

**Request Body (JSON):**
```json
{
  "empleadoId": "7ac159a4-28b9-4672-911e-b8d438fc7bfd",
  "fechaInicio": "2026-06-15",
  "fechaFin": "2026-06-30"
}
```
*Restricciones:*
- `empleadoId`: Obligatorio, UUID.
- `fechaInicio`: Obligatorio, formato `YYYY-MM-DD`.
- `fechaFin`: Obligatorio, formato `YYYY-MM-DD` (Debe ser posterior o igual a `fechaInicio`).

**Response (201 Created):**
```json
{
  "id": "fbc09d1e-829b-449e-b9b2-ea79038cf5a9",
  "empleadoId": "7ac159a4-28b9-4672-911e-b8d438fc7bfd",
  "empleadoNombre": "Carlos Pérez González",
  "fechaInicio": "2026-06-15",
  "fechaFin": "2026-06-30",
  "diasSolicitados": 15,
  "estadoSolicitud": "PENDIENTE",
  "saldoVacacionesAntes": 15,
  "saldoVacacionesDespues": 0,
  "creadoEn": "2026-05-28T04:36:00.000Z"
}
```

---

### GET /pendientes — Listar Solicitudes de Vacaciones Pendientes
- **Ruta:** `GET /api/v1/admin/vacaciones/pendientes`
- **Query Parameters:**
  - `page` (int, default: 0): Página.
  - `size` (int, default: 10): Tamaño.
  - `sort` (String, default: "creadoEn,desc"): Criterio de orden.

**Response (200 OK):**
```json
{
  "content": [
    {
      "id": "fbc09d1e-829b-449e-b9b2-ea79038cf5a9",
      "empleadoId": "7ac159a4-28b9-4672-911e-b8d438fc7bfd",
      "empleadoNombre": "Carlos Pérez González",
      "fechaInicio": "2026-06-15",
      "fechaFin": "2026-06-30",
      "diasSolicitados": 15,
      "estadoSolicitud": "PENDIENTE",
      "saldoVacacionesAntes": 15,
      "saldoVacacionesDespues": 0,
      "creadoEn": "2026-05-28T04:36:00.000Z"
    }
  ],
  "pageNumber": 0,
  "pageSize": 10,
  "totalElements": 1,
  "totalPages": 1
}
```

---

### PUT /{solicitudId}/aprobar — Aprobar Vacaciones
- **Ruta:** `PUT /api/v1/admin/vacaciones/{solicitudId}/aprobar`
- **Descripción:** Aprueba la solicitud y descuenta automáticamente los días laborables correspondientes del saldo de vacaciones del empleado (`saldoVacacionesDespues` se consolida en el perfil).

**Response (200 OK):**
*(Retorna el objeto `VacacionesResponse` con `estadoSolicitud` = `APROBADO`)*

---

### PUT /{solicitudId}/rechazar — Rechazar Vacaciones
- **Ruta:** `PUT /api/v1/admin/vacaciones/{solicitudId}/rechazar`
- **Descripción:** Rechaza las vacaciones sin afectar el saldo de vacaciones del trabajador.

**Response (200 OK):**
*(Retorna el objeto `VacacionesResponse` con `estadoSolicitud` = `RECHAZADO`)*

---

### GET /saldo/{empleadoId} — Consultar Saldo de Vacaciones de un Empleado
- **Ruta:** `GET /api/v1/admin/vacaciones/saldo/{empleadoId}`

**Response (200 OK):**
```json
{
  "empleadoId": "7ac159a4-28b9-4672-911e-b8d438fc7bfd",
  "empleadoNombre": "Carlos Pérez González",
  "saldoVacaciones": 15
}
```

---

## 8. REFERENCIA DE ENDPOINTS: MÓDULO PRE-NÓMINA Y REPORTES

Base path: `/api/v1/admin/reportes-prenomina`

---

### POST /generar — Procesar y Guardar Reportes de Pre-Nómina
- **Ruta:** `POST /api/v1/admin/reportes-prenomina/generar`
- **Descripción:** Calcula en el backend la pre-nómina consolidada por empleado y periodo, aplicando inasistencias, recargos y horas extras y guardando de forma persistente los registros de cierre.

**Request Body (JSON):**
```json
{
  "fechaInicio": "2026-05-01",
  "fechaFin": "2026-05-30",
  "empleadoId": "7ac159a4-28b9-4672-911e-b8d438fc7bfd"
}
```
*Restricciones:*
- `fechaInicio`: Obligatorio, LocalDate en el pasado o presente.
- `fechaFin`: Obligatorio, LocalDate en el pasado o presente.
- `empleadoId`: Opcional (Si se omite, se procesa la nómina de TODOS los empleados del tenant/empresa).

**Response (201 Created):**
```json
[
  {
    "id": "e67b2d5a-19df-4e2a-89a1-7786dfaf15bb",
    "empresaId": "0757d941-2038-4abc-a0e3-fd1eaffd4bf3",
    "empleadoId": "7ac159a4-28b9-4672-911e-b8d438fc7bfd",
    "empleadoNombre": "Carlos Pérez González",
    "mesPeriodo": 5,
    "anioPeriodo": 2026,
    "diasTrabajadosEfectivos": 20,
    "diasFaltaInjustificada": 2,
    "horasExtrasDiurnasTotales": 5.50,
    "horasExtrasNocturnasTotales": 2.00,
    "montoSalarioBaseProporcional": 1866666.67,
    "montoGananciaExtras": 125000.00,
    "montoDeduccionesFaltas": 133333.33,
    "montoNetoPagar": 1858333.34,
    "estadoReporte": "BORRADOR",
    "generadoEl": "2026-05-28T04:37:00.000Z"
  }
]
```

---

### GET / — Listar Reportes Persistidos de Pre-Nómina
- **Ruta:** `GET /api/v1/admin/reportes-prenomina`
- **Query Parameters (Obligatorios y Opcionales):**
  - `fechaInicio` (LocalDate, requerido): Rango inicial de generación del reporte.
  - `fechaFin` (LocalDate, requerido): Rango final.
  - `empleadoId` (UUID, opcional): Filtra por empleado.
  - `estadoReporte` (String, opcional): Filtra por estado (`BORRADOR`, `APROBADO_RRHH`, `PROCESADO_PAGO`).
  - `page` (int, default: 0): Paginación.
  - `size` (int, default: 10): Tamaño.
  - `sort` (String, default: "anioPeriodo,desc"): Campo de ordenamiento.

**Response (200 OK):**
```json
{
  "content": [
    {
      "id": "e67b2d5a-19df-4e2a-89a1-7786dfaf15bb",
      "empresaId": "0757d941-2038-4abc-a0e3-fd1eaffd4bf3",
      "empleadoId": "7ac159a4-28b9-4672-911e-b8d438fc7bfd",
      "empleadoNombre": "Carlos Pérez González",
      "mesPeriodo": 5,
      "anioPeriodo": 2026,
      "diasTrabajadosEfectivos": 20,
      "diasFaltaInjustificada": 2,
      "horasExtrasDiurnasTotales": 5.50,
      "horasExtrasNocturnasTotales": 2.00,
      "montoSalarioBaseProporcional": 1866666.67,
      "montoGananciaExtras": 125000.00,
      "montoDeduccionesFaltas": 133333.33,
      "montoNetoPagar": 1858333.34,
      "estadoReporte": "BORRADOR",
      "generadoEl": "2026-05-28T04:37:00.000Z"
    }
  ],
  "pageNumber": 0,
  "pageSize": 10,
  "totalElements": 1,
  "totalPages": 1
}
```

---

### GET /consolidado — Consultar Pre-Nómina en Memoria (Simulación)
- **Ruta:** `GET /api/v1/admin/reportes-prenomina/consolidado`
- **Descripción:** Realiza el cálculo matemático en caliente sin guardar registro en BD. Útil para previsualización inmediata.
- **Query Parameters:**
  - `fechaInicio` (LocalDate, requerido): Rango inicial.
  - `fechaFin` (LocalDate, requerido): Rango final.
  - `empleadoId` (UUID, opcional): Filtra por empleado específico.

**Response (200 OK):**
```json
{
  "empleadoId": "7ac159a4-28b9-4672-911e-b8d438fc7bfd",
  "empleadoNombre": "Carlos Pérez González",
  "tipoContrato": "TERMINO_INDEFINIDO",
  "tipoMoneda": "COP",
  "mesPeriodo": 5,
  "anioPeriodo": 2026,
  "fechaInicio": "2026-05-01",
  "fechaFin": "2026-05-30",
  "diasTrabajadosEfectivos": 20,
  "diasFaltaInjustificada": 2,
  "diasVacacionesAprobadas": 0,
  "llegadasTardias": 5,
  "horasTrabajadasTotales": 160.00,
  "horasExtrasDiurnasTotales": 5.50,
  "horasExtrasNocturnasTotales": 2.00,
  "montoSalarioBaseProporcional": 1866666.67,
  "montoGananciaExtras": 125000.00,
  "montoDeduccionesFaltas": 133333.33,
  "montoNetoPagar": 1858333.34
}
```

---

### GET /export/csv — Exportar Consolidado a CSV
- **Ruta:** `GET /api/v1/admin/reportes-prenomina/export/csv`
- **Query Parameters:** *(Igual que `/consolidado`)*
- **Response (200 OK):** Archivo binario de tipo `text/csv`. Header `Content-Disposition` con `attachment; filename=pre-nomina.csv`.

---

### GET /export/excel — Exportar Consolidado a Excel
- **Ruta:** `GET /api/v1/admin/reportes-prenomina/export/excel`
- **Query Parameters:** *(Igual que `/consolidado`)*
- **Response (200 OK):** Archivo binario de tipo `application/vnd.openxmlformats-officedocument.spreadsheetml.sheet`. Header `Content-Disposition` con `attachment; filename=pre-nomina.xlsx`.

---

### GET /export/pdf — Exportar Consolidado a PDF
- **Ruta:** `GET /api/v1/admin/reportes-prenomina/export/pdf`
- **Query Parameters:** *(Igual que `/consolidado`)*
- **Response (200 OK):** Archivo binario de tipo `application/pdf`. Header `Content-Disposition` con `attachment; filename=pre-nomina.pdf`.

---

### GET /{id} — Obtener Reporte Guardado por ID
- **Ruta:** `GET /api/v1/admin/reportes-prenomina/{id}`
- **Response (200 OK):**
*(Igual que un elemento de la respuesta `/generar`)*

---

## 9. REFERENCIA DE ENDPOINTS: MÓDULO CONFIGURACIÓN DE RECARGOS

Base path: `/api/v1/admin/recargos`

> [!WARNING]
> La respuesta de configuración de recargos utiliza un DTO mapeado a nivel de serialización JSON con formato **snake_case** (ej: `factor_hora_extra_diurna`). Sin embargo, el request body (payload de entrada) se envía en formato estándar **camelCase** (ej: `factorHoraExtraDiurna`). Preste especial atención a las llaves.

---

### POST / — Crear Configuración de Recargos de Empresa
- **Ruta:** `POST /api/v1/admin/recargos`
- **Descripción:** Registra las políticas de recargos por horas extras, dominicales y festivos para la empresa del administrador de RRHH actual.

**Request Body (JSON - camelCase):**
```json
{
  "factorHoraExtraDiurna": 1.25,
  "factorHoraExtraNocturna": 1.75,
  "factorHoraDominicalFestiva": 2.00,
  "multaRetardoPorMinuto": 500.00
}
```
*Restricciones:*
- `factorHoraExtraDiurna`: Obligatorio, decimal entre 1.0 y 3.0.
- `factorHoraExtraNocturna`: Obligatorio, decimal entre 1.0 y 3.0.
- `factorHoraDominicalFestiva`: Obligatorio, decimal entre 1.0 y 3.0.
- `multaRetardoPorMinuto`: Obligatorio, decimal entre 0.0 y 999999.99.

**Response (210 Created - snake_case):**
```json
{
  "id": "e67b2d5a-19df-4e2a-89a1-7786dfaf15bb",
  "factor_hora_extra_diurna": 1.25,
  "factor_hora_extra_nocturna": 1.75,
  "factor_hora_dominical_festiva": 2.00,
  "multa_retardo_por_minuto": 500.00
}
```

---

### GET / — Obtener Configuración de Recargos
- **Ruta:** `GET /api/v1/admin/recargos`
- **Descripción:** Obtiene la parametrización de recargos configurada para la empresa del administrador de RRHH actual.

**Response (200 OK - snake_case):**
```json
{
  "id": "e67b2d5a-19df-4e2a-89a1-7786dfaf15bb",
  "factor_hora_extra_diurna": 1.25,
  "factor_hora_extra_nocturna": 1.75,
  "factor_hora_dominical_festiva": 2.00,
  "multa_retardo_por_minuto": 500.00
}
```

---

### PUT / — Actualizar Configuración de Recargos
- **Ruta:** `PUT /api/v1/admin/recargos`
- **Descripción:** Modifica los factores de recargos y multas de la empresa del administrador de RRHH actual.

**Request Body (JSON - camelCase):**
```json
{
  "factorHoraExtraDiurna": 1.30,
  "factorHoraExtraNocturna": 1.80,
  "factorHoraDominicalFestiva": 2.10,
  "multaRetardoPorMinuto": 600.00
}
```

**Response (200 OK - snake_case):**
```json
{
  "id": "e67b2d5a-19df-4e2a-89a1-7786dfaf15bb",
  "factor_hora_extra_diurna": 1.30,
  "factor_hora_extra_nocturna": 1.80,
  "factor_hora_dominical_festiva": 2.10,
  "multa_retardo_por_minuto": 600.00
}
```

---

### DELETE / — Eliminar Configuración de Recargos
- **Ruta:** `DELETE /api/v1/admin/recargos`
- **Response (204 No Content):** Sin cuerpo.

---

## 10. REFERENCIA DE ENDPOINTS: CARGA DE ARCHIVOS A AWS S3 (MEDIA)

Base path: `/api/v1/media`

---

### POST /upload/justificacion — Subir Comprobante de Justificación
- **Ruta:** `POST /api/v1/media/upload/justificacion`
- **Descripción:** Sube un archivo de soporte (evidencia) para justificaciones de retardo/falta al bucket de S3.
- **Seguridad:** Requiere cualquier usuario autenticado (`EMPLEADO`, `ADMIN_RRHH`, `SUPERADMIN`).
- **Request Body (Multipart Form-Data):**
  - `file`: Archivo binario (PDF, PNG, JPG, etc.).
- **Response (200 OK):**
  ```json
  {
    "url": "https://mi-bucket-s3.s3.us-east-1.amazonaws.com/justificaciones/2d4993bd-a5b5-4a5f-9db0-5a3dcfcf4756.pdf"
  }
  ```

---

### POST /upload/empleado — Subir Foto de Colaborador
- **Ruta:** `POST /api/v1/media/upload/empleado`
- **Descripción:** Sube la foto de perfil o biometría de un empleado al bucket de S3.
- **Seguridad:** Requiere cualquier usuario autenticado (`EMPLEADO`, `ADMIN_RRHH`, `SUPERADMIN`).
- **Request Body (Multipart Form-Data):**
  - `file`: Archivo binario (PNG, JPG, etc.).
- **Response (200 OK):**
  ```json
  {
    "url": "https://mi-bucket-s3.s3.us-east-1.amazonaws.com/empleados/8e8a8b1a-28bb-4a5b-91cc-a003ccdc4ef1.png"
  }
  ```

---

## 11. PROTOCOLO DE ERRORES Y VALIDACIONES DE PAYLOAD

El backend intercepta las excepciones estándar de validación de Spring Validation (`@NotBlank`, `@Email`, `@Min`, `@Max`, `@DecimalMin`, `@DecimalMax`) y retorna respuestas con códigos 4xx estructuradas con la siguiente forma:

### 11.1. Error 400 - Bad Request (Validación fallida en campos)
Ocurre cuando el cuerpo enviado no satisface las restricciones declaradas en los DTOs.
```json
{
  "timestamp": "2026-05-28T04:40:00.123+00:00",
  "status": 400,
  "error": "Bad Request",
  "message": "Validation failed: El email no tiene un formato valido",
  "path": "/api/v1/admin/empleados"
}
```

### 11.2. Error 401 - Unauthorized (Token Inválido o Ausente)
Ocurre cuando falta la cabecera `Authorization` o el token JWT expiró.
```json
{
  "timestamp": "2026-05-28T04:40:05.123+00:00",
  "status": 401,
  "error": "Unauthorized",
  "message": "Acceso denegado: Token invalido o expirado.",
  "path": "/api/v1/admin/empleados"
}
```

### 11.3. Error 403 - Forbidden (Permisos insuficientes)
Ocurre cuando el JWT es válido pero el usuario tiene rol `EMPLEADO` e intenta ingresar a rutas `/api/v1/admin/*`.
```json
{
  "timestamp": "2026-05-28T04:40:10.123+00:00",
  "status": 403,
  "error": "Forbidden",
  "message": "Access Denied",
  "path": "/api/v1/admin/empleados"
}
```

### 11.4. Error 404 - Resource Not Found (Recurso inexistente)
Ocurre al intentar consultar o modificar entidades (empleados, geocercas, solicitudes) con IDs que no existen en la base de datos del tenant.
```json
{
  "timestamp": "2026-05-28T04:40:15.123+00:00",
  "status": 404,
  "error": "Not Found",
  "message": "Empleado no encontrado con ID: 7ac159a4-28b9-4672-911e-b8d438fc7bfd",
  "path": "/api/v1/admin/empleados/7ac159a4-28b9-4672-911e-b8d438fc7bfd"
}
```

### 11.5. Error 409 - Conflict (Duplicación de correo / registros)
Ocurre cuando se viola una restricción de unicidad, como registrar un empleado con un correo que ya existe en el sistema.
```json
{
  "timestamp": "2026-05-28T04:40:20.123+00:00",
  "status": 409,
  "error": "Conflict",
  "message": "Ya existe un registro con el email proporcionado: carlos.perez@empresa.com",
  "path": "/api/v1/admin/empleados"
}
```


