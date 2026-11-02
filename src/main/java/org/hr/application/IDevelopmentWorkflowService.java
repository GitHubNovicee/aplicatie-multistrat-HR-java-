package org.hr.application;

import org.hr.domain.development.DevelopmentPlanView;
import org.hr.domain.development.ObjectiveStatus;
import org.hr.domain.training.ProficiencyLevel;

import java.time.LocalDate;


public interface IDevelopmentWorkflowService {
    Long createCompetency(Long actorId, String name, String category, String description);

    Long assessCompetency(Long assessorId, Long employeeId, Long competencyId, ProficiencyLevel level);

    Long identifyTrainingNeed(Long actorId, Long employeeId, Long competencyId, ProficiencyLevel targetLevel, String justification);

    Long createDevelopmentPlan(Long actorId, Long employeeId, String title, String description,
                               LocalDate startDate, LocalDate endDate);

    void activatePlan(Long actorId, Long planId);

    Long addObjective(Long actorId, Long planId, String title, String description, String expectedResult, LocalDate dueDate);

    DevelopmentPlanView updateObjective(Long actorId, Long planId, Long objectiveId, ObjectiveStatus status, String actualResult);

    void removeObjective(Long actorId, Long planId, Long objectiveId);

    DevelopmentPlanView getPlanProgress(Long planId);
}
