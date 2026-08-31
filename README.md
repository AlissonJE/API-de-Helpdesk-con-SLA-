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

