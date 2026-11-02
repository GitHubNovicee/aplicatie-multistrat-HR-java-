package org.hr.application;

import org.hr.domain.recruitment.ApplicationScoreView;
import org.hr.domain.recruitment.ApplicationStatus;
import org.hr.domain.recruitment.Recommendation;
import org.hr.domain.recruitment.RequestStatus;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Set;

/*
 * Business Workflow (RQ-04, RQ-05): cerere de recrutare -> aprobare -> anunt -> candidaturi
 * -> interviuri -> oferta -> angajare. Cazuri de utilizare UC-04 .. UC-12.
 */
public interface IRecruitmentWorkflowService {
    /** UC-04 (MANAGER): cerere SUBMITTED; notifica HR_ADMIN. */
    Long createAndSubmitRecruitmentRequest(Long managerId, Long departmentId, Long jobPositionId,
                                           int headcount, String justification);

    /** UC-05 (HR_ADMIN): APPROVED / REJECTED. */
    RequestStatus decideRecruitmentRequest(Long adminId, Long requestId, boolean approve);

    /** UC-06 (HR_RECRUITER): anunt PUBLISHED (VR-27). */
    Long publishJobOpening(Long recruiterId, Long requestId, String title, String description, LocalDate closingDate);

    /** UC-07: candidat (creat daca nu exista) + candidatura RECEIVED (VR-20). */
    Long registerApplication(Long recruiterId, Long openingId, String firstName, String lastName,
                             String email, String phone, String cvPath);

    /** UC-08: SCREENING sau REJECTED. */
    ApplicationStatus screenApplication(Long recruiterId, Long applicationId, boolean passed);

    /** UC-09: interviu SCHEDULED + notificare INTERVIEW pentru intervievatori. */
    Long scheduleInterview(Long recruiterId, Long applicationId, LocalDateTime scheduledAt,
                           String location, Set<Long> interviewerIds);

    /** UC-10 (INTERVIEWER): feedback scor 1-10 + recomandare (VR-21). */
    Long submitInterviewFeedback(Long interviewerId, Long interviewId, int score, String comments,
                                 Recommendation recommendation);

    /** Interviu COMPLETED; recalculeaza scorul mediu al candidaturii (CR-06). */
    void completeInterview(Long recruiterId, Long interviewId);

    /** UC-11: oferta SENT, candidatura OFFER (VR-22). */
    Long issueOffer(Long recruiterId, Long applicationId, BigDecimal proposedSalary,
                    LocalDate startDate, LocalDate validUntil);

    /** UC-12: acceptata -> HIRED, angajat + cont create (VR-27), returneaza id-ul angajatului; refuzata -> null. */
    Long registerOfferResponse(Long recruiterId, Long offerId, boolean accepted);

    /** CR-10 / UC-28: marcheaza EXPIRED ofertele SENT cu validUntil depasit. */
    int expireOverdueOffers();

    ApplicationScoreView getApplicationScore(Long applicationId);
}
