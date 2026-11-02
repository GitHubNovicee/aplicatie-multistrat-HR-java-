package org.hr.infrastructure;

import org.hr.domain.common.ContactInfo;
import org.hr.domain.organization.Department;
import org.hr.domain.organization.Employee;
import org.hr.domain.organization.JobPosition;
import org.hr.domain.recruitment.*;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Component
public class RecruitmentEntityFactoryBase implements IRecruitmentEntityFactory {
    private final IRecruitmentRequestEntityRepository requestRepository;
    private final IJobOpeningEntityRepository openingRepository;
    private final ICandidateEntityRepository candidateRepository;
    private final IApplicationEntityRepository applicationRepository;
    private final IInterviewEntityRepository interviewRepository;
    private final IOfferEntityRepository offerRepository;
    private final Clock clock;

    public RecruitmentEntityFactoryBase(IRecruitmentRequestEntityRepository requestRepository,
                                        IJobOpeningEntityRepository openingRepository,
                                        ICandidateEntityRepository candidateRepository,
                                        IApplicationEntityRepository applicationRepository,
                                        IInterviewEntityRepository interviewRepository,
                                        IOfferEntityRepository offerRepository,
                                        Clock clock) {
        this.requestRepository = requestRepository;
        this.openingRepository = openingRepository;
        this.candidateRepository = candidateRepository;
        this.applicationRepository = applicationRepository;
        this.interviewRepository = interviewRepository;
        this.offerRepository = offerRepository;
        this.clock = clock;
    }

    @Override
    public RecruitmentRequest buildRecruitmentRequest(Employee requestedBy, Department department,
                                                      JobPosition jobPosition, int headcount, String justification) {
        RecruitmentRequest request = new RecruitmentRequest();
        request.setId(requestRepository.getNextID());
        request.setRequestedBy(requestedBy);
        request.setDepartment(department);
        request.setJobPosition(jobPosition);
        request.setHeadcount(headcount);
        request.setJustification(justification);
        request.setStatus(RequestStatus.DRAFT);
        return request;
    }

    @Override
    public JobOpening buildJobOpening(RecruitmentRequest request, String title, String description, LocalDate closingDate) {
        JobOpening opening = new JobOpening();
        opening.setId(openingRepository.getNextID());
        opening.setReference(String.format("JOB-%d-%04d", LocalDate.now(clock).getYear(), opening.getId()));
        opening.setRecruitmentRequest(request);
        opening.setTitle(title);
        opening.setDescription(description);
        opening.setClosingDate(closingDate);
        opening.setStatus(OpeningStatus.DRAFT);
        return opening;
    }

    @Override
    public Candidate buildCandidate(String firstName, String lastName, String email, String phone, String cvPath) {
        Candidate candidate = new Candidate();
        candidate.setId(candidateRepository.getNextID());
        candidate.setFirstName(firstName);
        candidate.setLastName(lastName);
        candidate.setContactInfo(ContactInfo.of(email, phone));
        candidate.setCvPath(cvPath);
        return candidate;
    }

    @Override
    public Application buildApplication(Candidate candidate, JobOpening opening) {
        Application application = new Application();
        application.setId(applicationRepository.getNextID());
        application.setCandidate(candidate);
        application.setJobOpening(opening);
        application.setAppliedOn(LocalDate.now(clock));
        application.setStatus(ApplicationStatus.RECEIVED);
        return application;
    }

    @Override
    public Interview buildInterview(Application application, LocalDateTime scheduledAt, String location,
                                    Set<Employee> interviewers, int roundNumber) {
        Interview interview = new Interview();
        interview.setId(interviewRepository.getNextID());
        interview.setApplication(application);
        interview.setScheduledAt(scheduledAt);
        interview.setLocation(location);
        interview.setInterviewers(interviewers == null ? new HashSet<>() : new HashSet<>(interviewers));
        interview.setRoundNumber(roundNumber);
        interview.setStatus(InterviewStatus.SCHEDULED);
        return interview;
    }

    @Override
    public InterviewFeedback buildFeedback(Employee interviewer, int score, String comments, Recommendation recommendation) {
        InterviewFeedback feedback = new InterviewFeedback();
        feedback.setInterviewer(interviewer);
        feedback.setScore(score);
        feedback.setComments(comments);
        feedback.setRecommendation(recommendation);
        return feedback;
    }

    @Override
    public Offer buildOffer(Application application, BigDecimal proposedSalary, LocalDate startDate, LocalDate validUntil) {
        Offer offer = new Offer();
        offer.setId(offerRepository.getNextID());
        offer.setApplication(application);
        offer.setProposedSalary(proposedSalary);
        offer.setStartDate(startDate);
        offer.setValidUntil(validUntil);
        offer.setStatus(OfferStatus.DRAFT);
        return offer;
    }
}
