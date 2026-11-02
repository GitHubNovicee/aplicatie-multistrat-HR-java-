package org.hr.infrastructure;

import org.hr.domain.common.DateRange;
import org.hr.domain.development.DevelopmentPlan;
import org.hr.domain.development.Objective;
import org.hr.domain.development.ObjectiveStatus;
import org.hr.domain.development.PlanStatus;
import org.hr.domain.organization.Employee;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Component
public class DevelopmentPlanEntityFactoryBase implements IDevelopmentPlanEntityFactory {
    private final IDevelopmentPlanEntityRepository planRepository;

    public DevelopmentPlanEntityFactoryBase(IDevelopmentPlanEntityRepository planRepository) {
        this.planRepository = planRepository;
    }

    @Override
    public DevelopmentPlan buildDevelopmentPlan(Employee employee, String title, String description,
                                                LocalDate startDate, LocalDate endDate) {
        DevelopmentPlan plan = new DevelopmentPlan();
        plan.setId(planRepository.getNextID());
        plan.setEmployee(employee);
        plan.setTitle(title);
        plan.setDescription(description);
        plan.setPeriod(DateRange.of(startDate, endDate));
        plan.setStatus(PlanStatus.DRAFT);
        plan.setProgress(0.0);
        return plan;
    }

    @Override
    public Objective buildObjective(String title, String description, String expectedResult, LocalDate dueDate) {
        Objective objective = new Objective();
        objective.setTitle(title);
        objective.setDescription(description);
        objective.setExpectedResult(expectedResult);
        objective.setDueDate(dueDate);
        objective.setStatus(ObjectiveStatus.NOT_STARTED);
        return objective;
    }
}
