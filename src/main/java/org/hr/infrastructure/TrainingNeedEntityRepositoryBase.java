package org.hr.infrastructure;

import org.hr.domain.training.TrainingNeed;
import org.springframework.stereotype.Repository;

//SDI Bean Component
@Repository
public class TrainingNeedEntityRepositoryBase extends InMemoryEntityRepositoryBase<TrainingNeed> implements ITrainingNeedEntityRepository {
    public TrainingNeedEntityRepositoryBase() {
        super("TrainingNeed");
    }
}
