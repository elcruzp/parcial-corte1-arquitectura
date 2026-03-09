package com.iglesia;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.Valid;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.stereotype.Service;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * Controlador REST para gestión de personas.
 * Implementa ADR-001 (Service Layer), ADR-002 (DTO + Mapper Pattern),
 * ADR-003 (Global Exception Handler), ADR-004 (JSR-303 Validation),
 * ADR-005 (Logging Centralizado con SLF4J) y ADR-010 (Configuración Externalizada).
 *
 * CAMBIO 5 IMPLEMENTADO: Configuración Externalizada (ADR-010)
 * - application.yml: Configuración base con variables de entorno
 * - application-local.yml: Desarrollo con logging detallado
 * - application-prod.yml: Producción con configuración segura
 * - JwtConfig.java: Clase de configuración tipada para JWT
 * - Variables de entorno: DB_URL, JWT_SECRET, SPRING_PROFILES_ACTIVE, etc.
 */
@RestController
@RequestMapping("/api/people")
public class PersonController {
    // ========================================================================
    // IMPLEMENTACIÓN ADR-001: SERVICE LAYER PATTERN
    // ========================================================================

    /*
    // ========================================================================
    // CÓDIGO ANTIGUO - ADR-001 (MALAS PRÁCTICAS)
    // ========================================================================
     Problema: Acoplamiento directo entre controlador y repositorios.
     Violación de SRP (Single Responsibility Principle): El controlador maneja
     lógica de negocio además de HTTP.
     Violación de DIP (Dependency Inversion Principle): Depende de concretos, no de abstracciones.
    // ========================================================================

    private final PersonRepository personRepository;
    private final ChurchRepository churchRepository;

    public PersonController(PersonRepository personRepository, ChurchRepository churchRepository) {
        this.personRepository = personRepository;
        this.churchRepository = churchRepository;
    }

    @PreAuthorize("hasRole('ADMIN') or hasRole('CLIENT')")
    @PostMapping
    public PersonResponse create(@RequestBody PersonRequest request) {
        Church church = requireChurch();
        Person person = new Person();
        person.setFirstName(request.firstName());
        person.setLastName(request.lastName());
        person.setDocument(request.document());
        person.setPhone(request.phone());
        person.setEmail(request.email());
        person.setChurch(church);
        personRepository.save(person);
        return PersonResponse.from(person);
    }

    @PreAuthorize("hasRole('ADMIN') or hasRole('CLIENT')")
    @GetMapping
    public List<PersonResponse> list() {
        Church church = requireChurch();
        return personRepository.findAllByChurchId(church.getId())
            .stream()
            .map(PersonResponse::from)
            .toList();
    }

    private Church requireChurch() {
        return churchRepository.findAll()
            .stream()
            .findFirst()
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Debe registrar una iglesia primero"));
    }

    // ========================================================================
    // FIN CÓDIGO ANTIGUO - ADR-001
    // ========================================================================
    */

    // ========================================================================
    // CÓDIGO NUEVO - ADR-001: SERVICE LAYER PATTERN
    // ========================================================================
    // Mejora: Separación de responsabilidades. El controlador solo maneja HTTP,
    // la lógica de negocio se delega a un servicio (PersonService).
    // Cumple SRP: Controlador = HTTP, Servicio = Lógica de negocio.
    // Cumple DIP: Controlador depende de interfaz PersonService, no de concretos.
    // ========================================================================

    private final PersonService personService;

    public PersonController(PersonService personService) {
        this.personService = personService;
    }

    @PreAuthorize("hasRole('ADMIN') or hasRole('CLIENT')")
    @PostMapping
    public PersonResponseDTO create(@Valid @RequestBody PersonCreateRequest request) {
        return personService.create(request);
    }

    @PreAuthorize("hasRole('ADMIN') or hasRole('CLIENT')")
    @GetMapping
    public List<PersonResponseDTO> list() {
        return personService.list();
    }

    // ========================================================================
    // FIN IMPLEMENTACIÓN ADR-001
    // ========================================================================

    // ========================================================================
    // IMPLEMENTACIÓN ADR-002: DTO + MAPPER PATTERN
    // ========================================================================

    /*
    // ========================================================================
    // CÓDIGO ANTIGUO - ADR-002 (MALAS PRÁCTICAS)
    // ========================================================================
     Problema: Exposición directa de entidades JPA en la API.
     Violación de ISP (Interface Segregation Principle): API expone más de lo necesario.
     Violación de DIP: API acoplada directamente al esquema de BD.
     Sin validación declarativa de entrada.
    // ========================================================================

    public record PersonRequest(
        @NotBlank String firstName,
        @NotBlank String lastName,
        String document,
        String phone,
        String email
    ) {}

    public record PersonResponse(
        Long id,
        String firstName,
        String lastName,
        String document,
        String phone,
        String email
    ) {
        public static PersonResponse from(Person person) {
            return new PersonResponse(
                person.getId(),
                person.getFirstName(),
                person.getLastName(),
                person.getDocument(),
                person.getPhone(),
                person.getEmail()
            );
        }
    }

    // ========================================================================
    // FIN CÓDIGO ANTIGUO - ADR-002
    // ========================================================================
    */

    // ========================================================================
    // CÓDIGO NUEVO - ADR-002: DTO + MAPPER PATTERN
    // ========================================================================
    // Mejora: DTOs segregados desacoplan API de BD.
    // Validación declarativa con JSR-303.
    // Mapper centraliza conversiones Entity ↔ DTO.
    // Cumple ISP: Solo expone campos necesarios.
    // Cumple DIP: Cambios en BD no afectan API.
    // ========================================================================

    /**
     * DTO para solicitud de creación de persona.
     * Incluye validaciones declarativas (JSR-303/Jakarta) para garantizar datos válidos
     * antes de que lleguen a la capa de servicio.
     * ADR-004: Validación declarativa reemplaza validación imperativa.
     */
    public record PersonCreateRequest(
        @NotBlank(message = "El nombre es requerido")
        @Size(min = 2, max = 50, message = "El nombre debe tener entre 2 y 50 caracteres")
        String firstName,

        @NotBlank(message = "El apellido es requerido")
        @Size(min = 2, max = 50, message = "El apellido debe tener entre 2 y 50 caracteres")
        String lastName,

        @NotBlank(message = "El documento es requerido")
        @Pattern(regexp = "^[0-9]{8,12}$", message = "El documento debe contener solo números (8-12 dígitos)")
        String document,

        @NotBlank(message = "El teléfono es requerido")
        @Pattern(regexp = "^[0-9+\\-\\s()]{7,15}$", message = "El teléfono debe tener un formato válido")
        String phone,

        @Email(message = "El email debe ser válido")
        @NotBlank(message = "El email es requerido")
        String email,

        @NotNull(message = "La iglesia es requerida")
        Long churchId
    ) {}

    /**
     * DTO anidado para información resumida de iglesia (solo id y nombre).
     * Previene la exposición de información innecesaria de iglesia en respuestas.
     */
    public record ChurchSummary(
        Long id,
        String name
    ) {}

    /**
     * DTO para respuesta que retorna información de persona.
     * Segrega qué campos se exponen a la API, desacoplando de la entidad JPA.
     * Permite cambiar la BD sin afectar el contrato de API.
     */
    public record PersonResponseDTO(
        Long id,
        String firstName,
        String lastName,
        String document,
        String phone,
        String email,
        ChurchSummary church,
        LocalDateTime createdAt
    ) {}

    /**
     * Componente responsable de mapear entre entidades JPA y DTOs.
     * Centraliza la lógica de conversión y permite cambios futuros sin afectar
     * servicios ni controladores. Implementa el patrón Mapper.
     */
    @Component
    public static class PersonMapper {

        /**
         * Convierte un DTO de creación en una entidad Person.
         * Nota: churchId se maneja en el servicio.
         */
        public Person toPerson(PersonCreateRequest request) {
            Person person = new Person();
            person.setFirstName(request.firstName());
            person.setLastName(request.lastName());
            person.setDocument(request.document());
            person.setPhone(request.phone());
            person.setEmail(request.email());
            return person;
        }

        /**
         * Convierte una entidad Person en un DTO de respuesta.
         * Segrega información y evita exposición de campos internos.
         */
        public PersonResponseDTO toResponseDTO(Person person) {
            ChurchSummary churchSummary = null;
            if (person.getChurch() != null) {
                churchSummary = new ChurchSummary(
                    person.getChurch().getId(),
                    person.getChurch().getName()
                );
            }

            return new PersonResponseDTO(
                person.getId(),
                person.getFirstName(),
                person.getLastName(),
                person.getDocument(),
                person.getPhone(),
                person.getEmail(),
                churchSummary,
                person.getCreatedAt()
            );
        }

        /**
         * Convierte una lista de personas en DTOs de respuesta.
         */
        public List<PersonResponseDTO> toResponseDTOList(List<Person> persons) {
            return persons.stream()
                    .map(this::toResponseDTO)
                    .toList();
        }
    }

    // ========================================================================
    // FIN IMPLEMENTACIÓN ADR-002
    // ========================================================================

    // ========================================================================
    // EXCEPCIONES PERSONALIZADAS (PARTE DE ADR-003)
    // ========================================================================

    /**
     * Excepciones personalizadas para errores específicos de negocio.
     * Permiten manejo granular en el GlobalExceptionHandler.
     */
    public static class DuplicateResourceException extends RuntimeException {
        public DuplicateResourceException(String message) {
            super(message);
        }
    }

    public static class ResourceNotFoundException extends RuntimeException {
        public ResourceNotFoundException(String message) {
            super(message);
        }
    }

    // ========================================================================
    // CLASES DE SERVICIO (PARTE DE ADR-001, USADAS TAMBIÉN EN ADR-002 Y ADR-003)
    // ========================================================================

    /**
     * Interfaz que define la API del servicio de personas. Desacopla el
     * controlador de los repositorios (principio DIP).
     */
    public interface PersonService {
        PersonResponseDTO create(PersonCreateRequest request);
        List<PersonResponseDTO> list();
    }

    @Service
    @Transactional
    public static class PersonServiceImpl implements PersonService {

        // Logger centralizado (ADR-005)
        private static final Logger logger = LoggerFactory.getLogger(PersonServiceImpl.class);

        private final PersonRepository personRepository;
        private final ChurchRepository churchRepository;
        private final PersonMapper personMapper;  // Inyección de mapper (ADR-002)

        public PersonServiceImpl(PersonRepository personRepository,
                                 ChurchRepository churchRepository,
                                 PersonMapper personMapper) {
            this.personRepository = personRepository;
            this.churchRepository = churchRepository;
            this.personMapper = personMapper;
        }

        @Override
        public PersonResponseDTO create(PersonCreateRequest request) {
            // Logging: Inicio de operación (ADR-005)
            logger.info("Iniciando creación de persona con email: {}", request.email());

            try {
                // Lógica de negocio movida del controlador (ADR-001)
                Church church = requireChurch();
                logger.debug("Iglesia obtenida: {} (ID: {})", church.getName(), church.getId());

                // Validación de reglas de negocio (ADR-004)
                validateBusinessRules(request);
                logger.debug("Validaciones de negocio completadas para email: {}", request.email());

                // Conversión con mapper (ADR-002: DTO -> Entity)
                Person person = personMapper.toPerson(request);
                person.setChurch(church);
                logger.debug("Persona mapeada desde DTO: {} {}", person.getFirstName(), person.getLastName());

                Person saved = personRepository.save(person);
                logger.info("Persona creada exitosamente con ID: {} y email: {}", saved.getId(), saved.getEmail());

                // Retorna DTO mapeado de la entidad (ADR-002: Entity -> DTO Response)
                PersonResponseDTO response = personMapper.toResponseDTO(saved);
                logger.debug("Respuesta DTO preparada para persona ID: {}", saved.getId());

                return response;

            } catch (Exception e) {
                // Logging: Error en operación (ADR-005)
                logger.error("Error al crear persona con email: {}. Causa: {}", request.email(), e.getMessage(), e);
                throw e; // Re-lanzar para que GlobalExceptionHandler lo maneje
            }
        }

        /**
         * Validación adicional de negocio: Verificar email duplicado.
         * Esta validación no puede ser declarativa porque requiere acceso a BD,
         * pero se centraliza aquí para mantener consistencia.
         * 
         * NOTA: PersonRepository debe tener el método existsByEmail(String) o findByEmail(String)
         * Si usa findByEmail, cambiar a: personRepository.findByEmail(request.email()).isPresent()
         */
        private void validateBusinessRules(PersonCreateRequest request) {
            // Verificar email duplicado (regla de negocio)
            // Este método debería estar definido en PersonRepository
            // Opción 1: Si PersonRepository tiene existsByEmail (recomendado)
            if (personRepository.existsByEmail(request.email())) {
                throw new DuplicateResourceException("Ya existe una persona con este email");
            }
            
            /* Opción 2: Si PersonRepository tiene findByEmail (alternativa)
            if (personRepository.findByEmail(request.email()).isPresent()) {
                throw new DuplicateResourceException("Ya existe una persona con este email");
            }
            */

            // Aquí se pueden agregar más validaciones de negocio en el futuro
            // sin modificar el controlador ni el DTO
        }

        @Override
        public List<PersonResponseDTO> list() {
            // Logging: Inicio de operación (ADR-005)
            logger.info("Iniciando consulta de lista de personas");

            try {
                Church church = requireChurch();
                logger.debug("Iglesia obtenida para listado: {} (ID: {})", church.getName(), church.getId());

                List<Person> persons = personRepository.findAllByChurchId(church.getId());
                logger.info("Encontradas {} personas para la iglesia {}", persons.size(), church.getName());

                // Mapeo de lista de entidades a DTOs (ADR-002)
                List<PersonResponseDTO> response = personMapper.toResponseDTOList(persons);
                logger.debug("Lista de personas mapeada a DTOs: {} registros", response.size());

                return response;

            } catch (Exception e) {
                // Logging: Error en operación (ADR-005)
                logger.error("Error al consultar lista de personas. Causa: {}", e.getMessage(), e);
                throw e; // Re-lanzar para que GlobalExceptionHandler lo maneje
            }
        }

        private Church requireChurch() {
            logger.debug("Obteniendo iglesia por defecto del sistema");

            Church church = churchRepository.findAll()
                .stream()
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("Debe registrar una iglesia primero"));

            logger.debug("Iglesia encontrada: {} (ID: {})", church.getName(), church.getId());
            return church;
        }
    }

    // ========================================================================
    // FIN CLASES DE SERVICIO
    // ========================================================================

    // ========================================================================
    // IMPLEMENTACIÓN ADR-003: GLOBAL EXCEPTION HANDLER (CENTRALIZACIÓN DE ERRORES)
    // ========================================================================

    /*
    // ========================================================================
    // CÓDIGO ANTIGUO - ADR-003 (MALAS PRÁCTICAS)
    // ========================================================================
     Problema: Manejo inconsistente de errores en controladores.
     Violación de SRP: Controladores manejan lógica de negocio Y errores HTTP.
     Violación de OCP: Cambios en manejo de errores requieren modificar cada controlador.
     Consecuencia: Respuestas inconsistentes, código duplicado, difícil mantenimiento.
    // ========================================================================

    // Ejemplo de cómo se manejaban errores ANTES (en el controlador):
    @PreAuthorize("hasRole('ADMIN') or hasRole('CLIENT')")
    @PostMapping("/old-create")
    public PersonResponseDTO oldCreate(@RequestBody PersonCreateRequest request) {
        try {
            // Lógica de negocio mezclada con manejo de errores
            if (request.firstName() == null || request.firstName().isBlank()) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El nombre es requerido");
            }

            Church church = churchRepository.findAll()
                .stream()
                .findFirst()
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Debe registrar una iglesia primero"));

            Person person = new Person();
            person.setFirstName(request.firstName());
            person.setLastName(request.lastName());
            person.setDocument(request.document());
            person.setPhone(request.phone());
            person.setEmail(request.email());
            person.setChurch(church);

            Person saved = personRepository.save(person);
            return personMapper.toResponseDTO(saved);

        } catch (ResponseStatusException e) {
                 Manejo manual de errores (duplicado en cada controlador)
            throw e; // O retornar ResponseEntity con formato inconsistente
        } catch (Exception e) {
                 Manejo genérico inconsistente
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error interno");
        }
    }

    // ========================================================================
    // FIN CÓDIGO ANTIGUO - ADR-003
    // ========================================================================
    */

    // ========================================================================
    // CÓDIGO NUEVO - ADR-003: GLOBAL EXCEPTION HANDLER PATTERN
    // ========================================================================
    // Mejora: Centralización de manejo de errores en un solo lugar.
    // Cumple SRP: Un solo responsable del formato de respuestas de error.
    // Cumple OCP: Nuevos tipos de error se agregan sin modificar controladores existentes.
    // Cumple DIP: Controladores dependen de abstracciones (excepciones), no de detalles HTTP.
    // Beneficios: Respuestas consistentes, logging centralizado, controladores limpios.
    // ========================================================================

    /**
     * DTO para respuestas de error consistentes en toda la API.
     */
    public record ErrorResponse(
        LocalDateTime timestamp,
        int status,
        String error,
        String message,
        String path
    ) {}

    // ========================================================================
    // FIN IMPLEMENTACIÓN ADR-003
    // ========================================================================

    // ========================================================================
    // IMPLEMENTACIÓN ADR-004: JSR-303/JAKARTA VALIDATION (VALIDACIÓN DECLARATIVA)
    // ========================================================================

    /*
    // ========================================================================
    // CÓDIGO ANTIGUO - ADR-004 (MALAS PRÁCTICAS)
    // ========================================================================
    // Problema: Validación imperativa dispersa en controladores y servicios.
    // Violación de SRP: Controladores manejan validación además de HTTP.
    // Violación de OCP: Agregar validaciones requiere modificar código existente.
    // Consecuencia: Código duplicado, difícil mantenimiento, validaciones inconsistentes.
    // ========================================================================

    // Ejemplo de validación manual ANTES (en el servicio):
    @Override
    public PersonResponseDTO create(PersonCreateRequest request) {
        // Validación imperativa (INCORRECTO)
        if (request.firstName() == null || request.firstName().trim().isEmpty()) {
            throw new IllegalArgumentException("El nombre es requerido");
        }
        if (request.lastName() == null || request.lastName().trim().isEmpty()) {
            throw new IllegalArgumentException("El apellido es requerido");
        }
        if (request.document() == null || request.document().trim().isEmpty()) {
            throw new IllegalArgumentException("El documento es requerido");
        }
        if (request.email() == null || !request.email().contains("@")) {
            throw new IllegalArgumentException("El email debe ser válido");
        }
        if (request.churchId() == null) {
            throw new IllegalArgumentException("La iglesia es requerida");
        }

        // Verificar duplicados manualmente
        if (personRepository.existsByEmail(request.email())) {
            throw new IllegalArgumentException("Email ya existe");
        }

        // Después de TODAS las validaciones...
        Church church = requireChurch();
        Person person = personMapper.toPerson(request);
        person.setChurch(church);
        Person saved = personRepository.save(person);
        return personMapper.toResponseDTO(saved);
    }

    // ========================================================================
    // FIN CÓDIGO ANTIGUO - ADR-004
    // ========================================================================
    */

    // ========================================================================
    // FIN IMPLEMENTACIÓN ADR-004
    // ========================================================================
}

// ========================================================================
// INTERFACES DE REPOSITORIO (PARTE DE ADR-001 Y ADR-004)
// ========================================================================
/**
 * Repositorio para operaciones de BD con entidades Person.
 * Extiende JpaRepository para operaciones CRUD básicas.
 */
@Repository
interface PersonRepository extends JpaRepository<Person, Long> {
    // Método para verificar existencia de email (usado en ADR-004)
    boolean existsByEmail(String email);
    
    // Método para listar personas por iglesia (usado en ADR-001)
    List<Person> findAllByChurchId(Long churchId);
}

/**
 * Repositorio para operaciones de BD con entidades Church.
 */
@Repository
interface ChurchRepository extends JpaRepository<Church, Long> {
    // Métodos básicos heredados de JpaRepository
}

// ========================================================================
// IMPLEMENTACIÓN ADR-005: LOGGING CENTRALIZADO CON SLF4J
// ========================================================================

/*
 // ========================================================================
 // CÓDIGO ANTIGUO - ADR-005 (MALAS PRÁCTICAS)
 // ========================================================================
 // Problema: Sin logging o logging inconsistente.
 // Violación de SRP: Código de negocio mezclado con debugging.
 // Consecuencia: Difícil debugging en producción, no trazabilidad.
 // ========================================================================

 // Ejemplo de código ANTES (sin logging):
 @Override
 public PersonResponseDTO create(PersonCreateRequest request) {
     Church church = requireChurch();

     validateBusinessRules(request);

     Person person = personMapper.toPerson(request);
     person.setChurch(church);

     Person saved = personRepository.save(person);

     return personMapper.toResponseDTO(saved);
     // Sin logging: No sabemos qué pasó, cuándo, ni por qué falló
 }

 @Override
 public List<PersonResponseDTO> list() {
     Church church = requireChurch();
     List<Person> persons = personRepository.findAllByChurchId(church.getId());
     return personMapper.toResponseDTOList(persons);
     // Sin logging: No sabemos si se ejecutó, cuántos registros retornó
 }

 // ========================================================================
 // FIN CÓDIGO ANTIGUO - ADR-005
 // ========================================================================
*/

/*
 // ========================================================================
 // CÓDIGO NUEVO - ADR-005: LOGGING CENTRALIZADO CON SLF4J PATTERN
 // ========================================================================
 // Mejora: Logging estructurado con SLF4J + Logback.
 // Cumple SRP: Logging separado de lógica de negocio.
 // Beneficios: Trazabilidad completa, debugging en producción, auditoría.
 // ========================================================================
*/

// ========================================================================
// GLOBAL EXCEPTION HANDLER (PARTE DE ADR-003)
// ========================================================================
/**
 * Global Exception Handler: Centraliza el manejo de todas las excepciones.
 * Aplica el patrón Aspect-Oriented Programming para separar concerns.
 * Ubicado fuera de PersonController para mejor organización y compatibilidad con IDEs.
 */
@ControllerAdvice
class GlobalExceptionHandler {

    @ExceptionHandler(PersonController.DuplicateResourceException.class)
    public ResponseEntity<PersonController.ErrorResponse> handleDuplicateResource(
            PersonController.DuplicateResourceException ex, HttpServletRequest request) {
        PersonController.ErrorResponse error = new PersonController.ErrorResponse(
            LocalDateTime.now(),
            HttpStatus.CONFLICT.value(),
            "DUPLICATE_RESOURCE",
            ex.getMessage(),
            request.getRequestURI()
        );
        return ResponseEntity.status(HttpStatus.CONFLICT).body(error);
    }

    @ExceptionHandler(PersonController.ResourceNotFoundException.class)
    public ResponseEntity<PersonController.ErrorResponse> handleResourceNotFound(
            PersonController.ResourceNotFoundException ex, HttpServletRequest request) {
        PersonController.ErrorResponse error = new PersonController.ErrorResponse(
            LocalDateTime.now(),
            HttpStatus.NOT_FOUND.value(),
            "RESOURCE_NOT_FOUND",
            ex.getMessage(),
            request.getRequestURI()
        );
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<PersonController.ErrorResponse> handleGenericException(
            Exception ex, HttpServletRequest request) {
        PersonController.ErrorResponse error = new PersonController.ErrorResponse(
            LocalDateTime.now(),
            HttpStatus.INTERNAL_SERVER_ERROR.value(),
            "INTERNAL_SERVER_ERROR",
            "Error interno del servidor",
            request.getRequestURI()
        );
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
    }
}
