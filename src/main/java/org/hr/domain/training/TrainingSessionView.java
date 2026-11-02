package org.hr.domain.training;

import lombok.Value;

/** Domain Entity View: sinteza unei sesiuni de training (CR-05). */
@Value
public class TrainingSessionView {
    Long sessionId;
    String courseCode;
    String courseTitle;
    SessionStatus status;
    int capacity;
    int availableSeats;
    int enrolledCount;
}
