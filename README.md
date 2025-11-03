# 📚 Guía de Inicio y Autenticación del Backend

Este documento proporciona los pasos necesarios para levantar el entorno de microservicios mediante Docker Compose y obtener un `access_token` válido.

## 1. 🚀 Inicio del Entorno (Docker Compose)

Asegúrate de estar en el directorio raíz donde se encuentra el archivo `docker-compose.yml`.

### A. Construir y Levantar los Contenedores

Ejecuta los siguientes comandos para construir las imágenes y levantar todos los servicios en modo _detached_ (`-d`).

- **Construir las imágenes:**

```bash
docker compose build
```

- **Iniciar los servicios:**

<!-- end list -->

```bash
docker compose up -d
```

---

## 2\. 🌐 Acceso a los Servicios Web

Una vez que los contenedores estén levantados, puedes acceder a las interfaces de gestión:

| Servicio                            | URL de Acceso                                       | Credenciales de Acceso (Iniciales)                         |
| :---------------------------------- | :-------------------------------------------------- | :--------------------------------------------------------- |
| **Keycloak** (Autenticación)        | `http://localhost:8180/admin/master/console/`       | **Usuario:** `admin` / **Contraseña:** `admin123`          |
| **Keycloak Realm TPI**              | `http://localhost:8180/admin/tpi-backend/console/`  | Usar usuarios creados (ver tabla abajo)                    |
| **PgAdmin** (Gestión de DB)         | `http://localhost:5050/`                            | **Email:** `admin@admin.com` / **Contraseña:** `admin123`  |
| **API Gateway** (Punto de Entrada)  | `http://localhost:8080/`                            | Requiere autenticación JWT                                 |
| **Servicio Recursos**               | `http://localhost:8082/`                            | Requiere autenticación JWT                                 |
| **Servicio Solicitudes**            | `http://localhost:8083/`                            | Requiere autenticación JWT                                 |
| **Servicio Logística**              | `http://localhost:8084/`                            | Requiere autenticación JWT                                 |

---

## 3\. 👥 Usuarios Pre-configurados

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

## 4\. 🗝️ Obtener un `access_token` a través del Gateway (RECOMENDADO)

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

## 5\. 🛠️ Conexión a la Base de Datos (pgAdmin)

Para administrar la base de datos PostgreSQL, accede a pgAdmin (`http://localhost:5050/`) e introduce los siguientes parámetros de conexión:

| Campo de Conexión                | Valor                                                   | Descripción                                                                                                               |
| :------------------------------- | :------------------------------------------------------ | :------------------------------------------------------------------------------------------------------------------------ |
| **Server Name**                  | `tp-backend`                                            | Nombre descriptivo del servidor.                                                                                          |
| **Nombre/Dirección de servidor** | `postgres`                                              | **Nombre del servicio de Docker Compose** (no `localhost` ni la IP externa, ya que pgAdmin está dentro de la red Docker). |
| **Puerto**                       | `5432`                                                  | Puerto interno predeterminado de PostgreSQL.                                                                              |
| **Base de Datos**                | `mydatabase`                                            | Nombre de la base de datos.                                                                                               |
| **Usuario**                      | `myuser`                                                | Usuario de la base de datos.                                                                                              |
| **Contraseña**                   | _La contraseña configurada en el `docker-compose.yml`._ | (Generalmente se pide en una pestaña separada).                                                                           |

> ℹ️ **Nota:** Si tu instancia de pgAdmin estuviera corriendo fuera de Docker, la dirección del servidor debería ser `localhost` o `127.0.0.1` (o la IP del host) y el puerto sería el mapeado externamente (ej: `5432`). Pero como pgAdmin está en el mismo `docker-compose.yml`, usa el **nombre del servicio: `postgres`**.

---

## 6\. 🔐 Usar el `access_token`

Una vez obtenido el token, úsalo en la cabecera **`Authorization`** para acceder a los microservicios a través del Gateway:

```bash
# Ejemplo de acceso a un endpoint protegido
curl -H "Authorization: Bearer <access_token>" http://localhost:8080/api/recursos/camiones
```

| Cabecera           | Valor                             |
| :----------------- | :-------------------------------- |
| **Authorization**  | `Bearer <access_token_extraido>`  |

---

## 7\. 📊 Estructura de Microservicios

| Servicio         | Puerto Interno | Puerto Externo | Descripción                           |
| :--------------- | :------------- | :------------- | :------------------------------------ |
| **Gateway**      | 8080           | 8080           | API Gateway - Punto de entrada único  |
| **Recursos**     | 8081           | 8082           | Gestión de camiones y contenedores    |
| **Solicitudes**  | 8082           | 8083           | Gestión de solicitudes de transporte  |
| **Logística**    | 8083           | 8084           | Planificación de rutas y logística    |

---

## 8\. ⚙️ Comandos Útiles de Docker Compose

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

## 9\. 🔍 Verificación y Debugging

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

## 10\. ❌ Solución de Problemas

### Error 401 en endpoints:

- **Causa:** Token inválido, expirado o faltante
- **Solución:** Obtener un nuevo token válido (Sección 4).

### Error de conexión a Keycloak:

- **Causa:** Keycloak no está completamente iniciado
- **Solución:** Esperar 30-60 segundos y reintentar

### Usuario/contraseña incorrectos:

- **Causa:** Credenciales erróneas
- **Solución:** Usar las credenciales de la tabla de usuarios (Sección 3).

### Error "Realm does not exist":

- **Causa:** Realm incorrecto en la URL
- **Solución:** Usar `tpi-backend` (no `tpi-backend-app`)

---

**✅ El sistema está configurado correctamente.** El error 401 en los endpoints es normal e indica que la autenticación está funcionando. Obtén un token siguiendo los pasos anteriores para acceder a las APIs.

```eof

```
