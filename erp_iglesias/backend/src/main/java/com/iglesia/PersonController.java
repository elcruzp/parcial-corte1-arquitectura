package com.iglesia;

import jakarta.validation.constraints.NotBlank;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import jakarta.validation.Valid;

import java.util.List;

@RestController
@RequestMapping("/api/people")
public class PersonController {
    /*
    --- Código antiguo con malas prácticas (acoplamiento directo al repositorio y lógica en el controlador) ---
    
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
    // -----------------------------------------------------------------------------------------------
    */

    // Nuevo diseño: delega la lógica de negocio a un servicio específico
    private final PersonService personService;

    public PersonController(PersonService personService) {
        this.personService = personService;
    }

    @PreAuthorize("hasRole('ADMIN') or hasRole('CLIENT')")
    @PostMapping
    public PersonResponse create(@Valid @RequestBody PersonRequest request) {
        return personService.create(request);
    }

    @PreAuthorize("hasRole('ADMIN') or hasRole('CLIENT')")
    @GetMapping
    public List<PersonResponse> list() {
        return personService.list();
    }

    /*
         antiguo: la lógica para obtener la iglesia estaba en el controlador.
        ahora forma parte de la capa de servicio, de modo que el controlador
        sólo se preocupa de manejar peticiones HTTP.

    private Church requireChurch() {
        return churchRepository.findAll()
            .stream()
            .findFirst()
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Debe registrar una iglesia primero"));
    }
    */

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

    // ----------------------------------------------------------------------
    // Capa de servicio (implementación del primer cambio propuesto)
    // Las siguientes clases se incluyen aquí por simplicidad, pero en un
    // proyecto real estarían en archivos separados bajo el paquete
    // ``com.iglesia.services``.
    
    /**
     * Interfaz que define la API del servicio de personas. Desacopla el
     * controlador de los repositorios (principio DIP).
     */
    public interface PersonService {
        PersonResponse create(PersonRequest request);
        List<PersonResponse> list();
    }

    @Service
    @Transactional
    public static class PersonServiceImpl implements PersonService {
        private final PersonRepository personRepository;
        private final ChurchRepository churchRepository;

        public PersonServiceImpl(PersonRepository personRepository,
                                 ChurchRepository churchRepository) {
            this.personRepository = personRepository;
            this.churchRepository = churchRepository;
        }

        @Override
        public PersonResponse create(PersonRequest request) {
            // la lógica previamente en el controlador se mueve aquí
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

        @Override
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
    }
}
