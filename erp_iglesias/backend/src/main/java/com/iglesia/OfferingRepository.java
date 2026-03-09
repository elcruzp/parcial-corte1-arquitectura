package com.iglesia;

import org.springframework.data.jpa.repository.JpaRepository;
import java.time.LocalDateTime;
import java.util.List;

public interface OfferingRepository extends JpaRepository<Offering, Long> {
    List<Offering> findAllByPersonChurchId(Long churchId);
    long countByCreatedAtBetween(LocalDateTime start, LocalDateTime end);
}
