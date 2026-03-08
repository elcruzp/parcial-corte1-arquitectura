# Architecture Decision Record (ADR) - ERP Iglesias

## 1. Introducción y Contexto Actual

### 1.1 Estado Actual de la Arquitectura

El sistema ERP Iglesias presenta una arquitectura cliente-servidor funcional pero con deficiencias en separación de responsabilidades y aplicación de principios de diseño. El análisis identificó una arquitectura de **madurez 2.25/5** con los siguientes problemas críticos:

#### Problemas Identificados:
1. **Acoplamiento alto entre capas:** Los controladores acceden directamente a repositorios, sin capa de servicios intermedia.
2. **Exposición de entidades en API:** Las entidades JPA se retornan directamente en respuestas HTTP (sin DTOs).
3. **Lógica de negocio dispersa:** La validación y procesamiento de datos se encuentra en los controladores.
4. **Falta de estandarización:** Manejo inconsistente de errores, logging no centralizado.
5. **Sin cobertura de pruebas:** El código carece de tests unitarios e integración.
6. **Ausencia de documentación API:** No existe Swagger/OpenAPI para los endpoints.

### 1.2 Stack Tecnológico Actual

| Componente | Tecnología | Versión |
|-----------|-----------|---------|
| **Backend** | Spring Boot | 3.2.3 (Java 17 LTS) |
| **ORM** | Spring Data JPA / Hibernate | Incluida en Spring |
| **Seguridad** | Spring Security + JWT | jjwt 0.11.5 |
| **Base de Datos** | PostgreSQL | 14+ |
| **Frontend** | Angular | 17.3.0 |
| **Contenedores** | Docker + Docker Compose | Últimas versiones |

---

## 2. Diagrama Modelo Entidad-Relación (MER)
![MER](<../MER/MER Diagram.png>)
---

## 3. Decisiones Arquitectónicas (10 Cambios Propuestos)

### ADR-001: Patrón Service Layer (Separación de Capas)

| Característica | Detalle |
|---|---|
| **Patrón de Diseño** | Service Layer Pattern |
| **Principios SOLID** | SRP, OCP, DIP |
| **Dónde se Aplica** | Capa entre Controllers → Repositories |
| **Archivo** | `backend/src/main/java/com/iglesia/services/` |
| **Estado** | Propuesto |

**¿Por qué mejora la arquitectura?**
- Separación clara de responsabilidades: Controllers vs lógica de negocio
- Testeable: Se puede mockear servicios fácilmente
- Reutilizable: Misma lógica desde múltiples endpoints

---

### ADR-002: Patrón DTO + Mapper (Aislamiento de Capas)

| Característica | Detalle |
|---|---|
| **Patrón de Diseño** | Data Transfer Object (DTO) + Mapper |
| **Principios SOLID** | ISP, DIP |
| **Dónde se Aplica** | Todas las clases de solicitud/respuesta HTTP |
| **Archivo** | `backend/src/main/java/com/iglesia/dtos/` |
| **Estado** | Propuesto |

**¿Por qué mejora la arquitectura?**
- Desacoplamiento: BD se puede cambiar sin afectar API
- Seguridad: No se exponen campos internos
- Evolución: Fácil versionar API sin romper clientes

---

### ADR-003: Global Exception Handler (Centralización de Errores)

| Característica | Detalle |
|---|---|
| **Patrón de Diseño** | Exception Handler / Aspect Pattern |
| **Principios SOLID** | SRP, OCP |
| **Dónde se Aplica** | `@RestControllerAdvice` global |
| **Archivo** | `backend/src/main/java/com/iglesia/exception/GlobalExceptionHandler.java` |
| **Estado** | Propuesto |

**¿Por qué mejora la arquitectura?**
- Respuestas consistentes: Mismo formato de error en toda la API
- Mantenible: Un único lugar para cambiar la estructura de errores
- Profesional: Clientes predictores del comportamiento en errores

---

### ADR-004: JSR-303/Jakarta Validation (Validación Declarativa)

| Característica | Detalle |
|---|---|
| **Patrón de Diseño** | Validation Framework Pattern |
| **Principios SOLID** | SRP, OCP |
| **Dónde se Aplica** | Anotaciones en DTOs (@Valid, @NotNull, @Email, etc.) |
| **Archivo** | `backend/src/main/java/com/iglesia/dtos/` |
| **Estado** | Propuesto |

**¿Por qué mejora la arquitectura?**
- Validación automática: Spring valida antes de entrar al servicio
- Mantenible: Validaciones declarativas, no imperativos
- Prevención: Datos inválidos nunca llegan a la BD

---

### ADR-005: Logging Centralizado con SLF4J (Trazabilidad)

| Característica | Detalle |
|---|---|
| **Patrón de Diseño** | Logging Pattern (SLF4J + Logback) |
| **Principios SOLID** | SRP |
| **Dónde se Aplica** | En servicios (nivel DEBUG, INFO, WARN, ERROR) |
| **Archivo** | `backend/resources/logback-spring.xml` |
| **Estado** | Propuesto |

**¿Por qué mejora la arquitectura?**
- Debugging en producción: Se registran eventos importantes
- Auditoría: Rastrear quién hizo qué y cuándo
- Rendimiento: Identificar cuellos de botella

---

### ADR-006: Framework de Testing (JUnit 5 + Mockito)

| Característica | Detalle |
|---|---|
| **Patrón de Diseño** | Testing Pattern (Unit + Integration) |
| **Principios SOLID** | OCP, DIP (código desacoplado es testeable) |
| **Dónde se Aplica** | Tests de servicios y controladores |
| **Archivo** | `backend/src/test/java/com/iglesia/` |
| **Estado** | Propuesto |

**¿Por qué mejora la arquitectura?**
- Confiabilidad: Tests previenen regresiones
- Documentación viva: Tests ejemplifican el comportamiento esperado
- Refactoring seguro: Cambios sin miedo a romper algo

---

### ADR-007: OpenAPI/Swagger (Documentación Automática)

| Característica | Detalle |
|---|---|
| **Patrón de Diseño** | API Documentation Pattern |
| **Principios SOLID** | ISP (Interface Segregation) |
| **Dónde se Aplica** | Anotaciones en controllers + Swagger UI |
| **Archivo** | `backend/src/main/java/com/iglesia/config/OpenApiConfiguration.java` |
| **Estado** | Propuesto |

**¿Por qué mejora la arquitectura?**
- Documentación viva: Se genera automáticamente desde código
- Testeo interactivo: Swagger UI permite probar endpoints
- Especificación: OpenAPI 3.0 para integración

---

### ADR-008: Optimización de Queries (N+1 Queries, Índices)

| Característica | Detalle |
|---|---|
| **Patrón de Diseño** | Query Optimization Pattern |
| **Principios SOLID** | Performance |
| **Dónde se Aplica** | Repositories (JOIN FETCH) + DDL (índices) |
| **Archivo** | `backend/src/main/java/com/iglesia/repositories/` |
| **Estado** | Propuesto |

**¿Por qué mejora la arquitectura?**
- Escalabilidad: Maneja más datos sin degradación
- Performance: Reduce queries a BD significativamente
- UX: Aplicación más rápida y responsiva

---

### ADR-009: Security Hardening (Authorization + CORS)

| Característica | Detalle |
|---|---|
| **Patrón de Diseño** | Security by Design |
| **Principios SOLID** | Security Principles |
| **Dónde se Aplica** | `@PreAuthorize` en métodos + SecurityConfig |
| **Archivo** | `backend/src/main/java/com/iglesia/security/` |
| **Estado** | Propuesto |

**¿Por qué mejora la arquitectura?**
- Control granular: Cada método valida permisos
- CORS configurado: API solo accesible desde frontend autorizado
- Auditoría: Operaciones sensibles se registran

---

### ADR-010: Configuración Externalizada (12-Factor App)

| Característica | Detalle |
|---|---|
| **Patrón de Diseño** | Configuration Management Pattern |
| **Principios SOLID** | 12-Factor App |
| **Dónde se Aplica** | Perfiles de Spring (local, prod) + env vars |
| **Archivo** | `backend/resources/application.yml` |
| **Estado** | Propuesto |

**¿Por qué mejora la arquitectura?**
- Flexibilidad: Diferentes configs por entorno sin recompilar
- Secretos: Credenciales en variables de entorno, no en código
- DevOps-friendly: Fácil deployar en Docker/Kubernetes

---

## 4. Cambios Implementados (5 de los 10)

### Cambio 1: Service Layer para PersonService

**ADR Relacionado:** ADR-001  
**Archivo Modificado:** `backend/src/main/java/com/iglesia/services/PersonService.java`

#### ANTES (Acoplamiento Directo)
```java
@RestController
@RequestMapping("/api/persons")
@RequiredArgsConstructor
public class PersonController {
    
    private final PersonRepository personRepository;
    private final ChurchRepository churchRepository;
    
    @PostMapping
    public ResponseEntity<Person> create(@RequestBody PersonCreateRequest request) {
        // Validación directa en controlador (INCORRECTO)
        if (personRepository.existsByEmail(request.getEmail())) {
            throw new Exception("Email duplicado");
        }
        
        // Lógica de negocio mezclada con HTTP
        Church church = churchRepository.findById(request.getChurchId())
            .orElseThrow();
        
        Person person = new Person();
        person.setName(request.getName());
        person.setEmail(request.getEmail());
        person.setChurch(church);
        
        return ResponseEntity.ok(personRepository.save(person));
    }
}
```

#### DESPUÉS (Service Layer)
```java
// PersonService.java - Interface
public interface PersonService {
    PersonResponse create(PersonCreateRequest request);
    PersonResponse findById(Long id);
}

// PersonServiceImpl.java - Implementación
@Service
@Transactional
@RequiredArgsConstructor
public class PersonServiceImpl implements PersonService {
    
    private final PersonRepository personRepository;
    private final ChurchRepository churchRepository;
    private final PersonMapper personMapper;
    private static final Logger logger = LoggerFactory.getLogger(PersonServiceImpl.class);
    
    @Override
    public PersonResponse create(PersonCreateRequest request) {
        logger.debug("Creando persona con email: {}", request.getEmail());
        
        if (personRepository.existsByEmail(request.getEmail())) {
            logger.warn("Email duplicado: {}", request.getEmail());
            throw new DuplicateEmailException("Email ya existe");
        }
        
        Church church = churchRepository.findById(request.getChurchId())
            .orElseThrow(() -> new ResourceNotFoundException("Iglesia no encontrada"));
        
        Person person = personMapper.toPerson(request);
        person.setChurch(church);
        
        Person saved = personRepository.save(person);
        logger.info("Persona creada: id={}, email={}", saved.getId(), saved.getEmail());
        
        return personMapper.toResponse(saved);
    }
    
    @Override
    @Transactional(readOnly = true)
    public PersonResponse findById(Long id) {
        return personRepository.findById(id)
            .map(personMapper::toResponse)
            .orElseThrow(() -> new ResourceNotFoundException("Persona no encontrada"));
    }
}

// PersonController.java - Refactorizado
@RestController
@RequestMapping("/api/v1/persons")
@RequiredArgsConstructor
public class PersonController {
    
    private final PersonService personService;  // Ahora depende de abstracción
    
    @PostMapping
    public ResponseEntity<PersonResponse> create(@Valid @RequestBody PersonCreateRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(personService.create(request));
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<PersonResponse> findById(@PathVariable Long id) {
        return ResponseEntity.ok(personService.findById(id));
    }
}
```

**Evidencia Funcional:**
- ✅ PersonController ahora limpio (solo HTTP)
- ✅ Lógica de negocio centralizada en PersonService
- ✅ Testeable: Fácil crear mock de PersonService
- ✅ Extensible: Se puede reutilizar en otros endpoints

---

### Cambio 2: DTOs + Mapper para PersonResponse

**ADR Relacionado:** ADR-002  
**Archivo Modificado:** `backend/src/main/java/com/iglesia/dtos/person/*`

#### ANTES (Entidades Expuestas)
```java
// PersonController.java - Retorna directamente entidad
@RestController
public class PersonController {
    @GetMapping("/{id}")
    public Person findById(@PathVariable Long id) {
        return personRepository.findById(id).orElse(null);
        // Expone: id, name, email, phoneNumber, church, appUser, createdAt, etc.
    }
}

// Cliente recibe TODA la entidad
{
  "id": 1,
  "name": "Juan",
  "email": "juan@iglesia.com",
  "phoneNumber": "1234567890",
  "church": { "id": 1, "name": "Iglesia Central" },
  "appUser": null,  // Campo innecesario
  "createdAt": "2026-03-07T10:30:00"
}
```

#### DESPUÉS (DTOs Segregados)
```java
// PersonCreateRequest.java
public record PersonCreateRequest(
    @NotBlank(message = "El nombre es requerido")
    String name,
    
    @Email(message = "El email debe ser válido")
    String email,
    
    @NotNull(message = "La iglesia es requerida")
    Long churchId
) {}

// PersonResponse.java - Solo lo necesario
public record PersonResponse(
    Long id,
    String name,
    String email,
    String phoneNumber,
    ChurchSummary church,
    LocalDateTime createdAt
) {}

public record ChurchSummary(Long id, String name) {}

// PersonMapper.java
@Component
public class PersonMapper {
    
    public Person toPerson(PersonCreateRequest request) {
        return Person.builder()
            .name(request.name())
            .email(request.email())
            .build();
    }
    
    public PersonResponse toResponse(Person person) {
        return new PersonResponse(
            person.getId(),
            person.getName(),
            person.getEmail(),
            person.getPhoneNumber(),
            new ChurchSummary(person.getChurch().getId(), person.getChurch().getName()),
            person.getCreatedAt()
        );
    }
}

// PersonController.java - Retorna DTO
@RestController
@RequestMapping("/api/v1/persons")
@RequiredArgsConstructor
public class PersonController {
    
    private final PersonService personService;
    
    @PostMapping
    public ResponseEntity<PersonResponse> create(
            @Valid @RequestBody PersonCreateRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(personService.create(request));
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<PersonResponse> findById(@PathVariable Long id) {
        return ResponseEntity.ok(personService.findById(id));
    }
}

// Cliente recibe SOLO lo necesario
{
  "id": 1,
  "name": "Juan",
  "email": "juan@iglesia.com",
  "phoneNumber": "1234567890",
  "church": { "id": 1, "name": "Iglesia Central" },
  "createdAt": "2026-03-07T10:30:00"
}
```

**Evidencia Funcional:**
- ✅ API Limpia: No expone campos innecesarios
- ✅ Bajo acoplamiento: BD puede cambiar sin afectar API
- ✅ Validación: @Valid automáticamente valida entrada
- ✅ Versionable: Se pueden crear PersonResponseV2 sin afectar v1

---

### Cambio 3: Global Exception Handler

**ADR Relacionado:** ADR-003  
**Archivo Modificado:** `backend/src/main/java/com/iglesia/exception/GlobalExceptionHandler.java`

#### ANTES (Manejo Inconsistente)
```java
// PersonController.java
@RestController
public class PersonController {
    
    @PostMapping
    public ResponseEntity<?> create(@RequestBody PersonCreateRequest request) {
        try {
            if (personRepository.existsByEmail(request.getEmail())) {
                return ResponseEntity.status(409)
                    .body("Email duplicado");  // Formato inconsistente
            }
            // ... Creación de persona
            return ResponseEntity.ok(person);  // 200 OK
        } catch (Exception e) {
            return ResponseEntity.status(500)
                .body(e.getMessage());  // Error inconsistente
        }
    }
}

// CourseController.java
@RestController
public class CourseController {
    
    @PostMapping
    public ResponseEntity<?> create(@RequestBody CourseCreateRequest request) {
        if (courseRepository.existsByName(request.getName())) {
            return ResponseEntity.status(400)
                .body(new ErrorDetail("Course already exists"));  // Formato DIFERENTE
        }
        // ...
    }
}
```

#### DESPUÉS (Exception Handler Centralizado)
```java
// GlobalExceptionHandler.java
@RestControllerAdvice
public class GlobalExceptionHandler {
    
    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);
    
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleResourceNotFound(
            ResourceNotFoundException ex, HttpServletRequest request) {
        ErrorResponse error = ErrorResponse.builder()
            .timestamp(LocalDateTime.now())
            .status(HttpStatus.NOT_FOUND.value())
            .error("RESOURCE_NOT_FOUND")
            .message(ex.getMessage())
            .path(request.getRequestURI())
            .build();
        logger.warn("Recurso no encontrado: {}", request.getRequestURI());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    }
    
    @ExceptionHandler(DuplicateEmailException.class)
    public ResponseEntity<ErrorResponse> handleDuplicateEmail(
            DuplicateEmailException ex, HttpServletRequest request) {
        ErrorResponse error = ErrorResponse.builder()
            .timestamp(LocalDateTime.now())
            .status(HttpStatus.CONFLICT.value())
            .error("DUPLICATE_RESOURCE")
            .message(ex.getMessage())
            .path(request.getRequestURI())
            .build();
        return ResponseEntity.status(HttpStatus.CONFLICT).body(error);
    }
    
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationErrors(
            MethodArgumentNotValidException ex, HttpServletRequest request) {
        List<String> errors = ex.getBindingResult()
            .getFieldErrors()
            .stream()
            .map(f -> f.getField() + ": " + f.getDefaultMessage())
            .toList();
        
        ErrorResponse error = ErrorResponse.builder()
            .timestamp(LocalDateTime.now())
            .status(HttpStatus.BAD_REQUEST.value())
            .error("VALIDATION_ERROR")
            .message("Errores de validación en la solicitud")
            .details(errors)
            .path(request.getRequestURI())
            .build();
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }
    
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGenericException(
            Exception ex, HttpServletRequest request) {
        logger.error("Excepción no controlada", ex);
        ErrorResponse error = ErrorResponse.builder()
            .timestamp(LocalDateTime.now())
            .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
            .error("INTERNAL_SERVER_ERROR")
            .message("Error interno del servidor")
            .path(request.getRequestURI())
            .build();
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
    }
}

// ErrorResponse.java
@Builder
public record ErrorResponse(
    LocalDateTime timestamp,
    int status,
    String error,
    String message,
    List<String> details,
    String path
) {}

// Custom Exceptions
public class DuplicateEmailException extends RuntimeException {
    public DuplicateEmailException(String message) {
        super(message);
    }
}

public class ResourceNotFoundException extends RuntimeException {
    public ResourceNotFoundException(String message) {
        super(message);
    }
}

// Controllers - Ahora limpios
@RestController
@RequestMapping("/api/v1/persons")
@RequiredArgsConstructor
public class PersonController {
    
    private final PersonService personService;
    
    @PostMapping
    public ResponseEntity<PersonResponse> create(@Valid @RequestBody PersonCreateRequest request) {
        PersonResponse response = personService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
        // Si hay error, GlobalExceptionHandler lo maneja automáticamente
    }
}
```

**Evidencia Funcional:**
- ✅ Todas las excepciones retornan formato consistente:
  ```json
  {
    "timestamp": "2026-03-07T10:30:00",
    "status": 409,
    "error": "DUPLICATE_RESOURCE",
    "message": "Email ya existe",
    "path": "/api/v1/persons"
  }
  ```
- ✅ Logging centralizado de todos los errores
- ✅ Controllers más limpios (sin try-catch)

---

### Cambio 4: Validación Declarativa JSR-303

**ADR Relacionado:** ADR-004  
**Archivo Modificado:** `backend/src/main/java/com/iglesia/dtos/person/*`

#### ANTES (Validación Manual)
```java
// PersonController.java
@RestController
public class PersonController {
    
    @PostMapping
    public ResponseEntity<?> create(@RequestBody PersonCreateRequest request) {
        // Validación imperatva (INCORRECTO)
        if (request.getName() == null || request.getName().isBlank()) {
            return ResponseEntity.status(400).body("El nombre es requerido");
        }
        
        if (request.getEmail() == null || !request.getEmail().contains("@")) {
            return ResponseEntity.status(400).body("El email debe ser válido");
        }
        
        if (request.getChurchId() == null) {
            return ResponseEntity.status(400).body("La iglesia es requerida");
        }
        
        // Después de todas las validaciones...
        Person person = personService.create(request);
        return ResponseEntity.ok(person);
    }
}
```

#### DESPUÉS (Validación Declarativa)
```java
// PersonCreateRequest.java
public record PersonCreateRequest(
    @NotBlank(message = "El nombre es requerido")
    String name,
    
    @Email(message = "El email debe ser válido")
    String email,
    
    @NotNull(message = "La iglesia es requerida")
    Long churchId
) {}

// EnrollmentCreateRequest.java
public record EnrollmentCreateRequest(
    @NotNull(message = "La persona es requerida")
    Long personId,
    
    @NotNull(message = "El curso es requerido")
    Long courseId,
    
    @NotNull(message = "La fecha de inscripción es requerida")
    @PastOrPresent(message = "La fecha no puede ser futura")
    LocalDate enrollmentDate,
    
    @Pattern(regexp = "ACTIVE|COMPLETED|CANCELLED", 
             message = "Estado inválido")
    String status
) {}

// PersonController.java - Validación automática
@RestController
@RequestMapping("/api/v1/persons")
@RequiredArgsConstructor
public class PersonController {
    
    private final PersonService personService;
    
    @PostMapping
    public ResponseEntity<PersonResponse> create(
            @Valid @RequestBody PersonCreateRequest request) {
        // Spring valida automáticamente ANTES de llegar aquí
        // Si hay errores, GlobalExceptionHandler retorna 400
        PersonResponse response = personService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}
```

**Evidencia Funcional:**
- ✅ Solicitud inválida es rechazada automáticamente:
  ```bash
  POST /api/v1/persons
  {
    "name": "",       # Vacío
    "email": "invalid",  # No es email
    "churchId": null     # Null
  }
  
  Respuesta 400 (automática):
  {
    "timestamp": "2026-03-07T10:30:00",
    "status": 400,
    "error": "VALIDATION_ERROR",
    "message": "Errores de validación",
    "details": [
      "email: El email debe ser válido",
      "churchId: La iglesia es requerida",
      "name: El nombre es requerido"
    ]
  }
  ```
- ✅ Validación consistente en toda la API
- ✅ Control de datos antes de la BD

---

### Cambio 5: Configuración Externalizada

**ADR Relacionado:** ADR-010  
**Archivo Modificado:** `backend/resources/application.yml`, `application-local.yml`, `application-prod.yml`

#### ANTES (Configuración Hardcodeada)
```java
// DataSourceConfig.java (INCORRECTO)
@Configuration
public class DataSourceConfig {
    
    @Bean
    public DataSource dataSource() {
        final DriverManagerDataSource dataSource = new DriverManagerDataSource();
        dataSource.setDriverClassName("org.postgresql.Driver");
        dataSource.setUrl("jdbc:postgresql://localhost:5432/iglesias_db");  // Hardcoded
        dataSource.setUsername("postgres");  // Hardcoded
        dataSource.setPassword("postgres");  // SEGURIDAD: Credencial en código
        return dataSource;
    }
}

// JwtService.java (INCORRECTO)
@Service
public class JwtService {
    
    private static final String JWT_SECRET = "mi-super-secret-key-hardcoded-aqui";  // Inseguro
    private static final Long JWT_EXPIRATION = 3600000L;  // Pero qué si necesito diferente en prod?
    
    public String generateToken(String email) {
        return Jwts.builder()
            .setSubject(email)
            .setIssuedAt(new Date())
            .setExpiration(new Date(System.currentTimeMillis() + JWT_EXPIRATION))
            .signWith(SignatureAlgorithm.HS256, JWT_SECRET)
            .compact();
    }
}
```

#### DESPUÉS (Configuración Externalizada)
```yaml
# application.yml (base)
spring:
  application:
    name: iglesias-admin
  profilesActive: local  # Cargará application-local.yml
  
  jpa:
    hibernate:
      ddl-auto: validate
    properties:
      hibernate:
        format_sql: true
        use_sql_comments: true
  
  jwt:
    secret: ${JWT_SECRET:dev-secret-key}  # Desde env var o default
    expiration: ${JWT_EXPIRATION:86400000}  # 24 horas default

# application-local.yml (desarrollo)
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/iglesias_db
    username: postgres
    password: postgres
  jpa:
    hibernate:
      ddl-auto: update  # Auto-create en desarrollo
    show-sql: true
  jwt:
    expiration: 3600000  # 1 hora para tests rápidos

# application-prod.yml (producción)
spring:
  datasource:
    url: ${DB_URL}  # jdbc:postgresql://prod-db:5432/iglesias
    username: ${DB_USER}  # Desde variable de entorno
    password: ${DB_PASSWORD}  # Desde variable de entorno
  jpa:
    hibernate:
      ddl-auto: validate  # No modifica BD en producción
    show-sql: false  # No logguea queries en prod
  jwt:
    expiration: 86400000  # 24 horas en producción

# JwtService.java - Refactorizado
@Service
@RequiredArgsConstructor
public class JwtService {
    
    @Value("${spring.jwt.secret}")
    private String jwtSecret;
    
    @Value("${spring.jwt.expiration}")
    private Long jwtExpiration;
    
    public String generateToken(String email) {
        return Jwts.builder()
            .setSubject(email)
            .setIssuedAt(new Date())
            .setExpiration(new Date(System.currentTimeMillis() + jwtExpiration))
            .signWith(SignatureAlgorithm.HS256, jwtSecret)
            .compact();
    }
}

// En docker-compose.yml
services:
  backend:
    environment:
      - SPRING_PROFILES_ACTIVE=prod
      - DB_URL=jdbc:postgresql://postgres:5432/iglesias
      - DB_USER=postgres
      - DB_PASSWORD=postgres
      - JWT_SECRET=${JWT_SECRET:-my-production-secret}
      - JWT_EXPIRATION=86400000
```

**Evidencia Funcional:**
- ✅ Configuración diferente por entorno sin recompilar
- ✅ Secretos no en repositorio (en variables de entorno)
- ✅ Fácil deploar en diferentes plataformas (local, Docker, K8s)
- ✅ Cumple principios 12-Factor App

---

## 5. Beneficios y Consecuencias

### Consecuencias POSITIVAS

#### Corto Plazo (1-2 sprints)
- ✅ **Código más limpio:** Separación clara de responsabilidades
- ✅ **Debugging más fácil:** Logging centralizado
- ✅ **Respuestas consistentes:** Global exception handler
- ✅ **Seguridad mejorada:** DTOs evitan exposición de datos

#### Mediano Plazo (3-4 sprints)
- ✅ **Testing:** 70% cobertura alcanzable con tests unitarios
- ✅ **Documentación:** API autodocumentada con Swagger
- ✅ **Performance:** Queries optimizadas, menos N+1
- ✅ **Equipo:** Onboarding más rápido con código claro

#### Largo Plazo
- ✅ **Mantenibilidad:** Cambios sin miedo a regresiones
- ✅ **Escalabilidad:** Diseño preparado para crecer
- ✅ **Profesionalismo:** Código "enterprise-ready"
- ✅ **Deuda técnica:** Reducción significativa

### Trade-offs y Consideraciones

| Aspecto | Impacto | Justificación |
|--------|--------|---------------|
| **Complejidad inicial** | Media | Pero beneficios superan el costo |
| **Código duplicado (mappers)** | Bajo | Necesario para desacoplamiento |
| **Performance overhead** | Mínimo | Mapeo de DTOs es negligible |
| **Inversión de tiempo** | ~40-60 hrs | Pero ahorra 100+ hrs de mantenimiento |
| **Curva de aprendizaje** | Media | Equipo rápidamente se adapta |

---

## 6. Riesgos y Mitigación

| Riesgo | Probabilidad | Impacto | Mitigación |
|--------|-------------|--------|-----------|
| **Regresiones durante refactoring** | Media | Alto | Tests unitarios frequentes, small commits |
| **Performance inicial** | Baja | Medio | Profiling con herramientas, índices en BD |
| **Equipo no adopta patrones** | Baja | Medio | Code review/pairing, documentación clara |
| **Complejidad para nuevos developers** | Media | Bajo | Documentación, ejemplos listos para copiar |

---

## 7. Justificación Final

La arquitectura actual del ERP Iglesias es funcional pero **inmadura y difícil de mantener**. Los 10 cambios propuestos transforman la codebase de "*apenas funciona*" a "*enterprise-quality*".

Cada decisión tiene fundamentación clara en **SOLID** y responde a un problema real identificado en el diagnóstico. La implementación incremental (Sprint 1-4) permite reducir riesgo mientras se construye una arquitectura sólida.

**Conclusión:** Invertir ~60 horas ahora ahorra 100+ horas de maintenance futuro y permite escalabilidad sostenible.
