package org.hr.infrastructure;

import org.hr.domain.organization.Employee;
import org.hr.domain.training.*;

import java.time.LocalDateTime;
import java.util.Set;

/** Factory pentru modulele Training si Competente (RQ-07, RQ-08). */
public interface ITrainingEntityFactory {
    Competency buildCompetency(String name, String category, String description);

    Course buildCourse(String code, String title, int durationHours, Set<Competency> targetCompetencies);

    /** Sesiune PLANNED fara inscrieri. */
    TrainingSession buildTrainingSession(Course course, LocalDateTime startsAt, LocalDateTime endsAt,
                                         String location, String trainer, int capacity, Employee coordinator);

    /** Componenta (id local atribuit de TrainingSession.addEnrollment). */
    Enrollment buildEnrollment(Employee employee);

    EmployeeCompetency buildEmployeeCompetency(Employee employee, Competency competency, ProficiencyLevel level);

    TrainingNeed buildTrainingNeed(Employee employee, Competency competency, ProficiencyLevel targetLevel,
                                   String justification, Course suggestedCourse);
}
