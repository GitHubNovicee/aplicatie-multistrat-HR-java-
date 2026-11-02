package org.hr.application;

import org.hr.HrTestSupport;
import org.hr.domain.common.AccessDeniedBusinessException;
import org.hr.domain.common.BusinessValidationException;
import org.hr.domain.organization.Employee;
import org.hr.domain.organization.Role;
import org.hr.domain.recruitment.*;
import org.hr.infrastructure.*;
import org.hr.services.INotificationService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/**
 * WORKFLOW Business Service Tests - flux recrutare UC-04 .. UC-12.
 * Strategy: Simple-Facade (workflow service injectat prin component scan).
 */
public class Test3_RecruitmentWorkflowService extends HrTestSupport {
    @Autowired private IRecruitmentWorkflowService workflow;
    @Autowired private IRecruitmentRequestEntityRepository requestRepository;
    @Autowired private IJobOpeningEntityRepository openingRepository;
    @Autowired private IApplicationEntityRepository applicationRepository;
    @Autowired private IInterviewEntityRepository interviewRepository;
    @Autowired private IOfferEntityRepository offerRepository;
    @Autowired private INotificationService notificationService;

    private Long manager() { return idOf(IHrSampleDataFactory.MANAGER_USERNAME); }
    private Long recruiter() { return idOf(IHrSampleDataFactory.RECRUITER_USERNAME); }
    private Long interviewer() { return idOf(IHrSampleDataFactory.INTERVIEWER_USERNAME); }

    private Long approvedRequest(int headcount) {
        Long requestId = workflow.createAndSubmitRecruitmentRequest(manager(), engineering().getId(),
                softwareEngineer().getId(), headcount, "growth");
        assertEquals(RequestStatus.APPROVED, workflow.decideRecruitmentRequest(adminId(), requestId, true));
        return requestId;
    }

    private Long interviewedApplication(Long openingId, String email) {
        Long applicationId = workflow.registerApplication(recruiter(), openingId, "Cand", "Idate", email, "0733333333", "cv.pdf");
        assertEquals(ApplicationStatus.SCREENING, workflow.screenApplication(recruiter(), applicationId, true));
        return applicationId;
    }

    @Test
    void fullRecruitmentFlow_requestToHire() {
        // UC-04 / UC-05
        Long requestId = workflow.createAndSubmitRecruitmentRequest(manager(), engineering().getId(),
                softwareEngineer().getId(), 1, "new feature team");
        assertEquals(RequestStatus.SUBMITTED, requestRepository.requireById(requestId).getStatus());
        assertFalse(notificationService.getNotificationsOf(adminId()).isEmpty(), "HR_ADMIN must be notified");
        workflow.decideRecruitmentRequest(adminId(), requestId, true);

        // UC-06
        Long openingId = workflow.publishJobOpening(recruiter(), requestId, "Java developer", "desc", LocalDate.now().plusMonths(1));
        JobOpening opening = openingRepository.requireById(openingId);
        assertEquals(OpeningStatus.PUBLISHED, opening.getStatus());
        assertTrue(opening.getReference().startsWith("JOB-"));

        // UC-07, UC-08
        String email = "cand" + System.nanoTime() + "@mail.example";
        Long applicationId = interviewedApplication(openingId, email);

        // UC-09
        Long interviewId = workflow.scheduleInterview(recruiter(), applicationId, LocalDateTime.now().plusDays(3),
                "Room 1", Set.of(interviewer()));
        assertEquals(ApplicationStatus.INTERVIEW, applicationRepository.requireById(applicationId).getStatus());
        assertEquals(1, interviewRepository.requireById(interviewId).getRoundNumber());
        assertTrue(notificationService.getNotificationsOf(interviewer()).stream()
                .anyMatch(n -> n.getReferenceId().equals(interviewId)));

        // UC-10 + CR-06
        workflow.submitInterviewFeedback(interviewer(), interviewId, 8, "good", Recommendation.YES);
        assertThrows(BusinessValidationException.class,                                   // VR-21
                () -> workflow.submitInterviewFeedback(interviewer(), interviewId, 9, "again", Recommendation.YES));
        workflow.completeInterview(recruiter(), interviewId);
        ApplicationScoreView score = workflow.getApplicationScore(applicationId);
        assertEquals(8.0, score.getAverageScore());
        assertEquals(1, score.getCompletedInterviews());

        // UC-11 + VR-22
        Long offerId = workflow.issueOffer(recruiter(), applicationId, new BigDecimal("12000.00"),
                LocalDate.now().plusMonths(1), LocalDate.now().plusDays(10));
        assertEquals(OfferStatus.SENT, offerRepository.requireById(offerId).getStatus());
        assertThrows(BusinessValidationException.class, () -> workflow.issueOffer(recruiter(), applicationId,
                new BigDecimal("1.00"), LocalDate.now().plusMonths(1), LocalDate.now().plusDays(10)));

        // UC-12: HIRED, angajat + cont create, cererea si anuntul se inchid (CR-07)
        Long employeeId = workflow.registerOfferResponse(recruiter(), offerId, true);
        Employee hired = employee(employeeId);
        assertEquals(email, hired.getContactInfo().getEmail());
        assertEquals(manager(), hired.getManager().getId());
        assertTrue(accountRepository.findFirst(a -> a.getEmployee().equals(hired)).isPresent());
        assertEquals(ApplicationStatus.HIRED, applicationRepository.requireById(applicationId).getStatus());
        assertEquals(RequestStatus.CLOSED, requestRepository.requireById(requestId).getStatus());
        assertEquals(0, requestRepository.requireById(requestId).getRemainingPositions());
        assertEquals(OpeningStatus.CLOSED, openingRepository.requireById(openingId).getStatus());
    }

    @Test
    void vr27_openingOnlyForApprovedRequest() {
        Long requestId = workflow.createAndSubmitRecruitmentRequest(manager(), engineering().getId(),
                softwareEngineer().getId(), 1, "x");
        BusinessValidationException ex = assertThrows(BusinessValidationException.class,
                () -> workflow.publishJobOpening(recruiter(), requestId, "t", "d", LocalDate.now().plusDays(5)));
        assertTrue(ex.getMessage().contains("VR-27"));
        workflow.decideRecruitmentRequest(adminId(), requestId, false);
        assertThrows(BusinessValidationException.class, () -> workflow.decideRecruitmentRequest(adminId(), requestId, true));
    }

    @Test
    void vr20_candidateAppliesOnlyOncePerOpening() {
        Long openingId = workflow.publishJobOpening(recruiter(), approvedRequest(2), "Dev", "d", LocalDate.now().plusDays(20));
        String email = "dup" + System.nanoTime() + "@mail.example";
        workflow.registerApplication(recruiter(), openingId, "A", "B", email, null, null);
        BusinessValidationException ex = assertThrows(BusinessValidationException.class,
                () -> workflow.registerApplication(recruiter(), openingId, "A", "B", email, null, null));
        assertTrue(ex.getMessage().contains("VR-20"));
    }

    @Test
    void vr27_hiresCannotExceedHeadcount() {
        Long requestId = approvedRequest(1);
        Long openingId = workflow.publishJobOpening(recruiter(), requestId, "Dev", "d", LocalDate.now().plusDays(20));
        Long app1 = interviewedApplication(openingId, "first" + System.nanoTime() + "@mail.example");
        Long app2 = interviewedApplication(openingId, "second" + System.nanoTime() + "@mail.example");
        for (Long app : new Long[]{app1, app2})
            workflow.scheduleInterview(recruiter(), app, LocalDateTime.now().plusDays(2), "r", Set.of(interviewer()));
        Long offer1 = workflow.issueOffer(recruiter(), app1, BigDecimal.TEN, LocalDate.now().plusDays(30), LocalDate.now().plusDays(5));
        Long offer2 = workflow.issueOffer(recruiter(), app2, BigDecimal.TEN, LocalDate.now().plusDays(30), LocalDate.now().plusDays(5));
        assertNotNull(workflow.registerOfferResponse(recruiter(), offer1, true));
        BusinessValidationException ex = assertThrows(BusinessValidationException.class,
                () -> workflow.registerOfferResponse(recruiter(), offer2, true));
        assertTrue(ex.getMessage().contains("VR-27"));
    }

    @Test
    void declinedOfferWithdrawsApplicationAndOverdueOffersExpire() {
        Long openingId = workflow.publishJobOpening(recruiter(), approvedRequest(3), "Dev", "d", LocalDate.now().plusDays(20));
        Long app1 = interviewedApplication(openingId, "decl" + System.nanoTime() + "@mail.example");
        Long app2 = interviewedApplication(openingId, "exp" + System.nanoTime() + "@mail.example");
        for (Long app : new Long[]{app1, app2})
            workflow.scheduleInterview(recruiter(), app, LocalDateTime.now().plusDays(2), "r", Set.of(interviewer()));
        Long declined = workflow.issueOffer(recruiter(), app1, BigDecimal.TEN, null, LocalDate.now().plusDays(5));
        Long overdue = workflow.issueOffer(recruiter(), app2, BigDecimal.TEN, null, LocalDate.now().plusDays(5));

        assertNull(workflow.registerOfferResponse(recruiter(), declined, false));
        assertEquals(ApplicationStatus.WITHDRAWN, applicationRepository.requireById(app1).getStatus());

        offerRepository.requireById(overdue).setValidUntil(LocalDate.now().minusDays(1));   // CR-10
        assertTrue(workflow.expireOverdueOffers() >= 1);
        assertEquals(OfferStatus.EXPIRED, offerRepository.requireById(overdue).getStatus());
    }

    @Test
    void permissions_onlyManagersCreateRequestsAndOnlyAdminApproves() {
        Long plainEmployee = newEmployee(Role.EMPLOYEE);
        assertThrows(AccessDeniedBusinessException.class, () -> workflow.createAndSubmitRecruitmentRequest(
                plainEmployee, engineering().getId(), softwareEngineer().getId(), 1, "x"));
        Long requestId = workflow.createAndSubmitRecruitmentRequest(manager(), engineering().getId(),
                softwareEngineer().getId(), 1, "x");
        assertThrows(AccessDeniedBusinessException.class,
                () -> workflow.decideRecruitmentRequest(recruiter(), requestId, true));
        assertThrows(AccessDeniedBusinessException.class,
                () -> workflow.submitInterviewFeedback(recruiter(), 1L, 5, "x", Recommendation.NO));
    }
}
