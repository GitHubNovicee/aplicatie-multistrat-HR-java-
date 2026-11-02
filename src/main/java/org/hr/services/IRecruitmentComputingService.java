package org.hr.services;

import org.hr.domain.recruitment.Application;
import org.hr.domain.recruitment.ApplicationScoreView;
import org.hr.domain.recruitment.Offer;
import org.hr.domain.recruitment.RecruitmentRequest;

/** Domain Computation Service (CR-06, CR-07, CR-10) - modulul Recrutare. */
public interface IRecruitmentComputingService {
    /** CR-06: media score din feedback-urile interviurilor COMPLETED; null daca nu exista feedback. */
    Double computeAverageScore(Application application);

    Application computeAverageScoreOf(Application application);

    /** CR-07: posturi ramase = headcount - nr. candidaturi HIRED pentru cererea respectiva. */
    int computeRemainingPositions(RecruitmentRequest request);

    RecruitmentRequest computeRemainingPositionsOf(RecruitmentRequest request);

    /** CR-10: oferta expirata = validUntil &lt; data curenta si status SENT. */
    boolean isOfferExpired(Offer offer);

    ApplicationScoreView toScoreView(Application application);
}
