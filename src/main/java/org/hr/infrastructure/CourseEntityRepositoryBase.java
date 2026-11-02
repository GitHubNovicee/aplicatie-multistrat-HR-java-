package org.hr.infrastructure;

import org.hr.domain.training.Course;
import org.springframework.stereotype.Repository;

//SDI Bean Component
@Repository
public class CourseEntityRepositoryBase extends InMemoryEntityRepositoryBase<Course> implements ICourseEntityRepository {
    public CourseEntityRepositoryBase() {
        super("Course");
    }
}
