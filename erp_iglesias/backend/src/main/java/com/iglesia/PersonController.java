package com.iglesia;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.stereotype.Service;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import jakarta.validation.Valid;

import java.util.List;
import java.time.LocalDateTime;

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
    // Problema: Acoplamiento directo entre controlador y repositorios.
    // Violación de SRP (Single Responsibility Principle): El controlador maneja
    // lógica de negocio además de HTTP.
    // Violación de DIP (Dependency Inversion Principle): Depende de concretos,
    // no de abstracciones.
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
    // Problema: Exposición directa de entidades JPA en la API.
    // Violación de ISP (Interface Segregation Principle): API expone más de lo necesario.
    // Violación de DIP: API acoplada directamente al esquema de BD.
    // Sin validación declarativa de entrada.
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
    // CLASES DE SERVICIO (PARTE DE ADR-001, USADAS TAMBIÉN EN ADR-002)
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
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Debe registrar una iglesia primero"));
        }
    }

    // ========================================================================
    // FIN CLASES DE SERVICIO
    // ========================================================================
}
