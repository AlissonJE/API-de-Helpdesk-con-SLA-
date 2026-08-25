# API de Mesa de Ayuda (Helpdesk) con SLA — Spring Boot 3 + JWT

Implementación completa del taller: autenticación con JWT (access + refresh token
persistido en BD), RBAC por rol, cálculo automático de SLA y reto opcional
(paginación, historial de estados, estadísticas).

## Cómo ejecutar

Requisitos: JDK 17+, Maven 3.9+ (o el wrapper `mvnw` si lo agregas con
`mvn -N wrapper:wrapper`).

```bash
mvn spring-boot:run
```

La API queda disponible en `http://localhost:8080`.
Consola H2 (opcional, para depurar): `http://localhost:8080/h2-console`
(JDBC URL: `jdbc:h2:mem:helpdeskdb`, usuario `sa`, sin contraseña).

## Estrategia de refresh token elegida

**Opción A — persistido en base de datos**, con **rotación**: cada vez que se
usa `/api/auth/refresh`, el refresh token usado se marca `revocado = true` y se
emite uno nuevo. Esto permite revocación real en logout y limita el daño si un
refresh token se filtra (solo sirve una vez).

## Cómo crear el primer ADMIN

El registro público (`/api/auth/registro`) siempre crea usuarios con rol
`USUARIO` (regla de negocio del enunciado). Para tener un ADMIN inicial, la
forma más simple es:
1. Registrar un usuario normal.
2. Cambiarle el rol manualmente vía consola H2:
   `UPDATE usuarios SET rol='ADMIN' WHERE email='tu_correo@ejemplo.com';`
3. Desde ese ADMIN, usar `POST /api/admin/soporte` para ascender a otros a SOPORTE.

## Estructura del proyecto

```
com.helpdesk
├── DataBaseConnection.java   (verifica la conexion a la BD al arrancar)
├── HelpdeskApiApplication.java
├── config/                   (SecurityConfig, JwtAuthenticationFilter, ApplicationConfig)
├── controllers/               (AuthController, TicketController, AdminController, PingController)
├── dto/                       (requests/responses)
├── entities/                  (Usuario, Ticket, RefreshToken, TicketHistorial)
├── enums/                     (Rol, Prioridad, Estado)
├── exceptions/                (excepciones personalizadas + GlobalExceptionHandler)
├── repositories/              (interfaces Spring Data JPA)
├── security/                  (JwtService)
└── services/                  (AuthService, TicketService)
```

`config/` y `security/` no estaban en la plantilla de referencia, pero son necesarios aquí porque el taller pide JWT + refresh token; en un CRUD simple sin seguridad no harían falta.

## Mapeo con la rúbrica de evaluación

| Criterio | Peso | Dónde está implementado |
|---|---|---|
| API funcional y modelo de datos correcto | 15% | `model/`, `repository/` — entidades `Usuario`, `Ticket`, `RefreshToken` según el modelo pedido |
| Autenticación con JWT (registro/login) | 15% | `AuthController`, `AuthService`, `JwtService`, contraseñas con BCrypt |
| Estrategia de refresh token (emisión, renovación, revocación) | 20% | `AuthService.emitirRefreshToken / refrescar / logout`, entidad `RefreshToken` |
| Protección de rutas por autenticación | 10% | `SecurityConfig` (stateless, `JwtAuthenticationFilter`), rutas públicas explícitas |
| Autorización por rol (RBAC) | 20% | `@PreAuthorize("hasAnyRole(...)")` en `TicketController`/`AdminController`; regla "usuario no ve tickets de otros" en `TicketService.obtenerPorId` |
| Cálculo correcto del SLA y tickets vencidos | 10% | `Prioridad` (horas por prioridad), `TicketService.crear` (slaVenceEn = creadoEn + horas), `Ticket.isVencido()`, `GET /api/tickets/vencidos` |
| Validaciones, códigos HTTP y buenas prácticas | 10% | Bean Validation en DTOs + `GlobalExceptionHandler` (400/401/403/404/409) |

## Endpoints (igual a la especificación)

**Públicos:** `POST /api/auth/registro`, `POST /api/auth/login`, `POST /api/auth/refresh`, `GET /api/ping`

**Autenticados (cualquier rol):** `POST /api/auth/logout`, `POST /api/tickets`, `GET /api/tickets/mios`, `GET /api/tickets/{id}` (dueño o SOPORTE/ADMIN)

**Por rol SOPORTE/ADMIN:** `GET /api/tickets` (paginado), `PATCH /api/tickets/{id}/estado`, `GET /api/tickets/vencidos`

**Por rol ADMIN:** `POST /api/admin/soporte`

## Reto opcional implementado

- **Paginación:** `GET /api/tickets?page=0&size=10&sort=creadoEn,desc` (Spring `Pageable`, ya soportado por `TicketRepository.findAll(Pageable)`).
- **Historial de cambios de estado:** entidad `TicketHistorial`, se registra automáticamente en `TicketService.cambiarEstado`; consultable en `GET /api/tickets/{id}/historial`.
- **Estadísticas (solo ADMIN):** `GET /api/admin/estadisticas` → cantidad de tickets por estado y % de cumplimiento de SLA.

## Notas de diseño

- El JWT de acceso incluye `sub` (email) y el claim `rol`, como pide el punto 8.2.
- El SLA y el estado inicial (`ABIERTO`) siempre los calcula el servidor; el DTO de entrada (`TicketRequest`) no tiene esos campos, así que el cliente no puede enviarlos.
- `prioridad` y `estado` inválidos (fuera del enum) devuelven 400 automáticamente porque Jackson falla al deserializar un enum desconocido, capturado en `GlobalExceptionHandler.handleNoLegible`.
