# Documentación del stack tecnológico actual - ERP Iglesias

## 1. STACK TECNOLÓGICO ACTUAL

### Backend
| Componente | Versión | Descripción |
|-----------|---------|------------|
| **Java** | 17 | Lenguaje de programación base |
| **Spring Boot** | 3.2.3 | Framework web y aplicación |
| **Spring Security** | 3.2.3 | Autenticación y autorización |
| **Spring Data JPA** | 3.2.3 | Persistencia de datos (ORM) |
| **PostgreSQL** | Latest | Base de datos relacional |
| **JWT (jjwt)** | 0.11.5 | Autenticación basada en tokens |
| **Maven** | 3.x | Gestor de dependencias |

### Frontend
| Componente | Versión | Descripción |
|-----------|---------|------------|
| **Angular** | 17.3.0 | Framework web SPA |
| **Angular Material** | 17.3.10 | Componentes UI |
| **TypeScript** | 5.4.2 | Lenguaje de programación |
| **RxJS** | 7.8.0 | Programación reactiva |
| **Angular CDK** | 17.3.10 | Component Dev Kit |
| **Nginx** | Latest | Servidor web (producción) |

### Infraestructura & Despliegue
| Componente | Descripción |
|-----------|------------|
| **Docker** | Containerización de servicios |
| **Docker Compose** | Orquestación de contenedores |
| **GitHub Actions** | CI/CD pipeline |

---

## 2. ANÁLISIS DE SEPARACIÓN DE RESPONSABILIDADES

### 2.1 Arquitectura en Capas

#### Backend - Estructura por Capas

```
                    ┌─────────────────────┐
                    │   REST Controllers  │  ← Capa de Presentación
                    │  (API Endpoints)    │
                    └──────────┬──────────┘
                               │
                    ┌──────────▼──────────┐
                    │   Servicios (TBD)   │  ← Capa de Lógica de Negocio
                    │                     │    [NECESARIO MEJORAR]
                    └──────────┬──────────┘
                               │
                    ┌──────────▼──────────┐
                    │  JPA Repositories   │  ← Capa de Acceso a Datos
                    │   (Data Access)     │
                    └──────────┬──────────┘
                               │
                    ┌──────────▼──────────┐
                    │  PostgreSQL DB      │  ← Capa de Base de Datos
                    └─────────────────────┘
```

#### Componentes Identificados:

**✅ FORTALEZAS:**
- Controllers bien separados (AuthController, PersonController, etc.)
- Uso de Repositories (Data Access Object Pattern)
- Spring Security correctamente configurado
- Entidades JPA bien anotadas
- JWT para autenticación sin estado

**⚠️ ÁREAS DE MEJORA:**
- ❌ No hay capa de servicios clara (Service Layer)
- ❌ Lógica de negocio probablemente esparcida en Controllers
- ❌ Falta de DTOs (Data Transfer Objects)
- ❌ Sin validación centralizada
- ❌ Sin manejo centralizado de excepciones

### 2.2 Componentes del Frontend

#### Angular - Separación de Responsabilidades

```
src/app/
├── Components          ← Presentación UI
├── Services            ← Lógica de negocio & comunicación
├── Guards              ← Protección de rutas
├── Interceptors        ← Manipulación de HTTP
└── Config              ← Configuración de rutas
```

**✅ FORTALEZAS:**
- Guards para protección de rutas (auth.guard.ts)
- Interceptors para agregar tokens JWT (auth.interceptor.ts)
- Separación de componentes por dominio
- Servicio centralizado de API (api.service.ts)

**⚠️ ÁREAS DE MEJORA:**
- ❌ Componentes monolíticos (mezclan lógica y presentación)
- ❌ Sin modelos/interfaces TypeScript tipados
- ❌ Posible duplicación de lógica en múltiples componentes
- ❌ Sin estrategia clara de estado (no se ve NgRx/Akita)

---

## 3. ANÁLISIS DE ACOPLAMIENTO ENTRE MÓDULOS

### 3.1 Acoplamiento Actual (Backend)

#### Control de Acoplamiento:

| Módulo | Acoplamiento | Nivel | Observaciones |
|--------|--------------|-------|--------------|
| **Controllers** | Controllers → Repositories | ALTO | Directa dependencia, sin servicios intermedios |
| **JPA Entities** | Bidireccional | ALTO | Entidades acopladas con persistencia |
| **Security** | Spring Security | BAJO | Bien integrado mediante anotaciones |
| **Database** | ORM (Hibernate) | MEDIO | Desacoplado por abstracción JPA |

#### Problemas de Acoplamiento Identificados:

```java
// ❌ ACOPLAMIENTO ALTO - Controlador directo a Repository
@PostMapping("{/person}/{id}")
public ResponseEntity<?> savePerson(@PathVariable Long id, ...) {
    Person person = repository.findById(id);  // Acoplamiento directo
    // Lógica compleja aquí
    return response;
}

// ✅ MEJOR - Con capa de servicios
@PostMapping("{/person}/{id}")
public ResponseEntity<?> savePerson(@PathVariable Long id, ...) {
    Person person = personService.getPerson(id);  // Desacoplado
    return response;
}
```

### 3.2 Acoplamiento Frontend-Backend

**Nivel: MEDIO-ALTO**
- Frontend acoplado a estructura exacta de APIs REST
- Sin versionamiento de API visible
- Sin contrato clara entre cliente-servidor

---

## 4. ANÁLISIS DE ESCALABILIDAD

### 4.1 Escalabilidad Horizontal

| Aspecto | Estado | Recomendación |
|--------|--------|--------------|
| **Stateless Backend** | ✅ JWT | Listo para múltiples instancias |
| **Database** | ⚠️ PostgreSQL monolítica | Considerar sharding en el futuro |
| **Cache** | ❌ No identificado | Implementar Redis para sesiones |
| **Load Balancer** | ❌ No visible | Necesario en producción |

### 4.2 Escalabilidad Vertical

**Limitaciones Identificadas:**
- Ausencia de paginación clara en endpoints
- Sin índices de base de datos documentados
- Entidades sin lazy loading optimizado
- Posibles N+1 queries

### 4.3 Limitaciones Actuales

```
┌─────────────────────────────────────────┐
│ Cuello de Botella: Database Connection  │
├─────────────────────────────────────────┤
│ • Pool de conexiones no configurado     │
│ • Sin caché de datos frecuentes         │
│ • Sin API Gateway para throttling       │
└─────────────────────────────────────────┘
```

---

## 5. ANÁLISIS DE MANTENIBILIDAD

### 5.1 Código Fuente

| Métrica | Evaluación | Evidencia |
|---------|-----------|----------|
| **Legibilidad** | ✅ BUENA | Nombres descriptivos, estructura clara |
| **Testabilidad** | ⚠️ MEDIA | Sin tests identificados, alto acoplamiento |
| **Documentación** | ❌ BAJA | Falta JavaDoc y comentarios |
| **Consistencia** | ✅ BUENA | Patrones similares a través del código |

### 5.2 Deuda Técnica Identificada

**CRÍTICA:**
- [ ] Implementar capa de servicios (Service Layer)
- [ ] Crear DTOs para transferencia de datos
- [ ] Agregar validación con JSR-303 (@Valid)
- [ ] Implementar manejo centralizado de excepciones

**IMPORTANTE:**
- [ ] Agregar logging estructurado
- [ ] Crear tests unitarios (JUnit 5)
- [ ] Documentar APIs REST (Swagger/OpenAPI)
- [ ] Implementar CI/CD pipeline

**MEDIA:**
- [ ] Agregar cache (Redis)
- [ ] Optimizar lazy loading
- [ ] Implementar auditoría (createdBy, updatedBy)

### 5.3 Documentación

| Tipo | Estado | Ubicación |
|------|--------|-----------|
| Code Comments | ❌ Falta | Bloques de Java sin documentar |
| API Docs | ❌ Falta | No se ve Swagger/OpenAPI |
| ADR | ✅ Existe | ADR/ folder -ojo ver contenido |
| README | ⚠️ Parcial | frontend/ tiene README |

---

## 6. ANÁLISIS DE SEGURIDAD ARQUITECTÓNICA

### 6.1 Controles Implementados

✅ **Bien Implementado:**
- JWT para autenticación sin estado
- Spring Security con @PreAuthorize
- Contraseñas hasheadas (passwordHash)
- Separación de users (AppUser vs Person)
- HTTPS recomendado (Nginx en Docker)

⚠️ **Pendiente:**
- CORS explícitamente configurado
- Rate limiting
- Validación de entrada (input sanitization)
- SQL Injection prevention (dependencia de ORM)


---

## 7. CONCLUSIONES

El proyecto ERP Iglesias presenta una arquitectura funcional pero inmadura que requiere refactorización estratégica para alcanzar calidad empresarial.

### Estado Actual
- ✅ Stack moderno y bien elegido (Spring Boot 3.2, Angular 17)
- ✅ Estructura básica correcta (Controllers, Repositories)
- ❌ Falta capa de servicios (crítico)
- ❌ Sin pruebas automatizadas
- ❌ Documentación insuficiente
- ⚠️ Acoplamiento alto en data access
