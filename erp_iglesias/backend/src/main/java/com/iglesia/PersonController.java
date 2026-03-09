package com.iglesia;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
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
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

/**
 * Controlador REST para gestión de personas.
 * Implementa ADR-001 (Service Layer) y ADR-002 (DTO + Mapper Pattern).
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
     * Incluye validaciones declarativas (JSR-303) para garantizar datos válidos
     * antes de que lleguen a la capa de servicio.
     */
    public record PersonCreateRequest(
        @NotBlank(message = "El nombre es requerido")
        String firstName,

        @NotBlank(message = "El apellido es requerido")
        String lastName,

        @NotBlank(message = "El documento es requerido")
        String document,

        @NotBlank(message = "El teléfono es requerido")
        String phone,

        @Email(message = "El email debe ser válido")
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
            // Lógica de negocio movida del controlador (ADR-001)
            Church church = requireChurch();

            // Conversión con mapper (ADR-002: DTO -> Entity)
            Person person = personMapper.toPerson(request);
            person.setChurch(church);

            Person saved = personRepository.save(person);

            // Retorna DTO mapeado de la entidad (ADR-002: Entity -> DTO Response)
            return personMapper.toResponseDTO(saved);
        }

        @Override
        public List<PersonResponseDTO> list() {
            Church church = requireChurch();
            List<Person> persons = personRepository.findAllByChurchId(church.getId());

            // Mapeo de lista de entidades a DTOs (ADR-002)
            return personMapper.toResponseDTOList(persons);
        }

        private Church requireChurch() {
            return churchRepository.findAll()
                .stream()
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("Debe registrar una iglesia primero"));
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
}

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
