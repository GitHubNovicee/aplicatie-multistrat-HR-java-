package org.hr.services;

import jakarta.validation.Validator;
import org.hr.domain.development.DevelopmentPlan;
import org.hr.domain.development.Objective;
import org.springframework.stereotype.Service;

import java.util.Set;
import java.util.TreeSet;

@Service
public class DevelopmentPlanValidatingServiceImpl extends ValidatingServiceSupport implements IDevelopmentPlanValidatingService {
    private final IEmployeeValidatingService employeeValidatingService;

    public DevelopmentPlanValidatingServiceImpl(Validator validator, IEmployeeValidatingService employeeValidatingService) {
        super(validator);
        this.employeeValidatingService = employeeValidatingService;
    }

    @Override
    public void validatePlan(DevelopmentPlan plan) {
        Set<String> violations = new TreeSet<>(beanViolations(plan));
        if (violations.isEmpty() && plan.getPeriod() != null) {
            for (Objective objective : plan.getObjectives())
                if (objective.getDueDate().isAfter(plan.getPeriod().getEndDate()))
                    violations.add("objectives: Objective '" + objective.getTitle()
                            + "' is due after the end of the plan!");
        }
        throwIfAny(violations);
        employeeValidatingService.validateEmployeeIsActive(plan.getEmployee());   // VR-28
    }
}
