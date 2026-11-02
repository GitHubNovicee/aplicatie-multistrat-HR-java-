package org.hr.infrastructure;

import org.hr.domain.development.DevelopmentPlan;
import org.hr.domain.development.Objective;
import org.hr.domain.organization.Employee;

import java.time.LocalDate;

/** Factory pentru agregatul DevelopmentPlan (RQ-09). */
public interface IDevelopmentPlanEntityFactory {
    /** Plan DRAFT fara obiective. */
    DevelopmentPlan buildDevelopmentPlan(Employee employee, String title, String description,
                                         LocalDate startDate, LocalDate endDate);

    /** Componenta (id local atribuit de DevelopmentPlan.addObjective). */
    Objective buildObjective(String title, String description, String expectedResult, LocalDate dueDate);
}
