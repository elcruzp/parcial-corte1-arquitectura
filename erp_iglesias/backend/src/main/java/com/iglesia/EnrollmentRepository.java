package com.iglesia;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface EnrollmentRepository extends JpaRepository<Enrollment, Long> {
    List<Enrollment> findAllByPersonChurchId(Long churchId);
    List<Enrollment> findAllByPersonId(Long personId);
}
