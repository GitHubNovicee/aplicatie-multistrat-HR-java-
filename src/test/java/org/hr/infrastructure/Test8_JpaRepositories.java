package org.hr.infrastructure;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.hr.HrTestSupport;
import org.hr.domain.common.CaseNumber;
import org.hr.domain.development.DevelopmentPlan;
import org.hr.domain.development.Objective;
import org.hr.domain.development.ObjectiveStatus;
import org.hr.domain.leave.LeaveRequest;
import org.hr.domain.leave.LeaveStatus;
import org.hr.domain.leave.LeaveType;
import org.hr.domain.organization.Employee;
import org.hr.domain.organization.Role;
import org.hr.domain.organization.UserAccount;
import org.hr.domain.relations.CaseAction;
import org.hr.domain.relations.CaseStatus;
import org.hr.domain.relations.CaseType;
import org.hr.domain.relations.EmployeeCase;
import org.hr.domain.training.Course;
import org.hr.domain.training.Enrollment;
import org.hr.domain.training.TrainingSession;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Sprint 3 - teste pentru modelul de persistenta JPA si pentru serviciile de repository (Spring Data JPA):
 * mapari (@Entity, @Embeddable, @Convert, @Enumerated), agregate (cascade + orphanRemoval),
 * operatii personalizate (findByXxx, @Query) si entity listener (@PrePersist / @PreUpdate).
 */
public class Test8_JpaRepositories extends HrTestSupport {
    @PersistenceContext private EntityManager em;

    @Autowired private IEmployeeCaseEntityRepository caseRepository;
    @Autowired private IEmployeeCaseEntityFactory caseFactory;
    @Autowired private IDevelopmentPlanEntityRepository planRepository;
    @Autowired private IDevelopmentPlanEntityFactory planFactory;
    @Autowired private ITrainingSessionEntityRepository sessionRepository;
    @Autowired private ITrainingEntityFactory trainingFactory;
    @Autowired private ICourseEntityRepository courseRepository;
    @Autowired private ILeaveRequestEntityRepository leaveRequestRepository;
    @Autowired private ILeaveEntityFactory leaveFactory;

    /** Sincronizeaza cu baza de date si goleste contextul de persistenta: urmatoarele citiri vin din DB. */
    private void flushAndClear() {
        em.flush();
        em.clear();
    }

    // ------------------------------------------------------------------ entitati si Value Objects

    @Test
    void entityIsPersistedWithGeneratedIdVersionAndAuditFields() {
        Long id = newEmployee(Role.EMPLOYEE);
        flushAndClear();

        Employee e = employeeRepository.requireById(id);
        assertNotNull(e.getId());
        assertEquals(0L, e.getVersion());
        assertNotNull(e.getCreatedAt());
        assertNotNull(e.getUpdatedAt());
        assertNotNull(e.getContactInfo().getEmail());                      // Value Object @Embeddable
        assertEquals("0722222222", e.getContactInfo().getPhone());
    }

    @Test
    void auditListenerUpdatesUpdatedAtAndVersionOnlyOnModification() throws InterruptedException {
        Employee e = employee(newEmployee(Role.EMPLOYEE));
        var createdAt = e.getCreatedAt();
        long version = e.getVersion();

        Thread.sleep(5);
        e.setLastName("Renamed");
        employeeRepository.add(e);                                          // @PreUpdate

        assertEquals(createdAt, e.getCreatedAt());
        assertTrue(e.getUpdatedAt().isAfter(createdAt));
        assertEquals(version + 1, e.getVersion());

        var updatedAt = e.getUpdatedAt();
        employeeRepository.add(e);                                          // fara modificari: nu se actualizeaza nimic
        assertEquals(updatedAt, e.getUpdatedAt());
        assertEquals(version + 1, e.getVersion());
    }

    @Test
    void enumRolesAreMappedAsElementCollection() {
        Long id = newEmployee(Role.MANAGER, Role.EMPLOYEE);
        flushAndClear();

        UserAccount account = accountRepository.findByEmployeeAndEnabledTrue(employee(id)).orElseThrow();
        assertEquals(Set.of(Role.MANAGER, Role.EMPLOYEE), account.getRoles());
    }

    @Test
    void caseNumberValueObjectIsStoredWithConverter() {
        Employee submitter = employee(newEmployee(Role.EMPLOYEE));
        EmployeeCase c = caseRepository.add(caseFactory.buildEmployeeCase(CaseType.REQUEST, submitter, "Subject", "Details"));
        CaseNumber number = c.getCaseNumber();
        flushAndClear();

        EmployeeCase loaded = caseRepository.findByCaseNumber(number).orElseThrow();
        assertEquals(c.getId(), loaded.getId());
        assertEquals(number, loaded.getCaseNumber());
        assertEquals(CaseStatus.OPEN, loaded.getStatus());                  // @Enumerated(STRING)
        assertEquals(number.toString(), em.createNativeQuery("select case_number from employee_case where id = :id")
                .setParameter("id", c.getId()).getSingleResult());
    }

    // ------------------------------------------------------------------ agregate

    @Test
    void rootEntityCascadesToComponentEntitiesAndRemovesOrphans() {
        Employee owner = employee(newEmployee(Role.EMPLOYEE));
        DevelopmentPlan plan = planFactory.buildDevelopmentPlan(owner, "Plan", null,
                LocalDate.now(), LocalDate.now().plusMonths(3));
        plan.addObjective(planFactory.buildObjective("Obj 1", null, null, LocalDate.now().plusDays(10)));
        plan.addObjective(planFactory.buildObjective("Obj 2", null, null, LocalDate.now().plusDays(20)));
        planRepository.add(plan);                                            // cascade = ALL: obiectivele se salveaza cu planul
        flushAndClear();

        DevelopmentPlan loaded = planRepository.requireById(plan.getId());
        assertEquals(2, loaded.getObjectives().size());
        Objective first = loaded.getObjectives().get(0);
        assertNotNull(first.getId());
        assertSame(loaded, first.getPlan());                                 // mappedBy = "plan"
        assertEquals(ObjectiveStatus.NOT_STARTED, first.getStatus());
        assertEquals(LocalDate.now(), loaded.getPeriod().getStartDate());   // @Embedded DateRange

        loaded.removeObjective(first);                                       // orphanRemoval = true
        planRepository.add(loaded);
        flushAndClear();

        assertEquals(1, planRepository.requireById(plan.getId()).getObjectives().size());
        assertEquals(1L, em.createQuery("select count(o) from Objective o where o.plan.id = :id", Long.class)
                .setParameter("id", plan.getId()).getSingleResult());
    }

    @Test
    void deletingTheRootDeletesTheWholeAggregate() {
        Employee reporter = employee(newEmployee(Role.EMPLOYEE));
        EmployeeCase c = caseFactory.buildEmployeeCase(CaseType.COMPLAINT, reporter, "Aggregate", null);
        c.addAction(caseFactory.buildCaseAction(reporter, "first"));
        c.addAction(caseFactory.buildCaseAction(reporter, "second"));
        caseRepository.add(c);
        flushAndClear();

        EmployeeCase loaded = caseRepository.requireById(c.getId());
        assertEquals(List.of("first", "second"), loaded.getActions().stream().map(CaseAction::getDescription).toList());

        assertTrue(caseRepository.remove(loaded));
        flushAndClear();
        assertNull(caseRepository.getById(c.getId()));
        assertEquals(0L, em.createQuery("select count(a) from CaseAction a where a.employeeCase.id = :id", Long.class)
                .setParameter("id", c.getId()).getSingleResult());
    }

    // ------------------------------------------------------------------ operatii personalizate (Spring Data)

    @Test
    void derivedQueriesFindByPropertyName() {
        Long id = newEmployee(Role.EMPLOYEE);
        Employee e = employee(id);
        String email = e.getContactInfo().getEmail();
        flushAndClear();

        assertEquals(id, employeeRepository.findByContactInfoEmail(email).orElseThrow().getId());
        assertTrue(employeeRepository.existsByContactInfoEmail(email));
        assertEquals(id, employeeRepository.findByEmployeeCode(e.getEmployeeCode()).orElseThrow().getId());
        assertTrue(employeeRepository.findByDepartmentAndArchivedFalse(engineering()).stream()
                .anyMatch(x -> x.getId().equals(id)));
        assertTrue(employeeRepository.findByManager(employee(idOf(IHrSampleDataFactory.MANAGER_USERNAME))).stream()
                .anyMatch(x -> x.getId().equals(id)));
        assertEquals(IHrSampleDataFactory.ADMIN_USERNAME,
                accountRepository.findByUsername(IHrSampleDataFactory.ADMIN_USERNAME).orElseThrow().getUsername());
        assertTrue(employeeRepository.findByEmployeeCode("NOPE").isEmpty());
    }

    @Test
    void customQueryFiltersWithQueryAnnotation() {
        Long id = newEmployee(Role.HR_RECRUITER);
        Employee e = employee(id);
        e.setLastName("Zanzibarescu");
        employeeRepository.add(e);

        assertEquals(List.of(id), employeeRepository.searchActiveByName("zanzibar").stream().map(Employee::getId).toList());

        assertTrue(accountRepository.findEnabledByRole(Role.HR_RECRUITER).stream()
                .anyMatch(a -> a.getEmployee().getId().equals(id)));
        assertTrue(accountRepository.findEnabledByRole(Role.HR_RECRUITER).stream()
                .allMatch(a -> a.hasRole(Role.HR_RECRUITER) && a.isEnabled()));
    }

    @Test
    void overlappingLeaveRequestsQueryUsesEmbeddedDateRange() {
        Employee e = employee(newEmployee(Role.EMPLOYEE));
        LocalDate start = nextWorkWeekStart();
        LeaveRequest request = leaveRequestRepository.add(
                leaveFactory.buildLeaveRequest(e, LeaveType.ANNUAL, start, start.plusDays(4), "holiday"));
        flushAndClear();

        Set<LeaveStatus> active = Set.of(LeaveStatus.PENDING, LeaveStatus.APPROVED);
        assertEquals(List.of(request.getId()), leaveRequestRepository
                .findOverlapping(employee(e.getId()), active, start.plusDays(2), start.plusDays(8))
                .stream().map(LeaveRequest::getId).toList());
        assertTrue(leaveRequestRepository.findOverlapping(employee(e.getId()), active,
                start.plusDays(5), start.plusDays(9)).isEmpty());
        assertEquals(1, leaveRequestRepository.findByEmployeeAndStatus(employee(e.getId()), LeaveStatus.PENDING).size());
    }

    @Test
    void trainingSessionAggregateAndParticipantQuery() {
        Employee participant = employee(newEmployee(Role.EMPLOYEE));
        Course course = courseRepository.findByCode(IHrSampleDataFactory.SAMPLE_COURSE_CODE).orElseThrow();
        TrainingSession session = trainingFactory.buildTrainingSession(course, LocalDateTime.now().plusDays(7),
                LocalDateTime.now().plusDays(8), "Room 1", "Trainer", 5, null);
        session.addEnrollment(trainingFactory.buildEnrollment(participant));
        sessionRepository.add(session);
        flushAndClear();

        List<TrainingSession> found = sessionRepository.findWithParticipant(participant.getId());
        assertEquals(1, found.size());
        TrainingSession loaded = found.get(0);
        assertEquals(1, loaded.getEnrollments().size());
        Enrollment enrollment = loaded.getEnrollments().get(0);
        assertNotNull(enrollment.getId());
        assertSame(loaded, enrollment.getSession());
        assertEquals(course.getId(), loaded.getCourse().getId());
        assertTrue(loaded.getCourse().getTargetCompetencies().size() >= 1);   // @ManyToMany incarcat lazy

        assertTrue(sessionRepository.findByCourse(course).stream().anyMatch(s -> s.getId().equals(session.getId())));
        assertTrue(sessionRepository.findWithParticipant(-1L).isEmpty());
    }

    @Test
    void springRepositoryContractIsKeptOnTopOfJpaRepository() {
        long before = employeeRepository.count();
        newEmployee(Role.EMPLOYEE);
        assertEquals(before + 1, employeeRepository.count());
        assertEquals(employeeRepository.size(), employeeRepository.toCollection().size());
        assertEquals(employeeRepository.findAll().size(), employeeRepository.find(x -> true).size());
        assertTrue(employeeRepository.findFirst(x -> x.getEmployeeCode() != null).isPresent());
    }
}
