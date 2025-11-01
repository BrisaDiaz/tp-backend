# 📚 Guía de Inicio y Autenticación del Backend

Este documento proporciona los pasos necesarios para levantar el entorno de microservicios mediante Docker Compose y obtener un `access_token` válido desde Keycloak.

## 1\. 🚀 Inicio del Entorno (Docker Compose)

Asegúrate de estar en el directorio raíz donde se encuentra el archivo `docker-compose.yml`.

### A. Construir y Levantar los Contenedores

Ejecuta los siguientes comandos para construir las imágenes y levantar todos los servicios en modo _detached_ (`-d`).

- **Construir las imágenes (con limpieza de caché):**
  ```
  export COMPOSE_BAKE=false
  docker compose build --no-cache
  ```
- **Iniciar los servicios:**
  ```bash
  docker compose up -d
  ```

---

## 2\. 🌐 Acceso a los Servicios Web

Una vez que los contenedores estén levantados, puedes acceder a las interfaces de gestión:

| Servicio                           | URL de Acceso            | Credenciales de Acceso (Iniciales)                        |
| :--------------------------------- | :----------------------- | :-------------------------------------------------------- |
| **Keycloak** (Autenticación)       | `http://localhost:8180/` | **Usuario:** `admin` / **Contraseña:** `admin123`         |
| **PgAdmin** (Gestión de DB)        | `http://localhost:5050/` | **Email:** `admin@admin.com` / **Contraseña:** `admin123` |
| **API Gateway** (Punto de Entrada) | `http://localhost:8080/` | N/A                                                       |

---

## 3\. 🗝️ Obtener un `access_token` (Flujo de Código de Autorización)

Para acceder a las APIs protegidas por el **API Gateway**, necesitas obtener un `access_token` de Keycloak. Aquí se utiliza el flujo de **Código de Autorización** (`authorization_code`).

### Paso 1: Obtener el Código de Autorización (`code`)

Navega a esta URL en tu navegador. Esto inicia el flujo de autenticación de Keycloak.

```
http://localhost:8180/realms/tpi-backend/protocol/openid-connect/auth?client_id=tpi-backend-client&response_type=code&redirect_uri=http://localhost:8080/api/login/oauth2/code/keycloak
```

1.  **Inicia sesión** con un usuario válido (ejemplo):

    - **Usuario:** `cliente01`
    - **Contraseña:** `clave123`

2.  Tras la autenticación exitosa, Keycloak te redirigirá a la `redirect_uri` especificada. **Esta redirección fallará** (es lo esperado, ya que no estamos ejecutando la aplicación de cliente completa), pero la URL contendrá el parámetro `code`.

    **Ejemplo de URL de redirección:**

    ```
    http://localhost:8080/api/login/oauth2/code/keycloak?session_state=...&code=0f716011-c34e-4c3b-a5e2-e18818dabeb2.c48f91b3-04d5-4e2f-9225-e2c64d45afd8.02960d42-e205-4c8e-ad42-6f765b909aa1
    ```

3.  **Extrae el valor completo del parámetro `code`** de la URL.

    - **Code Extraído:** `0f716011-c34e-4c3b-a5e2-e18818dabeb2.c48f91b3-04d5-4e2f-9225-e2c64d45afd8.02960d42-e205-4c8e-ad42-6f765b909aa1`

### Paso 2: Intercambiar el Código por el Token

Utiliza el `code` extraído en el paso anterior para realizar una petición **POST** al _Token Endpoint_.

Realiza la siguiente petición (usando herramientas como Postman, Insomnia o cURL):

```http
POST http://localhost:8180/realms/tpi-backend/protocol/openid-connect/token
Content-Type: application/x-www-form-urlencoded

grant_type=authorization_code
code=<el_code_recibido>  <-- REEMPLAZA ESTE VALOR
client_id=tpi-backend-client
redirect_uri=http://localhost:8080/api/login/oauth2/code/keycloak
```

### Paso 3: Usar el `access_token`

La respuesta del _Token Endpoint_ contiene el `access_token` (JWT) que debe usarse para interactuar con las APIs del backend.

**Ejemplo de Respuesta (Fragmento):**

```json
{
  "access_token": "eyJhbGciOiJSUzI1NiIsInR5cCIgOiAiSldUIiwi...",
  "expires_in": 300,
  "token_type": "Bearer"
  // ... otros campos
}
```

Para usar el token, colócalo en la cabecera **`Authorization`** de tus peticiones al API Gateway:

| Cabecera          | Valor                            |
| :---------------- | :------------------------------- |
| **Authorization** | `Bearer <access_token_extraido>` |

Ahora puedes realizar peticiones a tu API Gateway en `http://localhost:8080/` a los _endpoints_ protegidos.
