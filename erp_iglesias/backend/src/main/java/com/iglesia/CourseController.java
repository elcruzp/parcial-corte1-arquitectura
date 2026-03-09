package com.iglesia;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/courses")
public class CourseController {
    private final CourseRepository courseRepository;
    private final ChurchRepository churchRepository;

    public CourseController(CourseRepository courseRepository, ChurchRepository churchRepository) {
        this.courseRepository = courseRepository;
        this.churchRepository = churchRepository;
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    public CourseResponse create(@RequestBody CourseRequest request) {
        Church church = requireChurch();
        Course course = new Course();
        course.setName(request.name());
        course.setDescription(request.description());
        course.setPrice(request.price());
        course.setChurch(church);
        courseRepository.save(course);
        return CourseResponse.from(course);
    }

    @PreAuthorize("hasRole('ADMIN') or hasRole('CLIENT')")
    @GetMapping
    public List<CourseResponse> list() {
        Church church = requireChurch();
        return courseRepository.findAllByChurchId(church.getId())
            .stream()
            .map(CourseResponse::from)
            .toList();
    }

    private Church requireChurch() {
        return churchRepository.findAll()
            .stream()
            .findFirst()
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Debe registrar una iglesia primero"));
    }

    public record CourseRequest(
        @NotBlank String name,
        String description,
        @NotNull BigDecimal price
    ) {}

    public record CourseResponse(
        Long id,
        String name,
        String description,
        BigDecimal price,
        boolean active
    ) {
        public static CourseResponse from(Course course) {
            return new CourseResponse(
                course.getId(),
                course.getName(),
                course.getDescription(),
                course.getPrice(),
                course.isActive()
            );
        }
    }
}
