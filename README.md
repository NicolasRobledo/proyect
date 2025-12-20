# PIME - Plataforma de E-commerce

Sistema de e-commerce moderno construido con arquitectura de microservicios, autenticación OAuth2/JWT y despliegue containerizado.

## Arquitectura del Sistema

```mermaid
graph TB
    subgraph Internet
        U[Usuario]
    end

    subgraph Docker Network
        subgraph Gateway
            N[NGINX<br/>Puerto 80/443]
        end

        subgraph Services
            F[Frontend<br/>Astro + React<br/>:3000]
            B[Backend<br/>Spring Boot<br/>:8080]
        end

        subgraph Data
            DB[(MySQL 8<br/>:3306)]
        end
    end

    subgraph External
        G[Google OAuth2]
    end

    U -->|HTTP/HTTPS| N
    N -->|"/*"| F
    N -->|"/api/*"| B
    B --> DB
    B <-->|OAuth2| G

    style N fill:#2d5016,stroke:#4a7c23
    style F fill:#1a4d7c,stroke:#2980b9
    style B fill:#7c1a4d,stroke:#c0392b
    style DB fill:#4a4a7c,stroke:#8e44ad
    style G fill:#c27c1a,stroke:#e67e22
```

## Flujo de Autenticación

```mermaid
sequenceDiagram
    participant U as Usuario
    participant F as Frontend
    participant N as Nginx
    participant B as Backend
    participant G as Google
    participant DB as MySQL

    U->>F: Click "Login con Google"
    F->>N: GET /api/auth/oauth2/authorization/google
    N->>B: Proxy request
    B->>G: Redirect a Google OAuth
    G->>U: Pantalla de login
    U->>G: Credenciales
    G->>B: Callback con código
    B->>G: Intercambio por tokens
    G->>B: Access token + User info
    B->>DB: Crear/Actualizar usuario
    DB->>B: Usuario guardado
    B->>B: Generar JWT
    B->>F: Set Cookie (JWT) + Redirect /
    F->>N: GET /api/user/me
    N->>B: Proxy con cookie
    B->>B: Validar JWT
    B->>F: Datos del usuario
    F->>U: Mostrar perfil
```

## Modelo de Datos

```mermaid
erDiagram
    USUARIOS {
        bigint id PK
        varchar google_id UK
        varchar email
        boolean email_verified
        varchar nombre
        varchar nombre_pila
        varchar apellido
        varchar foto_url
        varchar locale
        timestamp created_at
        timestamp updated_at
    }

    PRODUCTOS {
        bigint id PK
        varchar nombre
        text descripcion
        decimal precio
        int stock
        varchar imagen_url
        bigint categoria_id FK
        timestamp created_at
    }

    CATEGORIAS {
        bigint id PK
        varchar nombre
        varchar descripcion
    }

    ORDENES {
        bigint id PK
        bigint usuario_id FK
        decimal total
        varchar estado
        timestamp created_at
    }

    ORDEN_ITEMS {
        bigint id PK
        bigint orden_id FK
        bigint producto_id FK
        int cantidad
        decimal precio_unitario
    }

    USUARIOS ||--o{ ORDENES : realiza
    ORDENES ||--|{ ORDEN_ITEMS : contiene
    PRODUCTOS ||--o{ ORDEN_ITEMS : incluido_en
    CATEGORIAS ||--o{ PRODUCTOS : agrupa
```

## Stack Tecnológico

```mermaid
graph LR
    subgraph Frontend
        A[Astro 5.0] --> R[React 18]
        R --> TS[TypeScript]
    end

    subgraph Backend
        SB[Spring Boot 4.0] --> J[Java 21]
        SB --> SEC[Spring Security]
        SEC --> OAuth[OAuth2 Client]
        SEC --> JWT[JWT]
        SB --> JPA[Spring Data JPA]
    end

    subgraph Database
        MY[MySQL 8]
        FW[Flyway]
    end

    subgraph Infrastructure
        D[Docker]
        DC[Docker Compose]
        NG[Nginx]
        GH[GitHub Actions]
    end

    style A fill:#ff5d01
    style SB fill:#6db33f
    style MY fill:#4479a1
    style D fill:#2496ed
```

## Estructura del Proyecto

```
proyecto-pime/
├── backend/                    # API REST (Spring Boot)
│   ├── src/main/java/
│   │   └── com/example/demo/
│   │       ├── config/         # Configuración de seguridad
│   │       ├── controller/     # Endpoints REST
│   │       ├── entity/         # Entidades JPA
│   │       ├── repository/     # Repositorios
│   │       └── service/        # Lógica de negocio
│   ├── src/main/resources/
│   │   └── application.yaml
│   ├── build.gradle.kts
│   └── Dockerfile
│
├── frontend/                   # Web UI (Astro + React)
│   ├── src/
│   │   ├── components/         # Componentes React
│   │   └── pages/              # Páginas Astro
│   ├── package.json
│   └── Dockerfile
│
├── nginx/                      # Proxy reverso
│   └── nginx.conf
│
├── database/                   # Migraciones SQL
│   └── migrations/
│
├── .github/workflows/          # CI/CD
│   ├── deploy-backend.yml
│   ├── deploy-frontend.yml
│   ├── deploy-db.yml
│   └── deploy-nginx.yml
│
└── docker-compose.yml
```

## API Endpoints

| Método | Endpoint | Descripción | Auth |
|--------|----------|-------------|------|
| GET | `/api/auth/oauth2/authorization/google` | Iniciar login con Google | No |
| GET | `/api/auth/callback/google` | Callback de OAuth2 | No |
| GET | `/api/user/me` | Obtener usuario actual | JWT |
| POST | `/api/user/logout` | Cerrar sesión | JWT |

### Respuesta `/api/user/me`

```json
{
  "id": 1,
  "email": "usuario@gmail.com",
  "nombre": "Juan Pérez",
  "nombrePila": "Juan",
  "apellido": "Pérez",
  "fotoUrl": "https://lh3.googleusercontent.com/..."
}
```

## Instalación

### Requisitos

- Docker & Docker Compose
- Cuenta de Google Cloud (para OAuth2)

### Variables de Entorno

Crear archivo `.env` en la raíz:

```env
# Base de Datos
MYSQL_ROOT_PASSWORD=tu_password_seguro
MYSQL_DATABASE=pime

# Google OAuth2
GOOGLE_CLIENT_ID=tu_client_id.apps.googleusercontent.com
GOOGLE_CLIENT_SECRET=tu_client_secret

# JWT
JWT_SECRET=tu_clave_secreta_muy_larga_y_segura
```

### Ejecutar Localmente

```bash
# Clonar repositorio
git clone https://github.com/tu-usuario/proyecto-pime.git
cd proyecto-pime

# Iniciar servicios
docker-compose up -d

# Ver logs
docker-compose logs -f
```

### Acceso

- **Aplicación**: http://localhost
- **API**: http://localhost/api

## Deployment

```mermaid
flowchart LR
    subgraph GitHub
        PR[Push/PR] --> GHA[GitHub Actions]
    end

    subgraph Workflows
        GHA --> |backend/**| WB[deploy-backend.yml]
        GHA --> |frontend/**| WF[deploy-frontend.yml]
        GHA --> |database/**| WD[deploy-db.yml]
        GHA --> |nginx/**| WN[deploy-nginx.yml]
    end

    subgraph VPS
        WB --> |SCP + SSH| CB[Build Backend]
        WF --> |SCP + SSH| CF[Build Frontend]
        WD --> |SSH| CD[Run Migrations]
        WN --> |SCP + SSH| CN[Reload Nginx]

        CB --> DC[Docker Container]
        CF --> DC
        CN --> DC
    end

    style GHA fill:#2088ff
    style DC fill:#2496ed
```

El despliegue es automático mediante GitHub Actions:

1. Cada push a `main` dispara el workflow correspondiente
2. Los archivos se copian al VPS via SCP
3. Se reconstruye la imagen Docker
4. Se reinicia el contenedor

### Secretos de GitHub Requeridos

| Secret | Descripción |
|--------|-------------|
| `VPS_HOST` | IP o dominio del servidor |
| `VPS_USER` | Usuario SSH |
| `VPS_PASSWORD` | Contraseña SSH |
| `MYSQL_ROOT_PASSWORD` | Contraseña de MySQL |
| `MYSQL_DATABASE` | Nombre de la base de datos |
| `GOOGLE_CLIENT_ID` | ID de OAuth2 de Google |
| `GOOGLE_CLIENT_SECRET` | Secret de OAuth2 |

## Seguridad

- **Autenticación**: OAuth2 con Google + JWT
- **Base de datos**: Aislada en red Docker interna
- **Proxy reverso**: Nginx maneja SSL/TLS
- **Tokens**: JWT con expiración de 24 horas
- **Migraciones**: Flyway para versionado de esquema

## Tecnologías

| Capa | Tecnología | Versión |
|------|------------|---------|
| Frontend | Astro | 5.0 |
| Frontend | React | 18.3 |
| Backend | Spring Boot | 4.0 |
| Backend | Java | 21 |
| Database | MySQL | 8 |
| Proxy | Nginx | Alpine |
| Container | Docker | Latest |
| CI/CD | GitHub Actions | - |

## Licencia

MIT License
