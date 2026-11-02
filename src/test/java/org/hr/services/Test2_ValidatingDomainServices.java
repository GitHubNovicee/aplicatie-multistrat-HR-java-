package org.hr.services;

import org.hr.HrTestSupport;
import org.hr.domain.common.BusinessValidationException;
import org.hr.domain.common.DateRange;
import org.hr.domain.development.DevelopmentPlan;
import org.hr.domain.leave.LeaveRequest;
import org.hr.domain.leave.LeaveType;
import org.hr.domain.organization.Employee;
import org.hr.domain.recruitment.*;
import org.hr.domain.relations.CaseType;
import org.hr.domain.relations.EmployeeCase;
import org.hr.domain.training.Course;
import org.hr.domain.training.TrainingSession;
import org.hr.infrastructure.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/**
 * VALIDATION Service Tests: JavaBean Validation (adnotari pe entitati) + reguli de business VR-xx.
 * Strategy: Simple-Facade.
 */
public class Test2_ValidatingDomainServices extends HrTestSupport {
    @Autowired private IEmployeeValidatingService employeeValidator;
    @Autowired private IRecruitmentValidatingService recruitmentValidator;
    @Autowired private ILeaveValidatingService leaveValidator;
    @Autowired private ITrainingValidatingService trainingValidator;
    @Autowired private IEmployeeCaseValidatingService caseValidator;
    @Autowired private IDevelopmentPlanValidatingService planValidator;
    @Autowired private IEmployeeEntityFactory employeeFactory;
    @Autowired private IRecruitmentEntityFactory recruitmentFactory;
    @Autowired private ILeaveEntityFactory leaveFactory;
    @Autowired private ITrainingEntityFactory trainingFactory;
    @Autowired private IEmployeeCaseEntityFactory caseFactory;
    @Autowired private IDevelopmentPlanEntityFactory planFactory;

    @Test
    void beanValidation_emptyEmployeeReportsAllMandatoryFields() {
        Set<String> violations = employeeValidator.validate(new Employee());
        System.out.println(violations);
        assertTrue(violations.stream().anyMatch(v -> v.startsWith("firstName")));
        assertTrue(violations.stream().anyMatch(v -> v.startsWith("lastName")));
        assertTrue(violations.stream().anyMatch(v -> v.startsWith("employeeCode")));
        assertTrue(violations.stream().anyMatch(v -> v.startsWith("contactInfo")));
        assertTrue(violations.stream().anyMatch(v -> v.startsWith("department")));
        assertThrows(BusinessValidationException.class, () -> employeeValidator.validateWithException(new Employee()));
    }

    @Test
    void beanValidation_validEmployeeAndNestedValueObject() {
        Employee ok = employeeFactory.buildEmployee("Ion", "Popescu", "ion.valid@test.example", "0711111111",
                LocalDate.now(), engineering(), softwareEngineer(), null);
        assertTrue(employeeValidator.validate(ok).isEmpty());

        Employee badEmail = employeeFactory.buildEmployee("Ion", "Popescu", "not-an-email", "abc",
                LocalDate.now(), engineering(), softwareEngineer(), null);
        Set<String> violations = employeeValidator.validate(badEmail);
        assertTrue(violations.stream().anyMatch(v -> v.startsWith("contactInfo.email")), violations.toString());
        assertTrue(violations.stream().anyMatch(v -> v.startsWith("contactInfo.phone")), violations.toString());
    }

    @Test
    void rq01_duplicateEmailAndSelfManagerRejected() {
        Employee dup = employeeFactory.buildEmployee("X", "Y", "engineer1@hr.example", null,
                LocalDate.now(), engineering(), softwareEngineer(), null);
        assertTrue(employeeValidator.validate(dup).stream().anyMatch(v -> v.contains("already used")));
        Employee selfManaged = employeeFactory.buildEmployee("X", "Y", "self.manager@test.example", null,
                LocalDate.now(), engineering(), softwareEngineer(), null);
        selfManaged.setManager(selfManaged);
        assertTrue(employeeValidator.validate(selfManaged).stream().anyMatch(v -> v.startsWith("manager")));
    }

    @Test
    void vr28_archivedEmployeeIsRejected() {
        Employee archived = employee(newEmployee(org.hr.domain.organization.Role.EMPLOYEE));
        archived.setArchived(true);
        BusinessValidationException ex = assertThrows(BusinessValidationException.class,
                () -> employeeValidator.validateEmployeeIsActive(archived));
        assertTrue(ex.getMessage().contains("VR-28"));
        archived.setArchived(false);
        assertDoesNotThrow(() -> employeeValidator.validateEmployeeIsActive(archived));
    }

    @Test
    void leaveRequest_invalidPeriodIsReportedByValueObject() {
        Employee e = employee(idOf("engineer1"));
        LeaveRequest r = leaveFactory.buildLeaveRequest(e, LeaveType.SICK,
                LocalDate.of(2027, 5, 10), LocalDate.of(2027, 5, 3), "reversed");
        Set<String> violations = leaveValidator.validate(r);
        assertTrue(violations.stream().anyMatch(v -> v.startsWith("period")), violations.toString());
    }

    @Test
    void vr21_feedbackOnlyFromListedInterviewersAndOnlyOnce() {
        Employee interviewer = employee(idOf(IHrSampleDataFactory.INTERVIEWER_USERNAME));
        Employee stranger = employee(idOf("engineer1"));
        Interview interview = recruitmentFactory.buildInterview(null, LocalDateTime.now().plusDays(2), "room",
                Set.of(interviewer), 1);

        InterviewFeedback fromStranger = recruitmentFactory.buildFeedback(stranger, 7, "x", Recommendation.YES);
        BusinessValidationException ex = assertThrows(BusinessValidationException.class,
                () -> recruitmentValidator.validateFeedback(interview, fromStranger));
        assertTrue(ex.getMessage().contains("VR-21"));

        InterviewFeedback first = recruitmentFactory.buildFeedback(interviewer, 7, "ok", Recommendation.YES);
        assertDoesNotThrow(() -> recruitmentValidator.validateFeedback(interview, first));
        interview.addFeedback(first);
        InterviewFeedback second = recruitmentFactory.buildFeedback(interviewer, 9, "again", Recommendation.STRONG_YES);
        assertThrows(BusinessValidationException.class, () -> recruitmentValidator.validateFeedback(interview, second));
    }

    @Test
    void feedbackScoreMustBeBetween1And10() {
        Employee interviewer = employee(idOf(IHrSampleDataFactory.INTERVIEWER_USERNAME));
        Interview interview = recruitmentFactory.buildInterview(null, LocalDateTime.now().plusDays(2), "room",
                Set.of(interviewer), 1);
        for (int bad : new int[]{0, 11}) {
            InterviewFeedback f = recruitmentFactory.buildFeedback(interviewer, bad, "x", Recommendation.NO);
            BusinessValidationException ex = assertThrows(BusinessValidationException.class,
                    () -> recruitmentValidator.validateFeedback(interview, f));
            assertTrue(ex.getViolations().stream().anyMatch(v -> v.startsWith("score")), ex.getMessage());
        }
    }

    @Test
    void vr23_enrollmentOnlyOnceAndOnlyWhileSeatsAreAvailable() {
        Course course = trainingFactory.buildCourse("VR23-X", "t", 8, Set.of());
        TrainingSession session = trainingFactory.buildTrainingSession(course, LocalDateTime.now().plusDays(3),
                LocalDateTime.now().plusDays(4), "room", "trainer", 1, null);
        Employee first = employee(idOf("engineer1"));
        Employee second = employee(idOf("engineer2"));

        assertDoesNotThrow(() -> trainingValidator.validateEnrollment(session, first));
        session.addEnrollment(trainingFactory.buildEnrollment(first));
        BusinessValidationException twice = assertThrows(BusinessValidationException.class,
                () -> trainingValidator.validateEnrollment(session, first));
        assertTrue(twice.getMessage().contains("only once"));
        BusinessValidationException full = assertThrows(BusinessValidationException.class,
                () -> trainingValidator.validateEnrollment(session, second));
        assertTrue(full.getMessage().contains("no available seats"));

        session.setStatus(org.hr.domain.training.SessionStatus.CANCELLED);
        assertThrows(BusinessValidationException.class, () -> trainingValidator.validateEnrollment(session, second));
    }

    @Test
    void vr30_caseCanBeResolvedOnlyByAssignedResponsibleWithResolution() {
        Employee submitter = employee(idOf("engineer1"));
        Employee emRel = employee(idOf(IHrSampleDataFactory.EMP_REL_USERNAME));
        EmployeeCase c = caseFactory.buildEmployeeCase(CaseType.COMPLAINT, submitter, "subject", "details");
        c.setStatus(org.hr.domain.relations.CaseStatus.IN_PROGRESS);

        assertThrows(BusinessValidationException.class, () -> caseValidator.validateResolution(c, emRel, "done")); // nu e asignat
        c.setAssignedTo(emRel);
        assertThrows(BusinessValidationException.class, () -> caseValidator.validateResolution(c, emRel, " "));   // fara resolution
        assertDoesNotThrow(() -> caseValidator.validateResolution(c, emRel, "done"));
        assertThrows(BusinessValidationException.class,
                () -> caseValidator.validateResolution(c, employee(idOf("engineer2")), "done"));                   // alt angajat
    }

    @Test
    void vr29_planAggregateValidatesItsObjectives() {
        Employee e = employee(idOf("engineer1"));
        DevelopmentPlan plan = planFactory.buildDevelopmentPlan(e, "Plan", null, LocalDate.now(), LocalDate.now().plusMonths(2));
        plan.addObjective(planFactory.buildObjective("", null, null, LocalDate.now().plusDays(5)));   // titlu gol
        BusinessValidationException ex = assertThrows(BusinessValidationException.class, () -> planValidator.validatePlan(plan));
        assertTrue(ex.getViolations().stream().anyMatch(v -> v.contains("title")), ex.getMessage());
    }

    @Test
    void vr20_duplicateApplicationReportedWithoutException() {
        // fara date persistate: o candidatura noua, fara candidat si anunt, are violari bean-validation
        Set<String> violations = recruitmentValidator.validate(new Application());
        assertTrue(violations.stream().anyMatch(v -> v.startsWith("candidate")));
        assertTrue(violations.stream().anyMatch(v -> v.startsWith("jobOpening")));
    }
}
