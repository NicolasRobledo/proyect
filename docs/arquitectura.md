# Arquitectura del Sistema

## Diagrama de Redes (Venn)

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                                                                             │
│                              public-network                                 │
│                                                                             │
│    ┌───────────────┐      ┌───────────────┐      ┌───────────────┐         │
│    │               │      │               │      │               │         │
│    │    NGINX      │      │   FRONTEND    │      │    BACKEND    │         │
│    │    :80        │─────▶│    :3000      │      │     :8080     │         │
│    │               │      │               │      │               │         │
│    │  (entrada     │      │  (Astro +     │      │  (Spring      │         │
│    │   pública)    │─────────────────────────────▶   Boot)       │         │
│    │               │      │   React)      │      │               │         │
│    └───────────────┘      └───────────────┘      └───────┬───────┘         │
│                                                          │                 │
│                                                          │                 │
└──────────────────────────────────────────────────────────┼─────────────────┘
                                                           │
                           ┌───────────────────────────────┼─────────────────┐
                           │                               │                 │
                           │        db-network             │                 │
                           │                               │                 │
                           │      ┌───────────────┐      ┌─┴─────────────┐   │
                           │      │               │      │               │   │
                           │      │    MYSQL      │◀─────│    BACKEND    │   │
                           │      │    :3306      │      │     :8080     │   │
                           │      │               │      │               │   │
                           │      │  (base de     │      │  (Spring      │   │
                           │      │   datos)      │      │   Boot)       │   │
                           │      │               │      │               │   │
                           │      └───────────────┘      └───────────────┘   │
                           │                                                 │
                           └─────────────────────────────────────────────────┘
```

## Intersección de Redes

```
                    ┌─────────────────────────────────┐
                    │         public-network          │
                    │                                 │
                    │   NGINX    FRONTEND             │
                    │                                 │
                    │              ┌──────────────────┼──────────────────┐
                    │              │                  │                  │
                    │              │     BACKEND      │                  │
                    │              │    (en ambas     │                  │
                    │              │      redes)      │                  │
                    │              │                  │                  │
                    └──────────────┼──────────────────┘                  │
                                   │                                     │
                                   │         db-network                  │
                                   │                                     │
                                   │                     MYSQL           │
                                   │                                     │
                                   └─────────────────────────────────────┘
```

## Explicación

### public-network (Red Pública)
Sistemas que pueden comunicarse entre sí:
- **NGINX** → puede enviar requests a FRONTEND y BACKEND
- **FRONTEND** → recibe requests de NGINX
- **BACKEND** → recibe requests de NGINX

### db-network (Red de Base de Datos)
Sistemas que pueden comunicarse entre sí:
- **BACKEND** → puede conectarse a MYSQL
- **MYSQL** → solo recibe conexiones del BACKEND

### BACKEND (Intersección)
El backend está en **ambas redes** porque:
1. Necesita recibir requests HTTP de NGINX (public-network)
2. Necesita conectarse a la base de datos (db-network)

## Flujo de Datos

```
Usuario
   │
   ▼
┌──────┐    ┌──────────┐
│NGINX │───▶│FRONTEND  │  Ruta: /
│ :80  │    │  :3000   │
└──────┘    └──────────┘

Usuario
   │
   ▼
┌──────┐    ┌──────────┐    ┌───────┐
│NGINX │───▶│ BACKEND  │───▶│ MYSQL │  Ruta: /api/*
│ :80  │    │  :8080   │    │ :3306 │
└──────┘    └──────────┘    └───────┘
```

## Aislamiento de Seguridad

| Sistema | Accesible desde Internet | Accesible desde public-network | Accesible desde db-network |
|---------|-------------------------|-------------------------------|---------------------------|
| NGINX | ✅ Puerto 80 | - | ❌ |
| FRONTEND | ❌ | ✅ | ❌ |
| BACKEND | ❌ | ✅ | ✅ |
| MYSQL | ❌ | ❌ | ✅ |

**MYSQL está completamente aislado** - solo el BACKEND puede acceder a él.
