package org.hr.services;

import org.hr.domain.recruitment.*;
import org.hr.infrastructure.IApplicationEntityRepository;
import org.hr.infrastructure.IInterviewEntityRepository;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.LocalDate;
import java.util.List;

@Service
public class RecruitmentComputingServiceImpl implements IRecruitmentComputingService {
    private final IInterviewEntityRepository interviewRepository;
    private final IApplicationEntityRepository applicationRepository;
    private final Clock clock;

    public RecruitmentComputingServiceImpl(IInterviewEntityRepository interviewRepository,
                                           IApplicationEntityRepository applicationRepository,
                                           Clock clock) {
        this.interviewRepository = interviewRepository;
        this.applicationRepository = applicationRepository;
        this.clock = clock;
    }

    private List<Interview> completedInterviewsOf(Application application) {
        return interviewRepository.find(i -> i.getApplication().equals(application)
                && i.getStatus() == InterviewStatus.COMPLETED);
    }

    @Override
    public Double computeAverageScore(Application application) {
        return completedInterviewsOf(application).stream()
                .flatMap(i -> i.getFeedbacks().stream())
                .mapToInt(InterviewFeedback::getScore)
                .average()
                .stream().boxed().findFirst().orElse(null);
    }

    @Override
    public Application computeAverageScoreOf(Application application) {
        application.setAverageScore(computeAverageScore(application));
        return application;
    }

    @Override
    public int computeRemainingPositions(RecruitmentRequest request) {
        long hired = applicationRepository.find(a -> a.getStatus() == ApplicationStatus.HIRED
                && a.getJobOpening().getRecruitmentRequest().equals(request)).size();
        return request.getHeadcount() - (int) hired;
    }

    @Override
    public RecruitmentRequest computeRemainingPositionsOf(RecruitmentRequest request) {
        request.setRemainingPositions(computeRemainingPositions(request));
        return request;
    }

    @Override
    public boolean isOfferExpired(Offer offer) {
        return offer.getStatus() == OfferStatus.SENT && offer.getValidUntil().isBefore(LocalDate.now(clock));
    }

    @Override
    public ApplicationScoreView toScoreView(Application application) {
        computeAverageScoreOf(application);
        return new ApplicationScoreView(
                application.getId(),
                application.getCandidate().getFirstName() + " " + application.getCandidate().getLastName(),
                application.getJobOpening().getReference(),
                application.getStatus(),
                application.getAverageScore(),
                completedInterviewsOf(application).size());
    }
}
