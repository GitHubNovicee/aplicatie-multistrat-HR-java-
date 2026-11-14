package org.hr.infrastructure;

import org.hr.domain.training.Course;

import java.util.List;
import java.util.Optional;

public interface ICourseEntityRepository extends IEntityRepository<Course> {
    Optional<Course> findByCode(String code);

    List<Course> findByActiveTrue();
}
