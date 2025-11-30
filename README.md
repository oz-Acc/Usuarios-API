# Usuarios API

API RESTful para gestión de usuarios con JWT, validaciones y persistencia en H2.

Esta aplicación expone endpoints para crear, leer, actualizar y eliminar usuarios (GET, POST, PUT, PATCH, DELETE).

---

# Características principales

- CRUD completo para `Usuario`
- Autenticación JWT
- Validaciones:
  - `correo` validado por anotación `@Email`. Ejemplo de formato `aaaaaaa@dominio.cl`.
  - `contrasena` validada por regex configurable (`security.password.regex` en `application.yml`).
- Todos los endpoints aceptan y retornan JSON únicamente (incluyendo errores).
- Error format estándar: `{ "mensaje": "texto" }`.
- H2 in-memory DB (script de creación `schema.sql`, script de carga de data de prueba `data.sql`).
- Swagger disponible.

---

# Endpoints principales

Base: `http://localhost:8080`

- POST /api/usuarios
  - Crea un usuario.
  - No requiere JWT.

- POST /auth/login
  - Autentica usuario.
  - No requiere JWT.

- GET /api/usuarios
  - Obtiene lista de usuarios.
  - Requiere JWT.

- GET /api/usuarios/{id}
  - Obtiene usuario por id.
  - Requiere JWT.

- PUT /api/usuarios/{id}
  - Reemplaza usuario completo.
  - Requiere JWT.

- PATCH /api/usuarios/{id}
  - Actualización parcial.
  - Requiere JWT.

- DELETE /api/usuarios/{id}
  - Borra usuario.
  - Requiere JWT.

> Todos los errores retornan JSON con `{"mensaje": "texto de error"}`.

---

# Ejecución local

1. Compilar y ejecutar tests:

```powershell
.\mvnw.cmd clean install
```

2. Ejecutar la aplicación:

```powershell
.\mvnw.cmd spring-boot:run
```

---

Por defecto la aplicación levantará en `http://localhost:8080`.

# Cómo probar

## Crear usuario

```bash
curl -X POST http://localhost:8080/api/usuarios \
  -H 'Content-Type: application/json' \
  -d '{"nombre":"Juan Rodriguez","correo":"juan@rodriguez.org","contrasena":"Password1!","telefonos":[{"numero":"1234567","codigoCiudad":"1","codigoPais":"57"}]}'
```

Respuesta (ejemplo):

```json
{
  "id": "...",
  "nombre": "Juan Rodriguez",
  "correo": "juan@rodriguez.org",
  "creado": "2025-11-28T12:00:00",
  "modificado": "2025-11-28T12:00:00",
  "ultimoLogin": "2025-11-28T12:00:00",
  "token": "<JWT>",
  "activo": true,
  "telefonos": [ ... ]
}
```

## Hacer Login

```bash
curl -X POST http://localhost:8080/auth/login \
  -H 'Content-Type: application/json' \
  -d '{"correo":"juan@rodriguez.org","contrasena":"Password1!"}'
```

Respuesta:
```json
{ "token": "<JWT>" }
```

## Consumir endpoint protegido

```bash
curl -H "Authorization: Bearer <JWT>" http://localhost:8080/api/usuarios
```

---

# Swagger / OpenAPI

- UI disponible en:
  - `/swagger-ui.html`
  - Documentación OpenAPI: `/v3/api-docs`

---

# Uso de Swagger UI con autenticación JWT

Si deseas probar endpoints protegidos:

1. Crea un usuario usando el endpoint `POST /api/usuarios` directamente desde Swagger.
2. Ejecuta `POST /auth/login` con las credenciales del usuario creado. Copia el token JWT de la respuesta.
3. Haz clic en el botón **Authorize** (candado verde) en la parte superior derecha de Swagger UI.
4. En el campo Value ingresa tu token jwt.
5. Haz clic en **Authorize** y luego **Close**.
6. Ahora puedes ejecutar endpoints como `GET /api/usuarios`, `PUT /api/usuarios/{id}`, etc. usando el token configurado.

---

# Caching

- Se habilita cache en memoria con Caffeine para acelerar lecturas frecuentes.
- Cachés configurados: `usuarios` (lista), `usuario-by-id`, `usuario-by-email`.

---

# Monitoreo y Métricas (Actuator + Prometheus)

- Endpoints Actuator expuestos: `/actuator/health`, `/actuator/metrics`, `/actuator/prometheus`.
- Integración con Micrometer Prometheus.

Métricas añadidas:
- `auth.login.attempts`: intentos de login totales.
- `auth.login.failures`: intentos fallidos.
- `rate.limit.hits`: peticiones bloqueadas por rate limiting.
- `password.validation.failures`: errores de validación de contraseña.
- `usuarios.created`: usuarios creados exitosamente.

Consulta de métricas:
```bash
curl http://localhost:8080/actuator/metrics
curl http://localhost:8080/actuator/metrics/auth.login.attempts
curl http://localhost:8080/actuator/prometheus
```
