package org.hr.services;

import org.hr.domain.development.DevelopmentPlan;

/** Domain Validation Service - modulul Dezvoltare (RQ-09, VR-28, VR-29). */
public interface IDevelopmentPlanValidatingService {
    /**
     * Bean validation pe plan si obiectivele sale (agregat - VR-29: obiectivele exista numai in plan)
     * + VR-28 (angajat activ) + obiectivele nu au termen dupa sfarsitul planului.
     */
    void validatePlan(DevelopmentPlan plan);
}
