package org.hr.application;

import org.hr.HrTestSupport;
import org.hr.domain.common.AccessDeniedBusinessException;
import org.hr.domain.common.BusinessValidationException;
import org.hr.domain.organization.Role;
import org.hr.domain.training.*;
import org.hr.infrastructure.IHrSampleDataFactory;
import org.hr.infrastructure.ITrainingSessionEntityRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/** WORKFLOW Business Service Tests - training UC-17, UC-20, UC-21, UC-22 (CR-05, VR-23, VR-28). */
public class Test5_TrainingWorkflowService extends HrTestSupport {
    @Autowired private ITrainingWorkflowService workflow;
    @Autowired private ITrainingSessionEntityRepository sessionRepository;
    private static int courseSeq = 0;

    private Long trainer() { return idOf(IHrSampleDataFactory.TRAINER_USERNAME); }

    private synchronized Long newCourse() {
        Long competencyId = competencyRepository.findFirst(c -> c.getName().equals(IHrSampleDataFactory.SAMPLE_COMPETENCY))
                .orElseThrow().getId();
        return workflow.createCourse(trainer(), "TW-" + (++courseSeq) + "-" + System.nanoTime() % 100000,
                "Workflow course", 16, Set.of(competencyId));
    }

    private Long newSession(Long courseId, int capacity) {
        return workflow.planSession(trainer(), courseId, LocalDateTime.now().plusDays(7),
                LocalDateTime.now().plusDays(8), "Room A", "Trainer X", capacity);
    }

    @Test
    void sessionLifecycle_enrollResultCertification() {
        Long sessionId = newSession(newCourse(), 2);
        Long e1 = newEmployee(Role.EMPLOYEE);
        Long e2 = newEmployee(Role.EMPLOYEE);
        Long e3 = newEmployee(Role.EMPLOYEE);

        workflow.enrollEmployee(e1, sessionId, e1);                                      // UC-21: auto-inscriere
        workflow.enrollEmployee(trainer(), sessionId, e2);                               // coordonatorul inscrie
        assertEquals(0, workflow.getSessionSummary(sessionId).getAvailableSeats());      // CR-05
        BusinessValidationException full = assertThrows(BusinessValidationException.class,
                () -> workflow.enrollEmployee(e3, sessionId, e3));
        assertTrue(full.getMessage().contains("VR-23"));
        assertThrows(BusinessValidationException.class, () -> workflow.enrollEmployee(e1, sessionId, e1));   // o singura data

        workflow.cancelEnrollment(e2, sessionId, e2);                                     // se elibereaza un loc
        assertEquals(1, workflow.getSessionSummary(sessionId).getAvailableSeats());

        // rezultate doar dupa start
        assertThrows(BusinessValidationException.class,
                () -> workflow.recordResult(trainer(), sessionId, e1, true, new BigDecimal("90"), true));
        assertEquals(SessionStatus.ONGOING, workflow.startSession(trainer(), sessionId));
        assertThrows(BusinessValidationException.class,                                   // certificare fara prezenta
                () -> workflow.recordResult(trainer(), sessionId, e1, false, null, true));
        workflow.recordResult(trainer(), sessionId, e1, true, new BigDecimal("90.5"), true);   // UC-22
        assertEquals(SessionStatus.COMPLETED, workflow.completeSession(trainer(), sessionId));

        Enrollment result = sessionRepository.requireById(sessionId).findEnrollmentOf(employee(e1)).orElseThrow();
        assertEquals(EnrollmentStatus.ATTENDED, result.getStatus());
        assertEquals(new BigDecimal("90.5"), result.getResultScore());
        assertTrue(result.isCertified());
        assertTrue(result.getCertificateNumber().startsWith("CERT-TW-"));
        assertNotNull(result.getCertifiedOn());
        assertEquals(1, workflow.getSessionSummary(sessionId).getAvailableSeats());  // ATTENDED ocupa locul, e2 a anulat
    }

    @Test
    void scoreOutOfRangeIsRejectedByBeanValidation() {
        Long sessionId = newSession(newCourse(), 3);
        Long e1 = newEmployee(Role.EMPLOYEE);
        workflow.enrollEmployee(e1, sessionId, e1);
        workflow.startSession(trainer(), sessionId);
        assertThrows(BusinessValidationException.class,
                () -> workflow.recordResult(trainer(), sessionId, e1, true, new BigDecimal("150"), false));
    }

    @Test
    void vr28_archivedEmployeeCannotBeEnrolled() {
        Long sessionId = newSession(newCourse(), 3);
        Long e1 = newEmployee(Role.EMPLOYEE);
        organizationWorkflow.archiveEmployee(adminId(), e1, null);
        BusinessValidationException ex = assertThrows(BusinessValidationException.class,
                () -> workflow.enrollEmployee(trainer(), sessionId, e1));
        assertTrue(ex.getMessage().contains("VR-28"));
    }

    @Test
    void inactiveCourseCannotBePlannedAndCancelledSessionRejectsEnrollment() {
        Long courseId = newCourse();
        workflow.setCourseActive(trainer(), courseId, false);
        assertThrows(BusinessValidationException.class, () -> newSession(courseId, 5));

        workflow.setCourseActive(trainer(), courseId, true);
        Long sessionId = newSession(courseId, 5);
        Long e1 = newEmployee(Role.EMPLOYEE);
        workflow.enrollEmployee(e1, sessionId, e1);
        assertEquals(SessionStatus.CANCELLED, workflow.cancelSession(trainer(), sessionId));
        assertEquals(5, workflow.getSessionSummary(sessionId).getAvailableSeats());
        Long e2 = newEmployee(Role.EMPLOYEE);
        assertThrows(BusinessValidationException.class, () -> workflow.enrollEmployee(e2, sessionId, e2));
    }

    @Test
    void permissions_onlyCoordinatorOrAdminManagesCatalogAndEnrollsOthers() {
        Long plain = newEmployee(Role.EMPLOYEE);
        assertThrows(AccessDeniedBusinessException.class,
                () -> workflow.createCourse(plain, "NOPE-1", "t", 8, Set.of()));
        Long sessionId = newSession(newCourse(), 3);
        Long other = newEmployee(Role.EMPLOYEE);
        assertThrows(AccessDeniedBusinessException.class, () -> workflow.enrollEmployee(plain, sessionId, other));
        assertThrows(BusinessValidationException.class,                                   // cod de curs duplicat
                () -> workflow.createCourse(trainer(), IHrSampleDataFactory.SAMPLE_COURSE_CODE, "dup", 8, Set.of()));
    }
}
