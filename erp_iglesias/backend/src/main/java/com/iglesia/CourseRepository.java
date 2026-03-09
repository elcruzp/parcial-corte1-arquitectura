package com.iglesia;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface CourseRepository extends JpaRepository<Course, Long> {
    List<Course> findAllByChurchId(Long churchId);
    long countByChurchIdAndActiveTrue(Long churchId);
}
