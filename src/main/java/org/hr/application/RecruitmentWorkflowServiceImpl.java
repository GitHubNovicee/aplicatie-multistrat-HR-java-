package org.hr.application;

import org.hr.domain.common.BusinessValidationException;
import org.hr.domain.common.HrAuditView.AuditOperation;
import org.hr.domain.notification.NotificationType;
import org.hr.domain.organization.*;
import org.hr.domain.recruitment.*;
import org.hr.infrastructure.*;
import org.hr.services.*;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
public class RecruitmentWorkflowServiceImpl implements IRecruitmentWorkflowService {
    private final IEmployeeEntityRepository employeeRepository;
    private final IDepartmentEntityRepository departmentRepository;
    private final IJobPositionEntityRepository jobPositionRepository;
    private final IRecruitmentRequestEntityRepository requestRepository;
    private final IJobOpeningEntityRepository openingRepository;
    private final ICandidateEntityRepository candidateRepository;
    private final IApplicationEntityRepository applicationRepository;
    private final IInterviewEntityRepository interviewRepository;
    private final IOfferEntityRepository offerRepository;
    private final IUserAccountEntityRepository accountRepository;
    private final IRecruitmentEntityFactory factory;
    private final IEmployeeEntityFactory employeeFactory;
    private final IRecruitmentValidatingService validator;
    private final IEmployeeValidatingService employeeValidator;
    private final IRecruitmentComputingService computingService;
    private final IAccessControlService accessControl;
    private final INotificationService notificationService;
    private final IHrAuditingService auditingService;
    private final Clock clock;

    public RecruitmentWorkflowServiceImpl(IEmployeeEntityRepository employeeRepository,
                                          IDepartmentEntityRepository departmentRepository,
                                          IJobPositionEntityRepository jobPositionRepository,
                                          IRecruitmentRequestEntityRepository requestRepository,
                                          IJobOpeningEntityRepository openingRepository,
                                          ICandidateEntityRepository candidateRepository,
                                          IApplicationEntityRepository applicationRepository,
                                          IInterviewEntityRepository interviewRepository,
                                          IOfferEntityRepository offerRepository,
                                          IUserAccountEntityRepository accountRepository,
                                          IRecruitmentEntityFactory factory,
                                          IEmployeeEntityFactory employeeFactory,
                                          IRecruitmentValidatingService validator,
                                          IEmployeeValidatingService employeeValidator,
                                          IRecruitmentComputingService computingService,
                                          IAccessControlService accessControl,
                                          INotificationService notificationService,
                                          IHrAuditingService auditingService, Clock clock) {
        this.employeeRepository = employeeRepository;
        this.departmentRepository = departmentRepository;
        this.jobPositionRepository = jobPositionRepository;
        this.requestRepository = requestRepository;
        this.openingRepository = openingRepository;
        this.candidateRepository = candidateRepository;
        this.applicationRepository = applicationRepository;
        this.interviewRepository = interviewRepository;
        this.offerRepository = offerRepository;
        this.accountRepository = accountRepository;
        this.factory = factory;
        this.employeeFactory = employeeFactory;
        this.validator = validator;
        this.employeeValidator = employeeValidator;
        this.computingService = computingService;
        this.accessControl = accessControl;
        this.notificationService = notificationService;
        this.auditingService = auditingService;
        this.clock = clock;
    }

    private static void requireStatus(boolean ok, String message) {
        if (!ok)
            throw new BusinessValidationException(message);
    }

    // UC-04
    @Override
    public Long createAndSubmitRecruitmentRequest(Long managerId, Long departmentId, Long jobPositionId,
                                                  int headcount, String justification) {
        accessControl.requireAnyRole(managerId, Role.MANAGER);
        RecruitmentRequest request = factory.buildRecruitmentRequest(employeeRepository.requireById(managerId),
                departmentRepository.requireById(departmentId), jobPositionRepository.requireById(jobPositionId),
                headcount, justification);
        request.setStatus(RequestStatus.SUBMITTED);
        validator.validateRequest(request);
        computingService.computeRemainingPositionsOf(request);
        requestRepository.add(request);
        notificationService.notifyRole(Role.HR_ADMIN, NotificationType.APPROVAL,
                "Recruitment request #" + request.getId() + " awaits approval", "RECRUITMENT_REQUEST", request.getId());
        auditingService.audit("RecruitmentRequest", request.getId(), AuditOperation.SUBMITTED, "headcount=" + headcount);
        return request.getId();
    }

    // UC-05
    @Override
    public RequestStatus decideRecruitmentRequest(Long adminId, Long requestId, boolean approve) {
        accessControl.requireAnyRole(adminId, Role.HR_ADMIN);
        RecruitmentRequest request = requestRepository.requireById(requestId);
        requireStatus(request.getStatus() == RequestStatus.SUBMITTED, "Only SUBMITTED requests can be decided!");
        request.setStatus(approve ? RequestStatus.APPROVED : RequestStatus.REJECTED);
        requestRepository.add(request);
        notificationService.notifyEmployee(request.getRequestedBy(), NotificationType.APPROVAL,
                "Recruitment request #" + requestId + " was " + request.getStatus(), "RECRUITMENT_REQUEST", requestId);
        auditingService.audit("RecruitmentRequest", requestId,
                approve ? AuditOperation.APPROVED : AuditOperation.REJECTED, "by " + adminId);
        return request.getStatus();
    }

    // UC-06
    @Override
    public Long publishJobOpening(Long recruiterId, Long requestId, String title, String description, LocalDate closingDate) {
        accessControl.requireAnyRole(recruiterId, Role.HR_RECRUITER, Role.HR_ADMIN);
        JobOpening opening = factory.buildJobOpening(requestRepository.requireById(requestId), title, description, closingDate);
        validator.validateOpeningForPublication(opening);
        opening.setStatus(OpeningStatus.PUBLISHED);
        opening.setPublishedOn(LocalDate.now(clock));
        openingRepository.add(opening);
        auditingService.audit("JobOpening", opening.getId(), AuditOperation.PUBLISHED, opening.getReference());
        return opening.getId();
    }

    // UC-07
    @Override
    public Long registerApplication(Long recruiterId, Long openingId, String firstName, String lastName,
                                    String email, String phone, String cvPath) {
        accessControl.requireAnyRole(recruiterId, Role.HR_RECRUITER, Role.HR_ADMIN);
        JobOpening opening = openingRepository.requireById(openingId);
        Candidate candidate = candidateRepository
                .findFirst(c -> c.getContactInfo().getEmail().equalsIgnoreCase(email))
                .orElseGet(() -> {
                    Candidate created = factory.buildCandidate(firstName, lastName, email, phone, cvPath);
                    validator.validateCandidate(created);
                    return candidateRepository.add(created);
                });
        Application application = factory.buildApplication(candidate, opening);
        validator.validateApplication(application);                       // VR-20
        applicationRepository.add(application);
        auditingService.audit("Application", application.getId(), AuditOperation.CREATED, email);
        return application.getId();
    }

    // UC-08
    @Override
    public ApplicationStatus screenApplication(Long recruiterId, Long applicationId, boolean passed) {
        accessControl.requireAnyRole(recruiterId, Role.HR_RECRUITER, Role.HR_ADMIN);
        Application application = applicationRepository.requireById(applicationId);
        requireStatus(application.getStatus() == ApplicationStatus.RECEIVED, "Only RECEIVED applications can be screened!");
        application.setStatus(passed ? ApplicationStatus.SCREENING : ApplicationStatus.REJECTED);
        applicationRepository.add(application);
        auditingService.audit("Application", applicationId,
                passed ? AuditOperation.UPDATED : AuditOperation.REJECTED, "screening");
        return application.getStatus();
    }

    // UC-09
    @Override
    public Long scheduleInterview(Long recruiterId, Long applicationId, LocalDateTime scheduledAt,
                                  String location, Set<Long> interviewerIds) {
        accessControl.requireAnyRole(recruiterId, Role.HR_RECRUITER, Role.HR_ADMIN);
        Application application = applicationRepository.requireById(applicationId);
        requireStatus(application.getStatus() == ApplicationStatus.SCREENING
                || application.getStatus() == ApplicationStatus.INTERVIEW,
                "Interviews can be scheduled only for SCREENING/INTERVIEW applications!");
        Set<Employee> interviewers = new HashSet<>();
        for (Long id : interviewerIds) {
            accessControl.requireAnyRole(id, Role.INTERVIEWER);
            interviewers.add(employeeRepository.requireById(id));
        }
        int round = interviewRepository.find(i -> i.getApplication().equals(application)).size() + 1;
        Interview interview = factory.buildInterview(application, scheduledAt, location, interviewers, round);
        validator.validateInterview(interview);
        interviewRepository.add(interview);
        application.setStatus(ApplicationStatus.INTERVIEW);
        applicationRepository.add(application);
        for (Employee interviewer : interviewers)
            notificationService.notifyEmployee(interviewer, NotificationType.INTERVIEW,
                    "Interview scheduled on " + scheduledAt, "INTERVIEW", interview.getId());
        auditingService.audit("Interview", interview.getId(), AuditOperation.SCHEDULED, "round " + round);
        return interview.getId();
    }

    // UC-10
    @Override
    public Long submitInterviewFeedback(Long interviewerId, Long interviewId, int score, String comments,
                                        Recommendation recommendation) {
        accessControl.requireAnyRole(interviewerId, Role.INTERVIEWER);
        Interview interview = interviewRepository.requireById(interviewId);
        InterviewFeedback feedback = factory.buildFeedback(employeeRepository.requireById(interviewerId),
                score, comments, recommendation);
        validator.validateFeedback(interview, feedback);                  // VR-21
        interview.addFeedback(feedback);
        interviewRepository.add(interview);
        applicationRepository.add(computingService.computeAverageScoreOf(interview.getApplication()));
        auditingService.audit("Interview", interviewId, AuditOperation.UPDATED, "feedback by " + interviewerId);
        return feedback.getId();
    }

    @Override
    public void completeInterview(Long recruiterId, Long interviewId) {
        accessControl.requireAnyRole(recruiterId, Role.HR_RECRUITER, Role.HR_ADMIN);
        Interview interview = interviewRepository.requireById(interviewId);
        requireStatus(interview.getStatus() == InterviewStatus.SCHEDULED, "Only SCHEDULED interviews can be completed!");
        interview.setStatus(InterviewStatus.COMPLETED);
        interviewRepository.add(interview);
        applicationRepository.add(computingService.computeAverageScoreOf(interview.getApplication()));   // CR-06
        auditingService.audit("Interview", interviewId, AuditOperation.COMPLETED, "round " + interview.getRoundNumber());
    }

    // UC-11
    @Override
    public Long issueOffer(Long recruiterId, Long applicationId, BigDecimal proposedSalary,
                           LocalDate startDate, LocalDate validUntil) {
        accessControl.requireAnyRole(recruiterId, Role.HR_RECRUITER, Role.HR_ADMIN);
        Application application = applicationRepository.requireById(applicationId);
        requireStatus(application.getStatus() == ApplicationStatus.INTERVIEW,
                "Offers can be issued only for applications in INTERVIEW stage!");
        Offer offer = factory.buildOffer(application, proposedSalary, startDate, validUntil);
        validator.validateOffer(offer);                                   // VR-22
        offer.setStatus(OfferStatus.SENT);
        offerRepository.add(offer);
        application.setStatus(ApplicationStatus.OFFER);
        applicationRepository.add(application);
        auditingService.audit("Offer", offer.getId(), AuditOperation.CREATED, "valid until " + validUntil);
        return offer.getId();
    }

    // UC-12
    @Override
    public Long registerOfferResponse(Long recruiterId, Long offerId, boolean accepted) {
        accessControl.requireAnyRole(recruiterId, Role.HR_RECRUITER, Role.HR_ADMIN);
        Offer offer = offerRepository.requireById(offerId);
        requireStatus(offer.getStatus() == OfferStatus.SENT, "Only SENT offers can receive a response!");
        if (computingService.isOfferExpired(offer)) {                     // CR-10
            offer.setStatus(OfferStatus.EXPIRED);
            offerRepository.add(offer);
            throw new BusinessValidationException("The offer expired on " + offer.getValidUntil() + "!");
        }
        Application application = offer.getApplication();
        offer.setRespondedOn(LocalDate.now(clock));
        if (!accepted) {
            offer.setStatus(OfferStatus.DECLINED);
            application.setStatus(ApplicationStatus.WITHDRAWN);
            offerRepository.add(offer);
            applicationRepository.add(application);
            auditingService.audit("Offer", offerId, AuditOperation.REJECTED, "declined by candidate");
            return null;
        }
        validator.validateHire(application);                              // VR-27
        RecruitmentRequest request = application.getJobOpening().getRecruitmentRequest();
        Candidate candidate = application.getCandidate();
        Employee employee = employeeFactory.buildEmployee(candidate.getFirstName(), candidate.getLastName(),
                candidate.getContactInfo().getEmail(), candidate.getContactInfo().getPhone(),
                offer.getStartDate() != null ? offer.getStartDate() : LocalDate.now(clock),
                request.getDepartment(), request.getJobPosition(), request.getRequestedBy());
        employeeValidator.validateWithException(employee);
        UserAccount account = employeeFactory.buildUserAccount(employee, candidate.getContactInfo().getEmail(),
                "{reset-required}", Set.of(Role.EMPLOYEE));
        employeeValidator.validateWithException(account);
        employeeRepository.add(employee);
        accountRepository.add(account);

        offer.setStatus(OfferStatus.ACCEPTED);
        application.setStatus(ApplicationStatus.HIRED);
        offerRepository.add(offer);
        applicationRepository.add(application);
        if (computingService.computeRemainingPositionsOf(request).getRemainingPositions() == 0) {
            request.setStatus(RequestStatus.CLOSED);
            application.getJobOpening().setStatus(OpeningStatus.CLOSED);
            openingRepository.add(application.getJobOpening());
        }
        requestRepository.add(request);
        auditingService.audit("Application", application.getId(), AuditOperation.HIRED, employee.getEmployeeCode());
        return employee.getId();
    }

    // CR-10 / UC-28
    @Override
    public int expireOverdueOffers() {
        List<Offer> overdue = offerRepository.find(computingService::isOfferExpired);
        for (Offer offer : overdue) {
            offer.setStatus(OfferStatus.EXPIRED);
            offerRepository.add(offer);
            notificationService.notifyRole(Role.HR_RECRUITER, NotificationType.DEADLINE,
                    "Offer #" + offer.getId() + " expired", "OFFER", offer.getId());
            auditingService.audit("Offer", offer.getId(), AuditOperation.EXPIRED, "validUntil " + offer.getValidUntil());
        }
        return overdue.size();
    }

    @Override
    public ApplicationScoreView getApplicationScore(Long applicationId) {
        return computingService.toScoreView(applicationRepository.requireById(applicationId));
    }
}
