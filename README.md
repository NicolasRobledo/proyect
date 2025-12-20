# PIME - Plataforma de E-commerce

Sistema de e-commerce moderno con arquitectura basada en perfiles de entorno, permitiendo desarrollo independiente de frontend y backend.

## Arquitectura por Perfiles

```mermaid
graph TB
    subgraph "Profile: PROD"
        direction TB
        UP[Usuario] --> NP[Nginx]
        NP --> FP[Frontend]
        NP --> BP[Backend]
        BP --> DBP[(MySQL Prod)]
        BP <--> GP[Google OAuth]
    end

    subgraph "Profile: DEV"
        direction TB
        UD[Frontend Dev] --> BD[Backend Dev]
        BD --> DBD[(MySQL Dev<br/>vacía)]
        BD --> SA[Auth Simulada<br/>/api/dev/login]
    end

    subgraph "Docker Hub"
        DH[pime-backend:dev]
    end

    DH -.->|docker pull| BD

    style GP fill:#c27c1a
    style SA fill:#27ae60
    style DH fill:#2496ed
```

## Perfiles de Entorno

| Profile | Auth | Base de Datos | Uso |
|---------|------|---------------|-----|
| `prod` | Google OAuth real | MySQL producción | Deploy en VPS |
| `dev` | Auth simulada | MySQL local vacía | Frontend developers |

### Profile DEV (para frontend developers)

```mermaid
sequenceDiagram
    participant FD as Frontend Dev
    participant B as Backend (dev)
    participant DB as MySQL Dev

    Note over FD: docker compose up
    FD->>B: POST /api/dev/login
    Note right of B: No requiere Google
    B->>B: Genera JWT fake
    B->>FD: { token, user }
    FD->>B: GET /api/user/me
    B->>FD: Datos del usuario
    Note over FD: Puede desarrollar<br/>sin OAuth real
```

### Profile PROD (producción)

```mermaid
sequenceDiagram
    participant U as Usuario
    participant F as Frontend
    participant B as Backend
    participant G as Google
    participant DB as MySQL

    U->>F: Click "Login con Google"
    F->>B: GET /api/auth/oauth2/authorization/google
    B->>G: Redirect a Google OAuth
    G->>U: Pantalla de login
    U->>G: Credenciales
    G->>B: Callback con código
    B->>G: Intercambio por tokens
    G->>B: Access token + User info
    B->>DB: Crear/Actualizar usuario
    B->>B: Generar JWT
    B->>F: Set Cookie (JWT) + Redirect
    F->>U: Mostrar perfil
```

## Estructura del Proyecto

```
proyecto-pime/
├── backend/
│   ├── src/main/resources/
│   │   ├── application.yml            # Config común
│   │   ├── application-dev.yml        # Auth simulada + DB local
│   │   └── application-prod.yml       # Google OAuth + DB prod
│   ├── Dockerfile
│   └── build.gradle.kts
│
├── frontend/
│   ├── src/
│   ├── package.json
│   └── Dockerfile
│
├── database/
│   └── migrations/
│
├── nginx/
│   └── nginx.conf
│
├── docker-compose.yml              # Producción completa
├── docker-compose.dev.yml          # Para frontend devs
│
└── .github/workflows/
    ├── deploy-prod.yml             # Deploy a VPS
    └── publish-dev-image.yml       # Publica imagen dev a Docker Hub
```

## Flujo de Desarrollo

```mermaid
flowchart TB
    subgraph "Backend Developer"
        BD1[Corre Spring Boot local]
        BD2[./gradlew bootRun]
        BD1 --> BD2
    end

    subgraph "Frontend Developer"
        FD1[docker compose -f docker-compose.dev.yml up]
        FD2[Tiene backend + DB listos]
        FD3[npm run dev]
        FD1 --> FD2 --> FD3
    end

    subgraph "Docker Hub"
        DH[tuusuario/pime-backend:dev]
    end

    DH -.->|pull automático| FD1

    style DH fill:#2496ed
```

## Guía para Frontend Developers

> **El backend viene incluido.** No necesitas instalar Java, configurar bases de datos, ni entender el backend. Solo Docker.

### ¿Qué obtienes?

```mermaid
graph TB
    subgraph FD[" 🧑‍💻 Frontend Developer "]
        subgraph LOCAL[" 💻 Tu máquina local "]
            subgraph DOCKER[" 🐳 Imagen Docker "]
                BE[Backend Real<br/>Spring Boot + Auth Simulada]
                DB[(MySQL vacía)]
                BE --> DB
            end
        end
        CODE[Tu código frontend<br/>npm run dev]
        CODE -->|fetch API| BE
    end

    style DOCKER fill:#1a4d7c,stroke:#2980b9,color:#fff
    style LOCAL fill:#2d3748,stroke:#4a5568,color:#fff
    style FD fill:#1a202c,stroke:#2d3748,color:#fff
    style BE fill:#27ae60,stroke:#1e8449,color:#fff
    style DB fill:#8e44ad,stroke:#6c3483,color:#fff
    style CODE fill:#e67e22,stroke:#d35400,color:#fff
```

**Un comando y listo:**
```bash
docker compose -f docker-compose.dev.yml up
```

El backend dentro de la imagen **es el mismo código de producción**, solo configurado con auth simulada.

### Requisitos
- Docker & Docker Compose (nada más)

### Setup

```bash
# 1. Clonar repo
git clone https://github.com/NicolasRobledo/proyect.git
cd proyect

# 2. Levantar backend + base de datos
docker compose -f docker-compose.dev.yml up
```

**Eso es todo.** Ahora tienes:

| Servicio | URL | Descripción |
|----------|-----|-------------|
| Backend API | `http://localhost:8080` | Backend real con auth simulada |
| MySQL | `localhost:3306` | Base de datos vacía |

### Login simulado (sin Google)

```bash
# Obtener token de desarrollo
curl -X POST http://localhost:8080/api/dev/login \
  -H "Content-Type: application/json" \
  -d '{"email": "test@example.com", "nombre": "Usuario Test"}'
```

Respuesta:
```json
{
  "token": "eyJhbGciOiJIUzI1NiIs...",
  "user": {
    "id": 1,
    "email": "test@example.com",
    "nombre": "Usuario Test"
  }
}
```

### API Endpoints

| Método | Endpoint | Descripción | Disponible en |
|--------|----------|-------------|---------------|
| POST | `/api/dev/login` | Login simulado | Solo DEV |
| GET | `/api/auth/oauth2/authorization/google` | Login con Google | Solo PROD |
| GET | `/api/user/me` | Usuario actual | DEV + PROD |
| POST | `/api/user/logout` | Cerrar sesión | DEV + PROD |

## Guía para Backend Developers

### Requisitos
- Java 21
- Docker (solo para MySQL)

### Setup

```bash
# Levantar solo la base de datos
docker compose -f docker-compose.dev.yml up db

# Correr backend con hot reload
cd backend
./gradlew bootRun --args='--spring.profiles.active=dev'
```

### Configuración de perfiles

**application-dev.yml**
```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/pime_dev
  security:
    # Auth simulada habilitada

app:
  auth:
    dev-mode: true  # Habilita /api/dev/login
```

**application-prod.yml**
```yaml
spring:
  datasource:
    url: jdbc:mysql://${DB_HOST}:3306/${DB_NAME}
  security:
    oauth2:
      client:
        registration:
          google:
            client-id: ${GOOGLE_CLIENT_ID}
            client-secret: ${GOOGLE_CLIENT_SECRET}

app:
  auth:
    dev-mode: false  # Deshabilita /api/dev/login
```

## CI/CD Pipeline

```mermaid
flowchart LR
    subgraph "Push a main"
        P[Push]
    end

    subgraph "GitHub Actions"
        P --> W1[publish-dev-image.yml]
        P --> W2[deploy-prod.yml]
    end

    subgraph "Docker Hub"
        W1 --> |build + push| DH[pime-backend:dev]
    end

    subgraph "VPS Producción"
        W2 --> |deploy| VPS[Contenedores Prod]
    end

    style DH fill:#2496ed
    style VPS fill:#27ae60
```

### Workflows

| Workflow | Trigger | Acción |
|----------|---------|--------|
| `publish-dev-image.yml` | Push a `main` en `backend/` | Construye y publica `pime-backend:dev` a Docker Hub |
| `deploy-prod.yml` | Push a `main` | Deploy completo a VPS |

### Secretos de GitHub Requeridos

| Secret | Uso |
|--------|-----|
| `DOCKERHUB_USERNAME` | Publicar imagen dev |
| `DOCKERHUB_TOKEN` | Publicar imagen dev |
| `VPS_HOST` | Deploy producción |
| `VPS_USER` | Deploy producción |
| `VPS_PASSWORD` | Deploy producción |
| `GOOGLE_CLIENT_ID` | OAuth producción |
| `GOOGLE_CLIENT_SECRET` | OAuth producción |

## Modelo de Datos

```mermaid
erDiagram
    USUARIOS {
        bigint id PK
        varchar google_id UK
        varchar email
        boolean email_verified
        varchar nombre
        varchar foto_url
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

| Capa | Tecnología | Versión |
|------|------------|---------|
| Frontend | Astro | 5.0 |
| Frontend | React | 18.3 |
| Backend | Spring Boot | 4.0 |
| Backend | Java | 21 |
| Database | MySQL | 8 |
| Proxy | Nginx | Alpine |
| Container | Docker | Latest |
| Registry | Docker Hub | - |
| CI/CD | GitHub Actions | - |

## Archivos Docker Compose

### docker-compose.dev.yml (Frontend developers)

```yaml
services:
  backend:
    image: tuusuario/pime-backend:dev
    ports:
      - "8080:8080"
    environment:
      - SPRING_PROFILES_ACTIVE=dev
    depends_on:
      - db

  db:
    image: mysql:8
    environment:
      - MYSQL_ROOT_PASSWORD=dev
      - MYSQL_DATABASE=pime_dev
    ports:
      - "3306:3306"
```

### docker-compose.yml (Producción)

```yaml
services:
  nginx:
    image: nginx:alpine
    ports:
      - "80:80"
      - "443:443"
    depends_on:
      - frontend
      - backend

  frontend:
    build: ./frontend

  backend:
    build: ./backend
    environment:
      - SPRING_PROFILES_ACTIVE=prod
      - GOOGLE_CLIENT_ID=${GOOGLE_CLIENT_ID}
      - GOOGLE_CLIENT_SECRET=${GOOGLE_CLIENT_SECRET}
    depends_on:
      - db

  db:
    image: mysql:8
    volumes:
      - mysql_data:/var/lib/mysql

volumes:
  mysql_data:
```

## Licencia

MIT License
