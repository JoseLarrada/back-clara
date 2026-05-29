# CONTRATO_API_SUPERADMIN

Contrato de integración REST para el módulo SuperAdmin (Panel de administración global de empresas).

Base URL: `/api/v1/superadmin`

Este documento define de forma estricta los endpoints, payloads, respuestas y errores que el frontend (React) debe implementar para integrarse con el backend.

-------------------------------------------------------------------------------
1) REGLAS GLOBALES Y SEGURIDAD
-------------------------------------------------------------------------------

- Todos los endpoints de este contrato están bajo la base URL: `https://<HOST>:<PORT>/api/v1/superadmin`.
- Autenticación: obligatorio el header `Authorization: Bearer <JWT>` en todas las peticiones.
- Autorización: acceso restringido únicamente a usuarios que posean la autoridad (rol) `ROLE_SUPER_ADMIN`.
  - Si no se provee el header o el token es inválido/expirado → respuesta 401 Unauthorized.
  - Si el token es válido pero no tiene el rol pedido → 403 Forbidden.

Nota: El backend valida el JWT y extrae roles directamente del claim de roles. Asegúrese de que el token incluya `ROLE_SUPER_ADMIN`.

-------------------------------------------------------------------------------
INFRAESTRUCTURA DE AUTENTICACIÓN (ENDPOINTS DE SEGURIDAD)
-------------------------------------------------------------------------------

El servicio de autenticación expone los siguientes endpoints bajo `/api/v1/auth`.

- POST `/api/v1/auth/login`
  - Request Body: `AuthenticationRequest` (email, password). Ejemplo:
    ```json
    { "email": "jose@mail.com", "password": "pAssword1!_" }
    ```
  - Response (200 OK): `AuthenticationResponse` con los siguientes campos:
    - `access_token` (String): JWT firmada (ACCESS_TOKEN). Usar para Authorization header.
    - `refresh_token` (String): JWT firmada (REFRESH_TOKEN). Usar para obtener nuevos access tokens.
    - `token_type` (String): típicamente `Bearer`.
    Ejemplo de respuesta:
    ```json
    {
      "access_token": "eyJhbGciOiJSUzI1NiJ9...",
      "refresh_token": "eyJhbGciOiJSUzI1NiJ9...",
      "token_type": "Bearer"
    }
    ```

- POST `/api/v1/auth/refresh`
  - Request Body: `RefreshRequest` { "refreshToken": "<REFRESH_TOKEN>" }
  - Response (200 OK): `AuthenticationResponse` (misma forma que el login) con un nuevo `access_token` y, en algunos flujos, un nuevo `refresh_token`.
  - Reglas: el servicio valida que el token recibido sea de tipo `REFRESH_TOKEN` (claim interno `token_type = "REFRESH_TOKEN"`) y que el subject coincida con el usuario objetivo; además valida expiración. Si falla, retornará 401/400 según el caso.

- POST `/api/v1/auth/register` (si está habilitado)
  - Request Body: `RegistrationRequest` (datos del nuevo usuario). Responde 201 Created.

-------------------------------------------------------------------------------
Estructura y contenido del JWT (claims)
-------------------------------------------------------------------------------

El `access_token` emitido es una JWT firmada asimétricamente (RSA) y contiene los siguientes claims relevantes para el frontend:

- `sub` (subject): el username / email del usuario.
- `exp`: fecha de expiración (UNIX epoch seconds). El frontend puede decodificar el payload para verificar expiración localmente.
- `tenant_id` (claim key = `tenant_id`): UUID (String) del tenant/empresa del usuario. Importante si la UI muestra contexto multi-tenant.
- `user_id` (claim key = `user_id`): UUID del usuario (empleado).
- `roles` (claim key = `roles`): arreglo de Strings con las autoridades, p.ej. `["ROLE_SUPER_ADMIN"]` o `['SUPERADMIN']` según emisor. El backend espera autoridades en este claim.
- `context` (claim key = `context`): objeto con datos rápidos, por ejemplo `{ "nombre_completo": "Jose Larrada", "modalidad_perfil": "HIBRIDO" }`.
- `token_type` (claim key = `token_type`): `ACCESS_TOKEN` o `REFRESH_TOKEN`.

Ejemplo de payload decodificado (JSON) de un access token:
```json
{
  "token_type": "ACCESS_TOKEN",
  "tenant_id": "0757d941-2038-4abc-a0e3-fd1eaffd4bf3",
  "user_id": "877a8298-bd09f-43a6-b3f9-6e63e8ddc5d3",
  "roles": ["ROLE_SUPER_ADMIN"],
  "context": { "nombre_completo": "Jose Larrada", "modalidad_perfil": "HIBRIDO" },
  "sub": "jose@mail.com",
  "iat": 1779823989,
  "exp": 1779910389
}
```

Recomendaciones para el Frontend sobre tokens:
- Almacenar el `access_token` en memoria (Redux/Context) o en storage seguro (si se usa, preferir httpOnly cookie en vez de localStorage). Evitar exposición innecesaria.
- Antes de cada petición, agregar `Authorization: Bearer <access_token>`.
- Detectar `401 Unauthorized` y, si el `refresh_token` existe, llamar a `/api/v1/auth/refresh` para obtener un nuevo `access_token`. Si el refresh falla (expirado), redirigir a login.
- Decodificar el payload JWT para extraer `roles` y `tenant_id` para lógica de UI (mostrar/ocultar elementos según roles y contexto del tenant).

-------------------------------------------------------------------------------
2) REFERENCIA DE ENDPOINTS
-------------------------------------------------------------------------------

Base path concreto de este módulo: `/api/v1/superadmin/empresas`

Cada endpoint especificado a continuación incluye: método, ruta, parámetros, ejemplos de request/response y códigos HTTP esperados.

---------------------------------------------------------------------------
POST /empresas — Crear empresa
---------------------------------------------------------------------------

- Ruta: `POST /api/v1/superadmin/empresas`
- Descripción: Crea una nueva empresa globalmente (fuera del filtrado tenant). Al crear la empresa se precargan plantillas de horario según `rubro` mediante la `PlantillaHorarioFactory`.
- Seguridad: requiere `Authorization: Bearer <JWT>` y `ROLE_SUPER_ADMIN`.

Request Body (JSON) — ejemplo
```json
{
  "nombre": "Fábrica Metalúrgica del Norte",
  "nitRut": "901445882-3",
  "rubro": "INDUSTRIAL",
  "limiteEmpleados": 120,
  "estadoLicencia": "ACTIVO"
}
```

Response (201 Created) — cuando se crea correctamente
```json
{
  "id": "4f9e2d3a-...",
  "nombre": "Fábrica Metalúrgica del Norte",
  "nitRut": "901445882-3",
  "rubro": "INDUSTRIAL",
  "limiteEmpleados": 120,
  "estadoLicencia": "ACTIVO",
  "creadoEn": "2026-05-26T15:00:00Z",
  "actualizadoEn": "2026-05-26T15:00:00Z"
}
```

Responses de error comunes:
- 400 Bad Request: payload inválido (falla validation annotations).
- 401 Unauthorized: token inválido/expirado o no enviado.
- 403 Forbidden: token válido pero sin `ROLE_SUPER_ADMIN`.
- 409 Conflict: NIT/RUT duplicado. Ejemplo (cuando `nitRut` ya existe):
```json
{
  "timestamp": "2026-05-26T15:05:00.123+00:00",
  "status": 409,
  "error": "Conflict",
  "message": "Ya existe una empresa con el NIT/RUT proporcionado: 901445882-3",
  "path": "/api/v1/superadmin/empresas"
}
```

---------------------------------------------------------------------------
GET /empresas — Listar empresas con filtros, paginación y ordenamiento
---------------------------------------------------------------------------

- Ruta: `GET /api/v1/superadmin/empresas`
- Descripción: Lista empresas aplicando filtros dinámicos (combinables). La paginación es por índice CERO (page = 0 es la primera página).
- Seguridad: `Authorization` + `ROLE_SUPER_ADMIN`.

Query Parameters (opcionales)
| Parámetro | Tipo | Default | Descripción |
|---|---:|---|---|
| nombre | String | - | Busca empresas cuyo `nombre` contiene (LIKE, case-insensitive) el texto provisto. |
| nitRut | String | - | Busca por `nitRut` exacto. |
| rubro | String | - | Filtra por `rubro` exacto (se normaliza a mayúsculas en el servidor). |
| estadoLicencia | String | - | Filtra por `estadoLicencia` exacto (ej: ACTIVO). |
| page | int | 0 | Índice de página (cero-based). |
| size | int | 10 | Tamaño de página. |
| sort | String | creadoEn,desc | Campo y dirección separados por coma. Ej: `nombre,asc` o `creadoEn,desc`. |

Ejemplo de petición (listado página 0, tamaño 10, orden por `creadoEn` descendente):
```
GET /api/v1/superadmin/empresas?nombre=Fábrica&page=0&size=10&sort=creadoEn,desc
Authorization: Bearer <JWT>
```

Response (200 OK) — `PageResponse<EmpresaResponse>`
```json
{
  "content": [
    {
      "id": "4f9e2d3a-...",
      "nombre": "Fábrica Metalúrgica del Norte",
      "nitRut": "901445882-3",
      "rubro": "INDUSTRIAL",
      "limiteEmpleados": 120,
      "estadoLicencia": "ACTIVO",
      "creadoEn": "2026-05-26T15:00:00Z",
      "actualizadoEn": "2026-05-26T15:00:00Z"
    }
  ],
  "pageNumber": 0,
  "pageSize": 10,
  "totalElements": 42,
  "totalPages": 5
}
```

Notas:
- Los filtros son combinables — p. ej. `?nombre=Fábrica&rubro=INDUSTRIAL` aplica ambos.
- La búsqueda por `nombre` utiliza `LIKE %texto%` case-insensitive.

---------------------------------------------------------------------------
GET /empresas/{id} — Obtener empresa por id
---------------------------------------------------------------------------

- Ruta: `GET /api/v1/superadmin/empresas/{id}`
- Descripción: Retorna la representación segura `EmpresaResponse`.
- Seguridad: `Authorization` + `ROLE_SUPER_ADMIN`.

Response (200 OK)
```json
{
  "id": "4f9e2d3a-...",
  "nombre": "Fábrica Metalúrgica del Norte",
  "nitRut": "901445882-3",
  "rubro": "INDUSTRIAL",
  "limiteEmpleados": 120,
  "estadoLicencia": "ACTIVO",
  "creadoEn": "2026-05-26T15:00:00Z",
  "actualizadoEn": "2026-05-26T15:00:00Z"
}
```

- 404 Not Found si la empresa no existe.

---------------------------------------------------------------------------
PUT /empresas/{id} — Actualizar empresa
---------------------------------------------------------------------------

- Ruta: `PUT /api/v1/superadmin/empresas/{id}`
- Descripción: Actualiza campos permitidos (nombre, rubro, limiteEmpleados).
- Seguridad: `Authorization` + `ROLE_SUPER_ADMIN`.

Request Body (ejemplo)
```json
{
  "nombre": "Fábrica Metalúrgica del Norte - Sucursal",
  "rubro": "INDUSTRIAL",
  "limiteEmpleados": 150
}
```

Response (200 OK)
```json
{
  "id": "4f9e2d3a-...",
  "nombre": "Fábrica Metalúrgica del Norte - Sucursal",
  "nitRut": "901445882-3",
  "rubro": "INDUSTRIAL",
  "limiteEmpleados": 150,
  "estadoLicencia": "ACTIVO",
  "creadoEn": "2026-05-26T15:00:00Z",
  "actualizadoEn": "2026-05-26T16:00:00Z"
}
```

---------------------------------------------------------------------------
PATCH /empresas/{id}/estado — Actualizar estado de licencia
---------------------------------------------------------------------------

- Ruta: `PATCH /api/v1/superadmin/empresas/{id}/estado`
- Descripción: Actualiza inmediatamente el campo `estadoLicencia` a `ACTIVO` o `SUSPENDIDO`.
- Seguridad: `Authorization` + `ROLE_SUPER_ADMIN`.

Request Body (ejemplo)
```json
{
  "estado": "SUSPENDIDO"
}
```

Response (200 OK)
```json
{
  "id": "4f9e2d3a-...",
  "nombre": "Fábrica Metalúrgica del Norte",
  "nitRut": "901445882-3",
  "rubro": "INDUSTRIAL",
  "limiteEmpleados": 120,
  "estadoLicencia": "SUSPENDIDO",
  "creadoEn": "2026-05-26T15:00:00Z",
  "actualizadoEn": "2026-05-26T17:00:00Z"
}
```

---------------------------------------------------------------------------
DELETE /empresas/{id} — Eliminar empresa
---------------------------------------------------------------------------

- Ruta: `DELETE /api/v1/superadmin/empresas/{id}`
- Descripción: Elimina la entidad empresa (operación destructiva). Validar permisos y efectos en cascada.
- Seguridad: `Authorization` + `ROLE_SUPER_ADMIN`.

Response (204 No Content)
- Sin body.

---------------------------------------------------------------------------
GET /empresas/dashboard — Dashboard global de consumo (RF05)
---------------------------------------------------------------------------

- Ruta: `GET /api/v1/superadmin/empresas/dashboard`
- Descripción: Retorna métricas consolidadas a nivel global: total de empresas activas y total de empleados global.
- Seguridad: `Authorization` + `ROLE_SUPER_ADMIN`.

Response (200 OK)
```json
{
  "totalEmpresasActivas": 128,
  "totalEmpleadosGlobales": 24015
}
```

---------------------------------------------------------------------------
CONFIGURACIÓN DE RECARGOS DE EMPRESA (TENANT DELEGADO)
---------------------------------------------------------------------------

El SuperAdmin puede configurar los recargos y multas de cualquier empresa específica por su ID.

- **POST** `/api/v1/superadmin/empresas/{empresaId}/recargos` — Crear configuración de recargos
  - Request Body:
    ```json
    {
      "factorHoraExtraDiurna": 1.25,
      "factorHoraExtraNocturna": 1.75,
      "factorHoraDominicalFestiva": 2.00,
      "multaRetardoPorMinuto": 50.00
    }
    ```
  - Response (201 Created):
    ```json
    {
      "id": "e4f8d223-8cfd-4d7a-b921-995f543e414c",
      "factorHoraExtraDiurna": 1.25,
      "factorHoraExtraNocturna": 1.75,
      "factorHoraDominicalFestiva": 2.00,
      "multaRetardoPorMinuto": 50.00
    }
    ```

- **GET** `/api/v1/superadmin/empresas/{empresaId}/recargos` — Obtener configuración de recargos
  - Response (200 OK):
    ```json
    {
      "id": "e4f8d223-8cfd-4d7a-b921-995f543e414c",
      "factorHoraExtraDiurna": 1.25,
      "factorHoraExtraNocturna": 1.75,
      "factorHoraDominicalFestiva": 2.00,
      "multaRetardoPorMinuto": 50.00
    }
    ```

- **PUT** `/api/v1/superadmin/empresas/{empresaId}/recargos` — Actualizar configuración de recargos
  - Request Body:
    ```json
    {
      "factorHoraExtraDiurna": 1.30,
      "factorHoraExtraNocturna": 1.80,
      "factorHoraDominicalFestiva": 2.10,
      "multaRetardoPorMinuto": 60.00
    }
    ```
  - Response (200 OK):
    ```json
    {
      "id": "e4f8d223-8cfd-4d7a-b921-995f543e414c",
      "factorHoraExtraDiurna": 1.30,
      "factorHoraExtraNocturna": 1.80,
      "factorHoraDominicalFestiva": 2.10,
      "multaRetardoPorMinuto": 60.00
    }
    ```

- **DELETE** `/api/v1/superadmin/empresas/{empresaId}/recargos` — Eliminar configuración
  - Response (204 No Content) — Sin body.

---------------------------------------------------------------------------
CONFIGURACIÓN DE REGLAS DE HORARIO (TENANT DELEGADO)
---------------------------------------------------------------------------

El SuperAdmin puede configurar y gestionar las jornadas y reglas de horario para cualquier empresa.

- **POST** `/api/v1/superadmin/empresas/{empresaId}/reglas-horario` — Crear regla de horario
  - Request Body:
    ```json
    {
      "descripcion": "Jornada Administrativa",
      "horaEntradaOficial": "08:00:00",
      "horaSalidaOficial": "17:00:00",
      "minutosToleranciaRetardo": 15,
      "tiempoLimiteFaltaMinutos": 120
    }
    ```
  - Response (201 Created):
    ```json
    {
      "id": "77f3a2c1-d419-4a0b-93ff-d9c02ff43bc1",
      "empresaId": "0757d941-2038-4abc-a0e3-fd1eaffd4bf3",
      "descripcion": "Jornada Administrativa",
      "horaEntradaOficial": "08:00:00",
      "horaSalidaOficial": "17:00:00",
      "minutosToleranciaRetardo": 15,
      "tiempoLimiteFaltaMinutos": 120
    }
    ```

- **GET** `/api/v1/superadmin/empresas/{empresaId}/reglas-horario` — Listar reglas de horario
  - Response (200 OK): Array de reglas de horario.

- **GET** `/api/v1/superadmin/empresas/{empresaId}/reglas-horario/paginated` — Listar reglas de horario con paginación
  - Parámetros: `page` (int, default 0), `size` (int, default 10), `sort` (String, default "descripcion,asc").
  - Response (200 OK): Objeto `PageResponse` con las reglas.

- **GET** `/api/v1/superadmin/empresas/{empresaId}/reglas-horario/{id}` — Obtener regla por ID
  - Response (200 OK): Objeto de regla de horario.

- **PUT** `/api/v1/superadmin/empresas/{empresaId}/reglas-horario/{id}` — Actualizar regla de horario
  - Request Body:
    ```json
    {
      "descripcion": "Jornada Administrativa Modificada",
      "horaEntradaOficial": "07:30:00",
      "horaSalidaOficial": "16:30:00",
      "minutosToleranciaRetardo": 10,
      "tiempoLimiteFaltaMinutos": 90
    }
    ```
  - Response (200 OK): Regla actualizada.

- **DELETE** `/api/v1/superadmin/empresas/{empresaId}/reglas-horario/{id}` — Eliminar regla de horario
  - Response (204 No Content) — Sin body.

-------------------------------------------------------------------------------
3) PROTOCOLO DE ERRORES
-------------------------------------------------------------------------------

El backend sigue el estándar de error de Spring Boot (cuando no se intercepta por un handler personalizado). Estructura típica de error HTTP:

```json
{
  "timestamp": "2026-05-26T15:10:00.123+00:00",
  "status": 400,
  "error": "Bad Request",
  "message": "Validation failed: nombre no puede estar vacío",
  "path": "/api/v1/superadmin/empresas"
}
```

Casos comunes y códigos:
- 400 Bad Request: payload inválido o violación de validaciones (`@NotBlank`, `@Size`, etc.).
- 401 Unauthorized: faltó Authorization o token inválido/expirado.
- 403 Forbidden: token válido pero usuario no tiene `ROLE_SUPER_ADMIN`.
- 404 Not Found: recurso no encontrado (p. ej. GET/PUT/DELETE por id inexistente).
- 409 Conflict: recurso en conflicto (p. ej. NIT/RUT duplicado). Ejemplo:
```json
{
  "timestamp": "2026-05-26T15:05:00.123+00:00",
  "status": 409,
  "error": "Conflict",
  "message": "Ya existe una empresa con el NIT/RUT proporcionado: 901445882-3",
  "path": "/api/v1/superadmin/empresas"
}
```

Recomendaciones para el Frontend (IA React):
- Siempre enviar el header `Authorization: Bearer <JWT>` en todas las peticiones.
- Manejar 401 redirigiendo a la pantalla de login.
- Mostrar mensajes claros en 4xx basados en `message` devuelto.
- Para listados, implementar paginación basada en `page` (0-based) y `size`, y usar `totalElements`/`totalPages` para paginador.
- En crear empresa, si recibe 201 y el cuerpo, mostrar el registro creado. Si recibe 409, mostrar error localizado en campo `nitRut`.

-------------------------------------------------------------------------------
Anexos
-------------------------------------------------------------------------------

- Campos del DTO `EmpresaCreateRequest` (frontend debe enviar exactamente estos nombres y tipos JSON).
- Campo `estado` en patch debe ser "ACTIVO" o "SUSPENDIDO".
- Los ejemplos de fechas usan formato ISO-8601 con zona (OffsetDateTime).

----
Documento generado a partir del código del backend (controlador y DTOs). Si el backend cambia, actualizar este contrato inmediatamente.

