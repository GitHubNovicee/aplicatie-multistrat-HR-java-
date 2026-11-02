package org.hr.services;

import jakarta.validation.Validator;
import org.hr.domain.recruitment.*;
import org.hr.infrastructure.IApplicationEntityRepository;
import org.hr.infrastructure.ICandidateEntityRepository;
import org.hr.infrastructure.IOfferEntityRepository;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Set;
import java.util.TreeSet;

@Service
public class RecruitmentValidatingServiceImpl extends ValidatingServiceSupport implements IRecruitmentValidatingService {
    private final IApplicationEntityRepository applicationRepository;
    private final ICandidateEntityRepository candidateRepository;
    private final IOfferEntityRepository offerRepository;
    private final IRecruitmentComputingService computingService;
    private final IEmployeeValidatingService employeeValidatingService;
    private final Clock clock;

    public RecruitmentValidatingServiceImpl(Validator validator,
                                            IApplicationEntityRepository applicationRepository,
                                            ICandidateEntityRepository candidateRepository,
                                            IOfferEntityRepository offerRepository,
                                            IRecruitmentComputingService computingService,
                                            IEmployeeValidatingService employeeValidatingService,
                                            Clock clock) {
        super(validator);
        this.applicationRepository = applicationRepository;
        this.candidateRepository = candidateRepository;
        this.offerRepository = offerRepository;
        this.computingService = computingService;
        this.employeeValidatingService = employeeValidatingService;
        this.clock = clock;
    }

    @Override
    public void validateRequest(RecruitmentRequest request) {
        throwIfAny(beanViolations(request));
        employeeValidatingService.validateEmployeeIsActive(request.getRequestedBy());   // VR-28
    }

    @Override
    public void validateOpeningForPublication(JobOpening opening) {
        Set<String> violations = new TreeSet<>(beanViolations(opening));
        if (opening.getRecruitmentRequest() != null
                && opening.getRecruitmentRequest().getStatus() != RequestStatus.APPROVED)
            violations.add("VR-27: A job opening can be published only for an APPROVED recruitment request!");
        if (opening.getClosingDate() != null && opening.getClosingDate().isBefore(LocalDate.now(clock)))
            violations.add("closingDate: Closing date cannot be in the past!");
        throwIfAny(violations);
    }

    @Override
    public void validateCandidate(Candidate candidate) {
        Set<String> violations = new TreeSet<>(beanViolations(candidate));
        if (candidate.getContactInfo() != null && candidate.getContactInfo().getEmail() != null
                && candidateRepository.findFirst(c -> c.getContactInfo().getEmail()
                .equalsIgnoreCase(candidate.getContactInfo().getEmail()) && !c.equals(candidate)).isPresent())
            violations.add("contactInfo.email: A candidate with this email already exists!");
        throwIfAny(violations);
    }

    @Override
    public Set<String> validate(Application application) {
        Set<String> violations = new TreeSet<>(beanViolations(application));
        if (application.getCandidate() != null && application.getJobOpening() != null
                && applicationRepository.findFirst(a -> a.getCandidate().equals(application.getCandidate())
                && a.getJobOpening().equals(application.getJobOpening()) && !a.equals(application)).isPresent())
            violations.add("VR-20: A candidate can apply only once to the same job opening!");
        return violations;
    }

    @Override
    public void validateApplication(Application application) {
        Set<String> violations = new TreeSet<>(validate(application));
        if (application.getJobOpening() != null && application.getJobOpening().getStatus() != OpeningStatus.PUBLISHED)
            violations.add("jobOpening: Applications are accepted only for PUBLISHED job openings!");
        throwIfAny(violations);
    }

    @Override
    public void validateInterview(Interview interview) {
        Set<String> violations = new TreeSet<>(beanViolations(interview));
        if (interview.getScheduledAt() != null && !interview.getScheduledAt().isAfter(LocalDateTime.now(clock)))
            violations.add("scheduledAt: An interview must be scheduled in the future!");
        throwIfAny(violations);
    }

    @Override
    public void validateFeedback(Interview interview, InterviewFeedback feedback) {
        Set<String> violations = new TreeSet<>(beanViolations(feedback));
        if (interview.getStatus() == InterviewStatus.CANCELLED)
            violations.add("status: Feedback cannot be given for a CANCELLED interview!");
        if (feedback.getInterviewer() != null) {
            if (!interview.getInterviewers().contains(feedback.getInterviewer()))
                violations.add("VR-21: The interviewer is not in the list of interviewers of this interview!");
            if (interview.hasFeedbackFrom(feedback.getInterviewer()))
                violations.add("VR-21: An interviewer can give only one feedback per interview!");
        }
        throwIfAny(violations);
    }

    @Override
    public void validateOffer(Offer offer) {
        Set<String> violations = new TreeSet<>(beanViolations(offer));
        if (offer.getApplication() != null
                && offerRepository.findFirst(o -> o.getApplication().equals(offer.getApplication())
                && !o.equals(offer)).isPresent())
            violations.add("VR-22: An application can have at most one offer!");
        if (offer.getValidUntil() != null && offer.getValidUntil().isBefore(LocalDate.now(clock)))
            violations.add("validUntil: An offer cannot be valid until a past date!");
        throwIfAny(violations);
    }

    @Override
    public void validateHire(Application application) {
        RecruitmentRequest request = application.getJobOpening().getRecruitmentRequest();
        if (computingService.computeRemainingPositions(request) <= 0)
            throwIfAny(Set.of("VR-27: The number of hired candidates cannot exceed the headcount ("
                    + request.getHeadcount() + ")!"));
    }
}
