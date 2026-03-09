package com.iglesia;

import org.springframework.data.jpa.repository.JpaRepository;

public interface ChurchRepository extends JpaRepository<Church, Long> {
    boolean existsByNameIgnoreCase(String name);
}
