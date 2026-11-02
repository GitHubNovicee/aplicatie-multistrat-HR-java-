package org.hr.domain.recruitment;

import lombok.Value;

/** Domain Entity View: scorul mediu al unei candidaturi (CR-06). */
@Value
public class ApplicationScoreView {
    Long applicationId;
    String candidateName;
    String openingReference;
    ApplicationStatus status;
    /** null daca nu exista feedback in interviuri COMPLETED */
    Double averageScore;
    int completedInterviews;
}
