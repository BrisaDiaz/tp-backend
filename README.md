Perfecto! Veo que los microservicios están configurados correctamente con `tpi-backend`. El problema era solo en el README. Aquí está el README actualizado:

# 📚 Guía de Inicio y Autenticación del Backend

Este documento proporciona los pasos necesarios para levantar el entorno de microservicios mediante Docker Compose y obtener un `access_token` válido desde Keycloak.

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

| Servicio                           | URL de Acceso                                      | Credenciales de Acceso (Iniciales)                        |
| :--------------------------------- | :------------------------------------------------- | :-------------------------------------------------------- |
| **Keycloak** (Autenticación)       | `http://localhost:8180/admin/master/console/`      | **Usuario:** `admin` / **Contraseña:** `admin123`         |
| **Keycloak Realm TPI**             | `http://localhost:8180/admin/tpi-backend/console/` | Usar usuarios creados (ver tabla abajo)                   |
| **PgAdmin** (Gestión de DB)        | `http://localhost:5050/`                           | **Email:** `admin@admin.com` / **Contraseña:** `admin123` |
| **API Gateway** (Punto de Entrada) | `http://localhost:8080/`                           | Requiere autenticación JWT                                |
| **Servicio Recursos**              | `http://localhost:8082/`                           | Requiere autenticación JWT                                |
| **Servicio Solicitudes**           | `http://localhost:8083/`                           | Requiere autenticación JWT                                |
| **Servicio Logística**             | `http://localhost:8084/`                           | Requiere autenticación JWT                                |

---

## 3. 👥 Usuarios Pre-configurados

**Realm:** `tpi-backend`

| Usuario             | Email                         | Contraseña | Rol             | Descripción               |
| :------------------ | :---------------------------- | :--------- | :-------------- | :------------------------ |
| **admin01**         | `admin01@example.com`         | `Clave123` | `admin`         | Administrador del sistema |
| **admin02**         | `admin02@example.com`         | `Clave123` | `admin`         | Administrador del sistema |
| **cliente01**       | `cliente01@example.com`       | `Clave123` | `cliente`       | Usuario cliente           |
| **cliente02**       | `cliente02@example.com`       | `Clave123` | `cliente`       | Usuario cliente           |
| **transportista01** | `transportista01@example.com` | `Clave123` | `transportista` | Usuario transportista     |
| **transportista02** | `transportista02@example.com` | `Clave123` | `transportista` | Usuario transportista     |

---

## 4. 🗝️ Obtener un `access_token`

Para acceder a las APIs protegidas, necesitas obtener un `access_token` de Keycloak.

### Método 1: Flujo Directo (Password Grant) - RECOMENDADO

```bash
curl -X POST http://localhost:8180/realms/tpi-backend/protocol/openid-connect/token \
  -H "Content-Type: application/x-www-form-urlencoded" \
  -d "username=cliente01&password=Clave123&grant_type=password&client_id=tpi-backend-client"
```

**Ejemplo de Respuesta:**

```json
{
  "access_token": "eyJhbGciOiJSUzI1NiIsInR5cCIgOiAiSldUIiwi...",
  "expires_in": 300,
  "refresh_token": "eyJhbGciOiJIUzI1NiIsInR5cCIgOiAiSldUIiw...",
  "token_type": "Bearer",
  "scope": "openid profile email"
}
```

### Método 2: Flujo de Código de Autorización

#### Paso 1: Obtener Código

Navega a:

```
http://localhost:8180/realms/tpi-backend/protocol/openid-connect/auth?client_id=tpi-backend-client&response_type=code&redirect_uri=http://localhost:8080/login/oauth2/code/keycloak
```

#### Paso 2: Intercambiar Código por Token

```http
POST http://localhost:8180/realms/tpi-backend/protocol/openid-connect/token
Content-Type: application/x-www-form-urlencoded

grant_type=authorization_code
code=<el_code_recibido>
client_id=tpi-backend-client
redirect_uri=http://localhost:8080/login/oauth2/code/keycloak
```

---

## 5. 🔐 Usar el `access_token`

Una vez obtenido el token, úsalo en la cabecera **`Authorization`**:

```bash
curl -H "Authorization: Bearer <access_token>" http://localhost:8080/api/recursos/camiones
```

| Cabecera          | Valor                            |
| :---------------- | :------------------------------- |
| **Authorization** | `Bearer <access_token_extraido>` |

---

## 6. 📊 Estructura de Microservicios

| Servicio        | Puerto Interno | Puerto Externo | Descripción                          |
| :-------------- | :------------- | :------------- | :----------------------------------- |
| **Gateway**     | 8080           | 8080           | API Gateway - Punto de entrada único |
| **Recursos**    | 8081           | 8082           | Gestión de camiones y contenedores   |
| **Solicitudes** | 8082           | 8083           | Gestión de solicitudes de transporte |
| **Logística**   | 8083           | 8084           | Planificación de rutas y logística   |

---

## 7. 🛠️ Comandos Útiles

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

## 8. 🔍 Verificación del Sistema

### Probar servicios individualmente (sin Gateway):

```bash
# Recursos
curl -H "Authorization: Bearer <token>" http://localhost:8082/actuator/health

# Solicitudes
curl -H "Authorization: Bearer <token>" http://localhost:8083/actuator/health

# Logística
curl -H "Authorization: Bearer <token>" http://localhost:8084/actuator/health
```

### Verificar base de datos:

```bash
# Conectar a PostgreSQL
docker exec -it postgres psql -U myuser -d mydatabase
```

---

## 9. ❌ Solución de Problemas

### Error 401 en endpoints:

- **Causa:** Token inválido, expirado o faltante
- **Solución:** Obtener un nuevo token válido

### Error de conexión a Keycloak:

- **Causa:** Keycloak no está completamente iniciado
- **Solución:** Esperar 30-60 segundos y reintentar

### Usuario/contraseña incorrectos:

- **Causa:** Credenciales erróneas
- **Solución:** Usar las credenciales de la tabla de usuarios

### Error "Realm does not exist":

- **Causa:** Realm incorrecto en la URL
- **Solución:** Usar `tpi-backend` (no `tpi-backend-app`)

---

**✅ El sistema está configurado correctamente.** El error 401 en los endpoints es normal e indica que la autenticación está funcionando. Obtén un token siguiendo los pasos anteriores para acceder a las APIs.
