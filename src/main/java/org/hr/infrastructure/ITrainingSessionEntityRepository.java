package org.hr.infrastructure;

import org.hr.domain.training.Course;
import org.hr.domain.training.SessionStatus;
import org.hr.domain.training.TrainingSession;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface ITrainingSessionEntityRepository extends IEntityRepository<TrainingSession> {
    List<TrainingSession> findByCourse(Course course);

    List<TrainingSession> findByStatusAndStartsAtAfter(SessionStatus status, LocalDateTime after);

    /** Filtru personalizat: sesiunile in care un angajat este inscris (cu inscrierea activa sau incheiata). */
    @Query("""
            select distinct s from TrainingSession s join s.enrollments e
            where e.employee.id = :employeeId and e.status <> org.hr.domain.training.EnrollmentStatus.CANCELLED
            """)
    List<TrainingSession> findWithParticipant(@Param("employeeId") Long employeeId);
}
