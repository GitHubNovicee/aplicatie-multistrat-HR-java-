package org.hr.services;

import org.hr.domain.organization.Employee;
import org.hr.domain.training.*;

/** Domain Validation Service - modulele Training si Competente (RQ-07, RQ-08, VR-23, VR-24, VR-28). */
public interface ITrainingValidatingService {
    /** Bean validation + codul cursului este unic. */
    void validateCourse(Course course);

    void validateCompetency(Competency competency);

    /** Bean validation + cursul este activ + sesiunea incepe in viitor. */
    void validateSession(TrainingSession session);

    /**
     * VR-23: un angajat se inscrie o singura data la o sesiune si numai daca sesiunea este PLANNED
     * si exista locuri; VR-28: angajatul nu trebuie sa fie arhivat.
     */
    void validateEnrollment(TrainingSession session, Employee employee);

    /** VR-24: un angajat are un singur nivel per competenta. */
    void validateEmployeeCompetency(EmployeeCompetency employeeCompetency);

    /** Bean validation + VR-28 + fara nevoie deschisa duplicat pentru aceeasi competenta. */
    void validateTrainingNeed(TrainingNeed need);
}
