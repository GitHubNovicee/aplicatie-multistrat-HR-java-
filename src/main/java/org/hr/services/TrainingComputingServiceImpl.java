package org.hr.services;

import org.hr.domain.training.Enrollment;
import org.hr.domain.training.EnrollmentStatus;
import org.hr.domain.training.TrainingSession;
import org.hr.domain.training.TrainingSessionView;
import org.springframework.stereotype.Service;

@Service
public class TrainingComputingServiceImpl implements ITrainingComputingService {

    private static boolean occupiesSeat(Enrollment enrollment) {
        return enrollment.getStatus() == EnrollmentStatus.ENROLLED || enrollment.getStatus() == EnrollmentStatus.ATTENDED;
    }

    @Override
    public int computeAvailableSeats(TrainingSession session) {
        long occupied = session.getEnrollments().stream().filter(TrainingComputingServiceImpl::occupiesSeat).count();
        return session.getCapacity() - (int) occupied;
    }

    @Override
    public TrainingSession computeAvailableSeatsOf(TrainingSession session) {
        session.setAvailableSeats(computeAvailableSeats(session));
        return session;
    }

    @Override
    public TrainingSessionView toView(TrainingSession session) {
        computeAvailableSeatsOf(session);
        return new TrainingSessionView(
                session.getId(), session.getCourse().getCode(), session.getCourse().getTitle(),
                session.getStatus(), session.getCapacity(), session.getAvailableSeats(),
                (int) session.getEnrollments().stream().filter(TrainingComputingServiceImpl::occupiesSeat).count());
    }
}
