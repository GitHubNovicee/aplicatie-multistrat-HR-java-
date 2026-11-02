package org.hr.application;

import org.hr.HrTestSupport;
import org.hr.domain.common.AccessDeniedBusinessException;
import org.hr.domain.common.BusinessValidationException;
import org.hr.domain.development.*;
import org.hr.domain.organization.Role;
import org.hr.domain.relations.*;
import org.hr.domain.training.*;
import org.hr.infrastructure.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

/** WORKFLOW Business Service Tests - competente/planuri (UC-18, 19, 23, 24) si cazuri HR (UC-25..27). */
public class Test6_DevelopmentAndCaseWorkflowServices extends HrTestSupport {
    @Autowired private IDevelopmentWorkflowService development;
    @Autowired private IEmployeeCaseWorkflowService caseWorkflow;
    @Autowired private IDevelopmentPlanEntityRepository planRepository;
    @Autowired private IEmployeeCaseEntityRepository caseRepository;
    @Autowired private ITrainingNeedEntityRepository needRepository;
    @Autowired private IEmployeeCompetencyEntityRepository employeeCompetencyRepository;
    @Autowired private org.hr.services.INotificationService notificationService;

    private Long manager() { return idOf(IHrSampleDataFactory.MANAGER_USERNAME); }
    private Long emRel() { return idOf(IHrSampleDataFactory.EMP_REL_USERNAME); }
    private Long javaId() {
        return competencyRepository.findFirst(c -> c.getName().equals(IHrSampleDataFactory.SAMPLE_COMPETENCY)).orElseThrow().getId();
    }

    // ---------------- competente si nevoi de training ----------------

    @Test
    void assessCompetency_singleLevelPerCompetency_vr24() {
        Long emp = newEmployee(Role.EMPLOYEE);
        Long first = development.assessCompetency(manager(), emp, javaId(), ProficiencyLevel.BEGINNER);
        Long second = development.assessCompetency(manager(), emp, javaId(), ProficiencyLevel.ADVANCED);   // actualizare
        assertEquals(first, second);
        assertEquals(1, employeeCompetencyRepository.find(x -> x.getEmployee().getId().equals(emp)).size());
        assertEquals(ProficiencyLevel.ADVANCED, employeeCompetencyRepository.requireById(first).getLevel());
        // UC-18: angajatul este notificat (EVALUATION)
        assertEquals(2, notificationService.getNotificationsOf(emp).stream()
                .filter(n -> n.getType() == org.hr.domain.notification.NotificationType.EVALUATION).count());
        assertThrows(AccessDeniedBusinessException.class,
                () -> development.assessCompetency(newEmployee(Role.EMPLOYEE), emp, javaId(), ProficiencyLevel.EXPERT));
    }

    @Test
    void identifyTrainingNeed_suggestsActiveCourse_noDuplicateOpenNeed() {
        Long emp = newEmployee(Role.EMPLOYEE);
        Long needId = development.identifyTrainingNeed(manager(), emp, javaId(), ProficiencyLevel.ADVANCED, "needed");
        TrainingNeed need = needRepository.requireById(needId);
        assertEquals(TrainingNeedStatus.IDENTIFIED, need.getStatus());
        assertNotNull(need.getSuggestedCourse());
        assertThrows(BusinessValidationException.class,
                () -> development.identifyTrainingNeed(manager(), emp, javaId(), ProficiencyLevel.EXPERT, "again"));
    }

    // ---------------- planuri de dezvoltare ----------------

    @Test
    void developmentPlan_objectivesProgressAndOrphanRemoval() {
        Long emp = newEmployee(Role.EMPLOYEE);
        Long planId = development.createDevelopmentPlan(manager(), emp, "Growth", "desc",
                LocalDate.now(), LocalDate.now().plusMonths(6));
        assertEquals(PlanStatus.DRAFT, planRepository.requireById(planId).getStatus());
        assertThrows(BusinessValidationException.class, () -> development.activatePlan(manager(), planId));   // fara obiective

        Long o1 = development.addObjective(manager(), planId, "Learn Java", null, "certificate", LocalDate.now().plusMonths(2));
        Long o2 = development.addObjective(manager(), planId, "Mentor a junior", null, null, LocalDate.now().plusMonths(3));
        assertNotEquals(o1, o2);
        development.activatePlan(manager(), planId);
        assertEquals(PlanStatus.ACTIVE, planRepository.requireById(planId).getStatus());

        // UC-24: angajatul isi actualizeaza obiectivul; CR-09
        DevelopmentPlanView view = development.updateObjective(emp, planId, o1, ObjectiveStatus.ACHIEVED, "passed");
        assertEquals(50.0, view.getProgress());
        development.updateObjective(manager(), planId, o2, ObjectiveStatus.CANCELLED, null);
        assertEquals(100.0, development.getPlanProgress(planId).getProgress());          // 1 / 1 neanulate

        // VR-29: obiectivul exista doar in agregat; eliminarea din plan il sterge
        development.removeObjective(manager(), planId, o1);
        assertTrue(planRepository.requireById(planId).findObjective(o1).isEmpty());
        assertEquals(1, planRepository.requireById(planId).getObjectives().size());
    }

    @Test
    void developmentPlan_objectiveDueAfterPlanEndIsRejectedAndAggregateStaysConsistent() {
        Long emp = newEmployee(Role.EMPLOYEE);
        Long planId = development.createDevelopmentPlan(manager(), emp, "Short", null,
                LocalDate.now(), LocalDate.now().plusMonths(1));
        assertThrows(BusinessValidationException.class, () -> development.addObjective(manager(), planId,
                "Too late", null, null, LocalDate.now().plusMonths(3)));
        assertTrue(planRepository.requireById(planId).getObjectives().isEmpty());
    }

    @Test
    void developmentPlan_permissions() {
        Long emp = newEmployee(Role.EMPLOYEE);
        assertThrows(AccessDeniedBusinessException.class, () -> development.createDevelopmentPlan(emp, emp, "x", null,
                LocalDate.now(), LocalDate.now().plusMonths(1)));           // angajatul doar vizualizeaza
        Long planId = development.createDevelopmentPlan(adminId(), emp, "By HR", null,
                LocalDate.now(), LocalDate.now().plusMonths(1));
        assertThrows(AccessDeniedBusinessException.class,
                () -> development.addObjective(emp, planId, "mine", null, null, LocalDate.now().plusDays(5)));
    }

    // ---------------- cazuri relatii angajati ----------------

    @Test
    void caseFlow_submitAssignActResolveClose() {
        Long emp = newEmployee(Role.EMPLOYEE);
        Long caseId = caseWorkflow.submitCase(emp, CaseType.COMPLAINT, "Noise", "too loud");
        EmployeeCase c = caseRepository.requireById(caseId);
        assertEquals(CaseStatus.OPEN, c.getStatus());
        assertTrue(c.getCaseNumber().toString().matches("ER-\\d{4}-\\d{4}"));            // CR-04

        caseWorkflow.assignCase(emRel(), caseId, emRel());
        assertEquals(CaseStatus.ASSIGNED, c.getStatus());
        // VR-30: doar responsabilul asignat poate rezolva; actiunile sunt obligatorii inainte
        assertThrows(BusinessValidationException.class, () -> caseWorkflow.resolveCase(emRel(), caseId, "too early"));
        caseWorkflow.registerAction(emRel(), caseId, "Talked to both parties");
        assertEquals(CaseStatus.IN_PROGRESS, c.getStatus());
        assertEquals(1, c.getActions().size());
        assertThrows(AccessDeniedBusinessException.class, () -> caseWorkflow.registerAction(adminId(), caseId, "not mine"));
        assertThrows(BusinessValidationException.class, () -> caseWorkflow.resolveCase(adminId(), caseId, "not assignee"));
        assertThrows(BusinessValidationException.class, () -> caseWorkflow.resolveCase(emRel(), caseId, " "));

        caseWorkflow.resolveCase(emRel(), caseId, "Agreement reached");
        assertEquals(CaseStatus.RESOLVED, c.getStatus());
        assertNotNull(c.getResolvedAt());
        caseWorkflow.closeCase(emRel(), caseId);
        assertEquals(CaseStatus.CLOSED, c.getStatus());
    }

    @Test
    void caseNumbersAreSequentialPerYear() {
        Long emp = newEmployee(Role.EMPLOYEE);
        Long a = caseWorkflow.submitCase(emp, CaseType.REQUEST, "a", null);
        Long b = caseWorkflow.submitCase(emp, CaseType.REQUEST, "b", null);
        var na = caseRepository.requireById(a).getCaseNumber();
        var nb = caseRepository.requireById(b).getCaseNumber();
        assertEquals(na.getYear(), nb.getYear());
        assertEquals(na.getSequence() + 1, nb.getSequence());
    }

    @Test
    void complaintsAreVisibleOnlyToSubmitterAssigneeAndManager() {
        Long emp = newEmployee(Role.EMPLOYEE);
        Long other = newEmployee(Role.EMPLOYEE);
        Long caseId = caseWorkflow.submitCase(emp, CaseType.COMPLAINT, "Boss", "details");
        assertNotNull(caseWorkflow.getCase(emp, caseId));
        assertNotNull(caseWorkflow.getCase(manager(), caseId));                           // managerul angajatului
        assertThrows(AccessDeniedBusinessException.class, () -> caseWorkflow.getCase(other, caseId));
        assertThrows(AccessDeniedBusinessException.class, () -> caseWorkflow.getCase(adminId(), caseId));
        caseWorkflow.assignCase(emRel(), caseId, emRel());
        assertNotNull(caseWorkflow.getCase(emRel(), caseId));                             // responsabilul HR

        Long request = caseWorkflow.submitCase(emp, CaseType.REQUEST, "Laptop", null);
        assertNotNull(caseWorkflow.getCase(adminId(), request));                          // cererile sunt vizibile pentru HR
    }

    @Test
    void vr28_archivedEmployeeCannotSubmitCases() {
        Long emp = newEmployee(Role.EMPLOYEE);
        organizationWorkflow.archiveEmployee(adminId(), emp, null);
        assertThrows(BusinessValidationException.class, () -> caseWorkflow.submitCase(emp, CaseType.REQUEST, "x", null));
    }
}
