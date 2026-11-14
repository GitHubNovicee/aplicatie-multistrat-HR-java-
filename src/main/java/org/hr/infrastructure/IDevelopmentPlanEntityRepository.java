package org.hr.infrastructure;

import org.hr.domain.development.DevelopmentPlan;
import org.hr.domain.development.ObjectiveStatus;
import org.hr.domain.development.PlanStatus;
import org.hr.domain.organization.Employee;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface IDevelopmentPlanEntityRepository extends IEntityRepository<DevelopmentPlan> {
    List<DevelopmentPlan> findByEmployee(Employee employee);

    List<DevelopmentPlan> findByStatus(PlanStatus status);

    /** Filtru personalizat: planurile care contin cel putin un obiectiv in starea data. */
    @Query("select distinct p from DevelopmentPlan p join p.objectives o where o.status = :status")
    List<DevelopmentPlan> findWithObjectiveStatus(@Param("status") ObjectiveStatus status);
}
