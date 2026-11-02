package org.hr.application;

import org.hr.domain.common.AccessDeniedBusinessException;
import org.hr.domain.common.BusinessValidationException;
import org.hr.domain.common.EntityNotFoundException;
import org.hr.domain.common.HrAuditView.AuditOperation;
import org.hr.domain.development.*;
import org.hr.domain.notification.NotificationType;
import org.hr.domain.organization.Employee;
import org.hr.domain.organization.Role;
import org.hr.domain.training.*;
import org.hr.infrastructure.*;
import org.hr.services.*;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.LocalDate;

@Service
public class DevelopmentWorkflowServiceImpl implements IDevelopmentWorkflowService {
    private final IEmployeeEntityRepository employeeRepository;
    private final ICompetencyEntityRepository competencyRepository;
    private final IEmployeeCompetencyEntityRepository employeeCompetencyRepository;
    private final ITrainingNeedEntityRepository needRepository;
    private final ICourseEntityRepository courseRepository;
    private final IDevelopmentPlanEntityRepository planRepository;
    private final ITrainingEntityFactory trainingFactory;
    private final IDevelopmentPlanEntityFactory planFactory;
    private final ITrainingValidatingService trainingValidator;
    private final IDevelopmentPlanValidatingService planValidator;
    private final IDevelopmentComputingService computingService;
    private final IAccessControlService accessControl;
    private final INotificationService notificationService;
    private final IHrAuditingService auditingService;
    private final Clock clock;

    public DevelopmentWorkflowServiceImpl(IEmployeeEntityRepository employeeRepository,
                                          ICompetencyEntityRepository competencyRepository,
                                          IEmployeeCompetencyEntityRepository employeeCompetencyRepository,
                                          ITrainingNeedEntityRepository needRepository,
                                          ICourseEntityRepository courseRepository,
                                          IDevelopmentPlanEntityRepository planRepository,
                                          ITrainingEntityFactory trainingFactory,
                                          IDevelopmentPlanEntityFactory planFactory,
                                          ITrainingValidatingService trainingValidator,
                                          IDevelopmentPlanValidatingService planValidator,
                                          IDevelopmentComputingService computingService,
                                          IAccessControlService accessControl,
                                          INotificationService notificationService,
                                          IHrAuditingService auditingService, Clock clock) {
        this.employeeRepository = employeeRepository;
        this.competencyRepository = competencyRepository;
        this.employeeCompetencyRepository = employeeCompetencyRepository;
        this.needRepository = needRepository;
        this.courseRepository = courseRepository;
        this.planRepository = planRepository;
        this.trainingFactory = trainingFactory;
        this.planFactory = planFactory;
        this.trainingValidator = trainingValidator;
        this.planValidator = planValidator;
        this.computingService = computingService;
        this.accessControl = accessControl;
        this.notificationService = notificationService;
        this.auditingService = auditingService;
        this.clock = clock;
    }

    /** matricea de permisiuni: ADMIN, TRAINING sau managerul echipei. */
    private void requireTeamAuthority(Long actorId, Employee subject) {
        accessControl.requireManagerOfOrAnyRole(actorId, subject, Role.HR_ADMIN, Role.HR_TRAINING_COORDINATOR);
    }

    @Override
    public Long createCompetency(Long actorId, String name, String category, String description) {
        accessControl.requireAnyRole(actorId, Role.HR_ADMIN, Role.HR_TRAINING_COORDINATOR);
        Competency competency = trainingFactory.buildCompetency(name, category, description);
        trainingValidator.validateCompetency(competency);
        competencyRepository.add(competency);
        auditingService.audit("Competency", competency.getId(), AuditOperation.CREATED, name);
        return competency.getId();
    }

    // UC-18
    @Override
    public Long assessCompetency(Long assessorId, Long employeeId, Long competencyId, ProficiencyLevel level) {
        Employee employee = employeeRepository.requireById(employeeId);
        requireTeamAuthority(assessorId, employee);
        Competency competency = competencyRepository.requireById(competencyId);
        EmployeeCompetency ec = employeeCompetencyRepository
                .findFirst(x -> x.getEmployee().equals(employee) && x.getCompetency().equals(competency))
                .orElseGet(() -> trainingFactory.buildEmployeeCompetency(employee, competency, level));
        ec.setLevel(level);
        ec.setAssessedOn(LocalDate.now(clock));
        trainingValidator.validateEmployeeCompetency(ec);                 // VR-24
        employeeCompetencyRepository.add(ec);
        notificationService.notifyEmployee(employee, NotificationType.EVALUATION,
                "Your competency '" + competency.getName() + "' was assessed as " + level,
                "EMPLOYEE_COMPETENCY", ec.getId());
        auditingService.audit("EmployeeCompetency", ec.getId(), AuditOperation.UPDATED, competency.getName() + "=" + level);
        return ec.getId();
    }

    // UC-19
    @Override
    public Long identifyTrainingNeed(Long actorId, Long employeeId, Long competencyId,
                                     ProficiencyLevel targetLevel, String justification) {
        Employee employee = employeeRepository.requireById(employeeId);
        requireTeamAuthority(actorId, employee);
        Competency competency = competencyRepository.requireById(competencyId);
        Course suggested = courseRepository
                .findFirst(c -> c.isActive() && c.getTargetCompetencies().contains(competency)).orElse(null);
        TrainingNeed need = trainingFactory.buildTrainingNeed(employee, competency, targetLevel, justification, suggested);
        trainingValidator.validateTrainingNeed(need);
        needRepository.add(need);
        auditingService.audit("TrainingNeed", need.getId(), AuditOperation.CREATED, competency.getName());
        return need.getId();
    }

    // UC-23
    @Override
    public Long createDevelopmentPlan(Long actorId, Long employeeId, String title, String description,
                                      LocalDate startDate, LocalDate endDate) {
        Employee employee = employeeRepository.requireById(employeeId);
        requireTeamAuthority(actorId, employee);
        DevelopmentPlan plan = planFactory.buildDevelopmentPlan(employee, title, description, startDate, endDate);
        planValidator.validatePlan(plan);
        planRepository.add(plan);
        auditingService.audit("DevelopmentPlan", plan.getId(), AuditOperation.CREATED, title);
        return plan.getId();
    }

    private DevelopmentPlan planFor(Long actorId, Long planId, boolean ownerAllowed) {
        DevelopmentPlan plan = planRepository.requireById(planId);
        if (!(ownerAllowed && plan.getEmployee().getId().equals(actorId)))
            requireTeamAuthority(actorId, plan.getEmployee());
        return plan;
    }

    @Override
    public void activatePlan(Long actorId, Long planId) {
        DevelopmentPlan plan = planFor(actorId, planId, false);
        if (plan.getStatus() != PlanStatus.DRAFT)
            throw new BusinessValidationException("Only DRAFT plans can be activated!");
        if (plan.getObjectives().isEmpty())
            throw new BusinessValidationException("A plan needs at least one objective to be activated!");
        plan.setStatus(PlanStatus.ACTIVE);
        planRepository.add(plan);
        notificationService.notifyEmployee(plan.getEmployee(), NotificationType.GENERAL,
                "Development plan '" + plan.getTitle() + "' is now active", "DEVELOPMENT_PLAN", planId);
        auditingService.audit("DevelopmentPlan", planId, AuditOperation.UPDATED, "ACTIVE");
    }

    // UC-24
    @Override
    public Long addObjective(Long actorId, Long planId, String title, String description,
                             String expectedResult, LocalDate dueDate) {
        DevelopmentPlan plan = planFor(actorId, planId, false);
        if (plan.getStatus() == PlanStatus.COMPLETED || plan.getStatus() == PlanStatus.CANCELLED)
            throw new BusinessValidationException("Objectives cannot be added to a " + plan.getStatus() + " plan!");
        Objective objective = plan.addObjective(planFactory.buildObjective(title, description, expectedResult, dueDate));
        try {
            planValidator.validatePlan(plan);
        } catch (RuntimeException ex) {
            plan.removeObjective(objective);          // agregatul ramane consistent
            throw ex;
        }
        computingService.computeProgressOf(plan);
        planRepository.add(plan);
        auditingService.audit("DevelopmentPlan", planId, AuditOperation.UPDATED, "objective added: " + title);
        return objective.getId();
    }

    @Override
    public DevelopmentPlanView updateObjective(Long actorId, Long planId, Long objectiveId,
                                               ObjectiveStatus status, String actualResult) {
        DevelopmentPlan plan = planFor(actorId, planId, true);
        Objective objective = plan.findObjective(objectiveId)
                .orElseThrow(() -> new EntityNotFoundException("Objective", objectiveId));
        objective.setStatus(status);
        objective.setActualResult(actualResult);
        planValidator.validatePlan(plan);
        planRepository.add(computingService.computeProgressOf(plan));      // CR-09
        auditingService.audit("DevelopmentPlan", planId, AuditOperation.UPDATED, "objective " + objectiveId + " -> " + status);
        return computingService.toView(plan);
    }

    @Override
    public void removeObjective(Long actorId, Long planId, Long objectiveId) {
        DevelopmentPlan plan = planFor(actorId, planId, false);
        Objective objective = plan.findObjective(objectiveId)
                .orElseThrow(() -> new EntityNotFoundException("Objective", objectiveId));
        plan.removeObjective(objective);                                  // VR-29
        planRepository.add(computingService.computeProgressOf(plan));
        auditingService.audit("DevelopmentPlan", planId, AuditOperation.UPDATED, "objective removed " + objectiveId);
    }

    @Override
    public DevelopmentPlanView getPlanProgress(Long planId) {
        return computingService.toView(planRepository.requireById(planId));
    }
}
