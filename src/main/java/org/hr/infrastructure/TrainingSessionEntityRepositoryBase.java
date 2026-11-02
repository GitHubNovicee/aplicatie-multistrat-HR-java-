package org.hr.infrastructure;

import org.hr.domain.training.TrainingSession;
import org.springframework.stereotype.Repository;

//SDI Bean Component
@Repository
public class TrainingSessionEntityRepositoryBase extends InMemoryEntityRepositoryBase<TrainingSession> implements ITrainingSessionEntityRepository {
    public TrainingSessionEntityRepositoryBase() {
        super("TrainingSession");
    }
}
