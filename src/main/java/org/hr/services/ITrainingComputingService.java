package org.hr.services;

import org.hr.domain.training.TrainingSession;
import org.hr.domain.training.TrainingSessionView;

/** Domain Computation Service (CR-05) - modulul Training. */
public interface ITrainingComputingService {
    /** CR-05: locuri disponibile = capacity - inscrieri ENROLLED sau ATTENDED. */
    int computeAvailableSeats(TrainingSession session);

    /** CR-05: completeaza campul calculat TrainingSession.availableSeats. */
    TrainingSession computeAvailableSeatsOf(TrainingSession session);

    TrainingSessionView toView(TrainingSession session);
}
