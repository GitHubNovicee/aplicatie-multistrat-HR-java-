package org.hr.infrastructure;

import org.hr.domain.organization.Employee;
import org.hr.domain.training.TrainingNeed;
import org.hr.domain.training.TrainingNeedStatus;

import java.util.List;

public interface ITrainingNeedEntityRepository extends IEntityRepository<TrainingNeed> {
    List<TrainingNeed> findByEmployee(Employee employee);

    List<TrainingNeed> findByStatus(TrainingNeedStatus status);
}
