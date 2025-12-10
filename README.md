# Arquitectura del Sistema

## Diagrama de Redes

```mermaid
graph TB
    subgraph Internet
        User[Usuario]
    end

    subgraph public-network
        NGINX[NGINX :80]
        FRONTEND[FRONTEND :3000]
        BACKEND1[BACKEND :8080]
    end

    subgraph db-network
        BACKEND2[BACKEND :8080]
        MYSQL[MYSQL :3306]
    end

    User --> NGINX
    NGINX -->|/| FRONTEND
    NGINX -->|/api/| BACKEND1
    BACKEND2 --> MYSQL

    BACKEND1 -.->|mismo contenedor| BACKEND2
```

## Intersección de Redes (Venn)

```mermaid
graph LR
    subgraph public-network
        N[NGINX]
        F[FRONTEND]
        B1[BACKEND]
    end

    subgraph db-network
        B2[BACKEND]
        M[MYSQL]
    end

    B1 === B2

    N --> F
    N --> B1
    B2 --> M
```

## Flujo de Requests

### Frontend (/)
```mermaid
sequenceDiagram
    Usuario->>NGINX: GET /
    NGINX->>FRONTEND: proxy_pass :3000
    FRONTEND-->>NGINX: HTML/JS
    NGINX-->>Usuario: Respuesta
```

### API (/api/)
```mermaid
sequenceDiagram
    Usuario->>NGINX: GET /api/users
    NGINX->>BACKEND: proxy_pass :8080
    BACKEND->>MYSQL: SELECT * FROM usuarios
    MYSQL-->>BACKEND: Datos
    BACKEND-->>NGINX: JSON
    NGINX-->>Usuario: Respuesta
```

## Contenedores por Red

| Red | Contenedores |
|-----|-------------|
| **public-network** | nginx, frontend, backend |
| **db-network** | backend, mysql |

## Aislamiento de Seguridad

| Sistema | Internet | public-network | db-network |
|---------|:--------:|:--------------:|:----------:|
| NGINX | ✅ | - | ❌ |
| FRONTEND | ❌ | ✅ | ❌ |
| BACKEND | ❌ | ✅ | ✅ |
| MYSQL | ❌ | ❌ | ✅ |

> **MYSQL está completamente aislado** - solo el BACKEND puede acceder a él.

## Workflows de Deploy

```mermaid
graph LR
    subgraph GitHub Actions
        F[deploy-frontend.yml] -->|push frontend/**| FE[Frontend]
        B[deploy-backend.yml] -->|push backend/**| BE[Backend]
        D[deploy-db.yml] -->|push database/**| DB[Migraciones]
        N[deploy-nginx.yml] -->|push nginx/**| NG[Nginx]
    end
```
