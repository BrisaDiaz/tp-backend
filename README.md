# 📚 Guía de Inicio y Autenticación del Backend

Este documento proporciona los pasos necesarios para levantar el entorno de microservicios mediante Docker Compose y obtener un `access_token` válido.

## 📋 Contenido Adicional

Además de esta guía, el proyecto incluye:

- **📖 Documentación con Swagger**: Accede a la documentación interactiva de las APIs en `http://localhost:8080/swagger-ui.html`
- **🎯 Datos Mock**: Archivo de datos de prueba en `./mocks/datos.json` para testing y desarrollo
- **🗄️ Script de Base de Datos**: Archivo `./data-seed.sql` con datos iniciales para insertar en la BD mediante pgAdmin

## 1. 🚀 Inicio del Entorno (Docker Compose)

Asegúrate de estar en el directorio raíz donde se encuentra el archivo `docker-compose.yml`.

### A. Construir y Levantar los Contenedores

Ejecuta los siguientes comandos para construir las imágenes y levantar todos los servicios en modo _detached_ (`-d`).

- **Construir las imágenes:**

```bash
docker compose build
```

- **Iniciar los servicios:**

```bash
docker compose up -d
```

---

## 2. 🌐 Acceso a los Servicios Web

Una vez que los contenedores estén levantados, puedes acceder a las interfaces de gestión:

| Servicio                            | URL de Acceso                                       | Credenciales de Acceso (Iniciales)                         |
| :---------------------------------- | :-------------------------------------------------- | :--------------------------------------------------------- |
| **Keycloak** (Autenticación)        | `http://localhost:8180/admin/master/console/`       | **Usuario:** `admin` / **Contraseña:** `admin123`          |
| **Keycloak Realm TPI**              | `http://localhost:8180/admin/tpi-backend/console/`  | Usar usuarios creados (ver tabla abajo)                    |
| **PgAdmin** (Gestión de DB)         | `http://localhost:5050/`                            | **Email:** `admin@admin.com` / **Contraseña:** `admin123`  |
| **API Gateway** (Punto de Entrada)  | `http://localhost:8080/`                            | Requiere autenticación JWT                                 |
| **Swagger UI** (Documentación API)  | `http://localhost:8080/swagger-ui.html`             | Documentación interactiva de todos los microservicios      |
| **Servicio Recursos**               | `http://localhost:8082/`                            | Requiere autenticación JWT                                 |
| **Servicio Solicitudes**            | `http://localhost:8083/`                            | Requiere autenticación JWT                                 |
| **Servicio Logística**              | `http://localhost:8084/`                            | Requiere autenticación JWT                                 |

---

## 3. 📖 Documentación con Swagger

El proyecto incluye documentación interactiva de las APIs mediante Swagger UI:

### Acceso a Swagger

- **URL Principal**: `http://localhost:8080/swagger-ui.html`
- **Configuración**: `http://localhost:8080/v3/api-docs/swagger-config`

### Servicios Documentados

- **Logística Service**: Gestión de rutas, tramos y camiones
- **Solicitudes Service**: Gestión de solicitudes de transporte y clientes
- **Recursos Service**: Gestión de tarifas, combustibles y parámetros

### Características

- Documentación interactiva en tiempo real
- Posibilidad de hacer pruebas directamente desde la interfaz
- Autenticación integrada con JWT
- Descarga de especificaciones OpenAPI

---

## 4. 🎯 Datos Mock y Scripts

### Datos Mock (`./mocks/datos.json`)

Archivo JSON con datos de ejemplo para testing que incluye:

- Ejemplos de solicitudes de transporte
- Datos de clientes, camiones y depósitos
- Estructuras completas para flujos de trabajo
- IDs de referencia para desarrollo

### Script de Base de Datos (`./data-seed.sql`)

Script SQL completo con:

- Inserción de ciudades y depósitos
- Configuración de precios de combustible y cargos de gestión
- Registro de camiones con transportistas
- Clientes pre-configurados
- Estados del sistema
- Solicitudes de transporte de ejemplo
- Contenedores y históricos de estado

---

## 5. 👥 Usuarios Pre-configurados

**Realm:** `tpi-backend`

| Usuario             | Email                         | Contraseña | Rol             | Descripción               |
| :------------------ | :---------------------------- | :--------- | :-------------- | :------------------------ |
| **admin01**         | `admin01@example.com`         | `Clave123` | `admin`         | Administrador del sistema |
| **admin02**         | `admin02@example.com`         | `Clave123` | `admin`         | Administrador del sistema |
| **cliente01**       | `cliente01@example.com`       | `Clave123` | `cliente`       | Usuario cliente           |
| **cliente02**       | `cliente02@example.com`       | `Clave123` | `cliente`       | Usuario cliente           |
| **transportista01** | `transportista01@example.com` | `Clave123` | `transportista` | Usuario transportista     |
| **transportista02** | `transportista02@example.com` | `Clave123` | `transportista` | Usuario transportista     |

---

## 6. 🗝️ Obtener un `access_token` a través del Gateway (RECOMENDADO)

Para acceder a las APIs protegidas, obtén un `access_token` a través del endpoint proxy del **API Gateway**. El Gateway se encarga de realizar el intercambio de credenciales con Keycloak.

### Método Único: Flujo Simplificado (Password Grant) a través del GATEWAY

Utiliza el endpoint `/auth/token` del Gateway enviando solo el nombre de usuario y la contraseña en el cuerpo.

```bash
# Ejemplo de solicitud usando el usuario 'admin01'
curl -X POST 'http://localhost:8080/auth/token' \
  -H 'Content-Type: application/x-www-form-urlencoded' \
  -d 'username=admin01&password=Clave123'
```

**Ejemplo de Respuesta (JSON):**

```json
{
  "access_token": "eyJhbGciOiJSUzI1NiIsInR5cCIgOiAiSldUIiwi...",
  "expires_in": 300,
  "refresh_token": "eyJhbGciOiJIUzI1NiIsInR5cCIgOiAiSldUIiw...",
  "token_type": "Bearer",
  "scope": "openid profile email"
}
```

---

## 7. 🗄️ Conexión a la Base de Datos (pgAdmin)

Para administrar la base de datos PostgreSQL, accede a pgAdmin (`http://localhost:5050/`) e introduce los siguientes parámetros de conexión:

### Configuración de Conexión

| Campo de Conexión                | Valor        | Descripción                                                                                                               |
| :------------------------------- | :----------- | :------------------------------------------------------------------------------------------------------------------------ |
| **Server Name**                  | `tp-backend` | Nombre descriptivo del servidor.                                                                                          |
| **Nombre/Dirección de servidor** | `postgres`   | **Nombre del servicio de Docker Compose** (no `localhost` ni la IP externa, ya que pgAdmin está dentro de la red Docker). |
| **Puerto**                       | `5432`       | Puerto interno predeterminado de PostgreSQL.                                                                              |
| **Base de Datos**                | `mydatabase` | Nombre de la base de datos.                                                                                               |
| **Usuario**                      | `myuser`     | Usuario de la base de datos.                                                                                              |
| **Contraseña**                   | `secret`     | Contraseña de la base de datos.                                                                                           |

### Ejecutar Script de Datos Iniciales

Una vez conectado a pgAdmin:

1. Navega hasta la base de datos `mydatabase`
2. Haz clic derecho y selecciona **Query Tool**
3. Copia y pega el contenido del archivo `./data-seed.sql`
4. Ejecuta el script (F5 o botón Execute)

> ℹ️ **Nota:** El script `data-seed.sql` incluye todos los datos iniciales necesarios para que el sistema funcione correctamente.

---

## 8. 🔐 Usar el `access_token`

Una vez obtenido el token, úsalo en la cabecera **`Authorization`** para acceder a los microservicios a través del Gateway:

```bash
# Ejemplo de acceso a un endpoint protegido
curl -H "Authorization: Bearer <access_token>" http://localhost:8080/api/recursos/camiones
```

| Cabecera           | Valor                             |
| :----------------- | :-------------------------------- |
| **Authorization**  | `Bearer <access_token_extraido>`  |

---

## 9. 📊 Estructura de Microservicios

| Servicio         | Puerto Interno | Puerto Externo | Descripción                           |
| :--------------- | :------------- | :------------- | :------------------------------------ |
| **Gateway**      | 8080           | 8080           | API Gateway - Punto de entrada único  |
| **Recursos**     | 8081           | 8082           | Gestión de camiones y contenedores    |
| **Solicitudes**  | 8082           | 8083           | Gestión de solicitudes de transporte  |
| **Logística**    | 8083           | 8084           | Planificación de rutas y logística    |

---

## 10. ⚙️ Comandos Útiles de Docker Compose

### Ver estado de los contenedores:

```bash
docker compose ps
```

### Ver logs de servicios:

```bash
docker compose logs gateway
docker compose logs keycloak
docker compose logs recursos
```

### Reiniciar servicios:

```bash
docker compose restart gateway
```

### Detener todos los servicios:

```bash
docker compose down
```

---

## 11. 🔍 Verificación y Debugging

### Probar servicios individualmente (sin Gateway):

```bash
# Recursos
curl -H "Authorization: Bearer <token>" http://localhost:8082/actuator/health

# Solicitudes
curl -H "Authorization: Bearer <token>" http://localhost:8083/actuator/health

# Logística
curl -H "Authorization: Bearer <token>" http://localhost:8084/actuator/health
```

### Verificar base de datos (por consola):

```bash
# Conectar a PostgreSQL
docker exec -it postgres psql -U myuser -d mydatabase
```

---

## 12. ❌ Solución de Problemas

### Error 401 en endpoints:

- **Causa:** Token inválido, expirado o faltante
- **Solución:** Obtener un nuevo token válido (Sección 6).

### Error de conexión a Keycloak:

- **Causa:** Keycloak no está completamente iniciado
- **Solución:** Esperar 30-60 segundos y reintentar

### Usuario/contraseña incorrectos:

- **Causa:** Credenciales erróneas
- **Solución:** Usar las credenciales de la tabla de usuarios (Sección 5).

### Error "Realm does not exist":

- **Causa:** Realm incorrecto en la URL
- **Solución:** Usar `tpi-backend` (no `tpi-backend-app`)

### Problemas con la base de datos:

- **Causa:** Datos iniciales no cargados
- **Solución:** Ejecutar el script `./data-seed.sql` en pgAdmin (Sección 7)
