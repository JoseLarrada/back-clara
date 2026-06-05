# Guía de Integración para Frontend: Consola de Monitoreo GPS y Telemetría

Este documento detalla todos los endpoints, estructuras de datos (DTOs) y el comportamiento del WebSocket para la consola de monitoreo de geolocalización y seguridad en tiempo real.

---

## 1. Roles y Autenticación
* Todos los endpoints de administración RRHH requieren cabecera `Authorization: Bearer <TOKEN>` con rol `ADMIN_RRHH` o `ROLE_ADMIN_RRHH`.
* El endpoint de envío de coordenadas del empleado requiere cabecera `Authorization: Bearer <TOKEN>` con rol `EMPLEADO` o `ROLE_EMPLEADO`.

---

## 2. API REST: Endpoints de Geolocalización y Monitoreo

### 2.1. Obtener Monitoreo Completo de Empleados (Dashboard Principal)
Devuelve el estado de la jornada diaria (EN_JORNADA, FINALIZADA, ALMUERZO, NO_INICIADA) junto con la última geolocalización, coordenadas de geocerca asignada, métricas de distancia y conteo de anomalías del día actual.

* **Método:** `GET`
* **Ruta:** `/api/v1/admin/empleados/monitoreo`
* **Respuesta (200 OK):**
```json
[
  {
    "empleadoId": "a0081d4d-c5fb-419b-9a73-63bc1ce4a7b2",
    "empleadoNombre": "Carles Perez",
    "email": "carles.perez@empresa.com",
    "modalidad": "REMOTO",
    "jornadaEstado": "EN_JORNADA",
    "latitud": 11.71133850,
    "longitud": -72.26611700,
    "estadoConexion": "ACTIVO",
    "ultimaActualizacion": "2026-06-05T00:30:14-05:00",
    "precisionGps": 15.20,
    "velocidad": 0.50,
    "geocercaLatitud": 11.71130000,
    "geocercaLongitud": -72.26600000,
    "geocercaRadioMetros": 50,
    "geocercaDescripcion": "Casa / Home Office",
    "fueraDeGeocerca": false,
    "distanciaGeocercaMetros": 13.52,
    "anomaliasHoy": 0
  }
]
```

---

### 2.2. Obtener Últimas Ubicaciones (Solo Posiciones de Mapa)
Retorna la última ubicación de todos los empleados activos de la empresa para pintar los marcadores en el mapa general de telemetría de RRHH.

* **Método:** `GET`
* **Ruta:** `/api/v1/admin/empleados/ultimas-ubicaciones`
* **Respuesta (200 OK):**
```json
[
  {
    "empleadoId": "a0081d4d-c5fb-419b-9a73-63bc1ce4a7b2",
    "empleadoNombre": "Carles Perez",
    "latitud": 11.71133850,
    "longitud": -72.26611700,
    "precisionGps": 15.20,
    "velocidad": 0.50,
    "direccion": 180.00,
    "estadoConexion": "ACTIVO",
    "registradoEn": "2026-06-05T00:30:14-05:00",
    "fueraDeGeocerca": null
  }
]
```

---

### 2.3. Obtener Historial de Ruta (Recorrido Histórico)
Devuelve la lista ordenada cronológicamente de todas las coordenadas GPS registradas para un colaborador. 
* **Mejora Crítica:** Si el parámetro `fecha` no es provisto, o se envía como vacío `""`, `"null"` o `"undefined"`, **el backend retornará la totalidad del historial de ubicaciones** del empleado sin limitar la búsqueda al día actual.

* **Método:** `GET`
* **Ruta:** `/api/v1/admin/empleados/{empleadoId}/ruta`
* **Parámetros de Query:**
  * `fecha` (Opcional, String en formato `YYYY-MM-DD`). Ejemplo: `2026-06-01`.
* **Respuesta (200 OK):**
```json
[
  {
    "empleadoId": "a0081d4d-c5fb-419b-9a73-63bc1ce4a7b2",
    "empleadoNombre": "Carles Perez",
    "latitud": 11.38916597,
    "longitud": -72.23495458,
    "precisionGps": 209.00,
    "velocidad": 1.20,
    "direccion": 90.00,
    "estadoConexion": "ACTIVO",
    "registradoEn": "2026-06-01T04:11:19.412Z",
    "fueraDeGeocerca": null
  },
  {
    "empleadoId": "a0081d4d-c5fb-419b-9a73-63bc1ce4a7b2",
    "empleadoNombre": "Carles Perez",
    "latitud": 11.38916101,
    "longitud": -72.23500039,
    "precisionGps": 10.00,
    "velocidad": null,
    "direccion": null,
    "estadoConexion": "ACTIVO",
    "registradoEn": "2026-06-01T04:13:00.571Z",
    "fueraDeGeocerca": null
  }
]
```

---

### 2.4. Registrar Ubicación (Lado del Empleado)
Permite al dispositivo del empleado enviar coordenadas. Soporta envío en lote para sincronización diferida cuando se recupere la conexión offline.

* **Método:** `POST`
* **Ruta:** `/api/v1/empleado/panel/ubicaciones`
* **Cuerpo de la Petición (Request Body):**
```json
{
  "pings": [
    {
      "latitud": 11.38916597,
      "longitud": -72.23495458,
      "precisionGps": 10.00,
      "velocidad": 1.10,
      "direccion": 180.00,
      "registradoEn": "2026-06-05T00:35:00Z"
    }
  ]
}
```
* **Respuesta:** `200 OK` (vacío).

---

## 3. WebSockets (Rastreo en Tiempo Real)

El panel del administrador abre una conexión WebSocket para recibir las coordenadas de los empleados de forma caliente e instantánea sin necesidad de HTTP polling.

* **Ruta de Conexión:** `/ws/ubicaciones` (Ejemplo: `ws://localhost:8080/ws/ubicaciones` o `wss://dominio.com/ws/ubicaciones`)
* **Mensaje Recibido (JSON del canal):**
Cada vez que un empleado reporta una ubicación, el WebSocket distribuye un payload con la estructura de `UbicacionResponse`:
```json
{
  "empleadoId": "a0081d4d-c5fb-419b-9a73-63bc1ce4a7b2",
  "empleadoNombre": "Carles Perez",
  "latitud": 11.71133850,
  "longitud": -72.26611700,
  "precisionGps": 15.20,
  "velocidad": 0.50,
  "direccion": 180.00,
  "estadoConexion": "ACTIVO",
  "registradoEn": "2026-06-05T00:30:14Z",
  "fueraDeGeocerca": false
}
```

---

## 4. Estado de Conexión y Heartbeat
El backend cuenta con una tarea en segundo plano que corre cada 60 segundos evaluando el tiempo transcurrido desde el último ping registrado de cada empleado:
* **ACTIVO:** Si envió coordenadas en los últimos 2 minutos.
* **INACTIVO:** Si transcurrieron entre 2 y 5 minutos sin reportes GPS.
* **DESCONECTADO:** Si pasaron más de 5 minutos sin señales GPS.

Cualquier cambio de estado (`INACTIVO` / `DESCONECTADO`) se notifica inmediatamente a través del canal de WebSockets de los administradores con el campo `"estadoConexion"` actualizado para actualizar los marcadores dinámicamente.
