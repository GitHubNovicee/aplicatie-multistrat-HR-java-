package org.hr.services;

import org.hr.domain.recruitment.*;

import java.util.Set;

/** Domain Validation Service - modulul Recrutare (RQ-04, RQ-05, VR-20, VR-21, VR-22, VR-27, VR-28). */
public interface IRecruitmentValidatingService {
    /** Bean validation + VR-28 (solicitantul trebuie sa fie activ). */
    void validateRequest(RecruitmentRequest request);

    /** VR-27: anunt PUBLISHED numai pentru cerere APPROVED (+ bean validation). */
    void validateOpeningForPublication(JobOpening opening);

    void validateCandidate(Candidate candidate);

    /** Violari pentru candidatura (VR-20 + bean validation); multime goala daca este valida. */
    Set<String> validate(Application application);

    /** VR-20: un candidat poate aplica o singura data la un anunt (+ anuntul trebuie sa fie PUBLISHED). */
    void validateApplication(Application application);

    void validateInterview(Interview interview);

    /** VR-21: un intervievator da un singur feedback per interviu si trebuie sa fie in lista intervievatorilor. */
    void validateFeedback(Interview interview, InterviewFeedback feedback);

    /** VR-22: o candidatura are cel mult o oferta (+ bean validation). */
    void validateOffer(Offer offer);

    /** VR-27: numarul de angajari HIRED nu poate depasi headcount. */
    void validateHire(Application application);
}
