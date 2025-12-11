# Proyecto PIME

Aplicación de microservicios con frontend (Astro), backend (Spring Boot), base de datos (MySQL) y proxy reverso (Nginx).

---

## Guía para Desarrolladores

### Cómo trabajar

1. **Clonar el repositorio**
   ```bash
   git clone https://github.com/NicolasRobledo/proyect.git
   cd proyect
   ```

2. **Hacer cambios en el código**

3. **Subir cambios**
   ```bash
   git add .
   git commit -m "descripción del cambio"
   git push origin main
   ```

4. **Listo** - El deploy es automático

### Qué pasa cuando haces push

| Si modificas... | Se despliega automáticamente... |
|-----------------|--------------------------------|
| `frontend/**` | Frontend (Astro) |
| `backend/**` | Backend (Spring Boot) |
| `nginx/**` | Nginx (proxy) |
| `database/migrations/**` | Migraciones de BD |

> Los secretos (credenciales de Google, BD, VPS, etc.) ya están configurados en GitHub. No necesitas configurar nada local para desplegar.

### Ver estado del deploy

1. Ve a GitHub → **Actions**
2. Busca el workflow que se ejecutó
3. Verde = éxito, Rojo = error

### Agregar nueva migración de BD

1. Crear archivo en `database/migrations/`
2. Nombre: `V{numero}__descripcion.sql` (ej: `V2__add_productos.sql`)
3. Push y se ejecuta automáticamente

---

## Estructura del Proyecto

```
proyecto-pime/
├── backend/          # API Spring Boot (Java 21)
├── frontend/         # Web Astro + React
├── nginx/            # Configuración del proxy
├── database/         # Migraciones SQL (Flyway)
└── .github/workflows # Pipelines de deploy automático
```

---

## Arquitectura del Sistema

### Diagrama de Red

```mermaid
graph TB
    subgraph Internet
        User[Usuario]
    end

    subgraph pime-network
        NGINX[NGINX :80]
        FRONTEND[FRONTEND :3000]
        BACKEND[BACKEND :8080]
        MYSQL[MYSQL :3306]
    end

    User --> NGINX
    NGINX -->|/| FRONTEND
    NGINX -->|/api/| BACKEND
    BACKEND --> MYSQL
```

### Flujo de Requests

#### Frontend (/)
```mermaid
sequenceDiagram
    Usuario->>NGINX: GET /
    NGINX->>FRONTEND: proxy_pass :3000
    FRONTEND-->>NGINX: HTML/JS
    NGINX-->>Usuario: Respuesta
```

#### API (/api/)
```mermaid
sequenceDiagram
    Usuario->>NGINX: GET /api/users
    NGINX->>BACKEND: proxy_pass :8080
    BACKEND->>MYSQL: SELECT * FROM usuarios
    MYSQL-->>BACKEND: Datos
    BACKEND-->>NGINX: JSON
    NGINX-->>Usuario: Respuesta
```

### Flujo de Autenticación (Google OAuth2)

```mermaid
sequenceDiagram
    Usuario->>Frontend: Click "Login con Google"
    Frontend->>Backend: GET /api/auth/oauth2/authorization/google
    Backend->>Google: Redirect a Google
    Google->>Usuario: Pantalla de login
    Usuario->>Google: Autentica
    Google->>Backend: Callback con código
    Backend->>Backend: Guarda usuario en BD
    Backend->>Backend: Genera JWT
    Backend->>Frontend: Redirect con cookie "token"
    Frontend->>Backend: GET /api/user/me
    Backend->>Frontend: Datos del usuario
    Frontend->>Usuario: Muestra perfil
```

---

## Endpoints del Backend

| Método | Ruta | Descripción |
|--------|------|-------------|
| GET | `/api/auth/oauth2/authorization/google` | Inicia login con Google |
| GET | `/api/auth/callback/google` | Callback de Google |
| GET | `/api/user/me` | Obtiene usuario actual (requiere cookie) |
| POST | `/api/user/logout` | Cierra sesión |

---

## Aislamiento de Seguridad

| Sistema | Acceso Internet | Red Interna |
|---------|:---------------:|:-----------:|
| NGINX | ✅ | ✅ |
| FRONTEND | ❌ | ✅ |
| BACKEND | ❌ | ✅ |
| MYSQL | ❌ | ✅ |

> **MYSQL está completamente aislado de internet** - solo accesible desde la red interna.

---

## Desarrollo Local (Opcional)

Si quieres probar localmente antes de subir:

```bash
# Requiere Docker
docker compose up --build

# Accede a http://localhost
```

Para desarrollo local necesitas crear un archivo `.env` con las credenciales de Google OAuth. Contacta al administrador del proyecto para obtenerlas.

---

## Troubleshooting

| Problema | Solución |
|----------|----------|
| Deploy falló | Revisa logs en GitHub Actions |
| Error 502 en la web | El backend puede estar reiniciando, espera 30 segundos |
| Login no funciona | Verifica que los secretos de Google estén configurados en GitHub |
