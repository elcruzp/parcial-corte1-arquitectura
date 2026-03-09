package com.iglesia;

import jakarta.validation.constraints.NotBlank;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/church")
public class ChurchController {
    private final ChurchRepository churchRepository;

    public ChurchController(ChurchRepository churchRepository) {
        this.churchRepository = churchRepository;
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    public ChurchResponse create(@RequestBody ChurchRequest request) {
        if (churchRepository.count() > 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Ya existe una iglesia registrada");
        }
        Church church = new Church();
        church.setName(request.name());
        church.setAddress(request.address());
        churchRepository.save(church);
        return ChurchResponse.from(church);
    }

    @GetMapping
    public ChurchResponse get() {
        return churchRepository.findAll()
            .stream()
            .findFirst()
            .map(ChurchResponse::from)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "No hay iglesia registrada"));
    }

    public record ChurchRequest(
        @NotBlank String name,
        String address
    ) {}

    public record ChurchResponse(
        Long id,
        String name,
        String address
    ) {
        public static ChurchResponse from(Church church) {
            return new ChurchResponse(church.getId(), church.getName(), church.getAddress());
        }
    }
}
