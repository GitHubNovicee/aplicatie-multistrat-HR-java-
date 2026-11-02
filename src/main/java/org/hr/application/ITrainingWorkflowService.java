package org.hr.application;

import org.hr.domain.training.SessionStatus;
import org.hr.domain.training.TrainingSessionView;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Set;

/*
 * Business Workflow (RQ-07): catalog cursuri -> sesiune -> inscrieri -> rezultate si certificare. UC-17, UC-20 .. UC-22.
 */
public interface ITrainingWorkflowService {
    /** UC-17 (HR_TRAINING_COORDINATOR / HR_ADMIN). */
    Long createCourse(Long coordinatorId, String code, String title, int durationHours, Set<Long> competencyIds);

    void setCourseActive(Long coordinatorId, Long courseId, boolean active);

    /** UC-20: sesiune PLANNED. */
    Long planSession(Long coordinatorId, Long courseId, LocalDateTime startsAt, LocalDateTime endsAt,
                     String location, String trainer, int capacity);

    /** UC-21: inscriere ENROLLED (angajatul pe sine sau coordonatorul); VR-23, CR-05. */
    Long enrollEmployee(Long actorId, Long sessionId, Long employeeId);

    void cancelEnrollment(Long actorId, Long sessionId, Long employeeId);

    SessionStatus startSession(Long coordinatorId, Long sessionId);

    /** UC-22: ATTENDED/ABSENT, scor si certificare (numar de certificat generat). */
    void recordResult(Long coordinatorId, Long sessionId, Long employeeId, boolean attended,
                      BigDecimal score, boolean certified);

    SessionStatus completeSession(Long coordinatorId, Long sessionId);

    SessionStatus cancelSession(Long coordinatorId, Long sessionId);

    TrainingSessionView getSessionSummary(Long sessionId);
}
