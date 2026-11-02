package org.hr.services;

import org.hr.domain.development.DevelopmentPlan;
import org.hr.domain.development.DevelopmentPlanView;

/** Domain Computation Service (CR-09) - modulul Dezvoltare. */
public interface IDevelopmentComputingService {
    /** CR-09: progres plan (procent) = obiective ACHIEVED / obiective neanulate. */
    double computeProgress(DevelopmentPlan plan);

    DevelopmentPlan computeProgressOf(DevelopmentPlan plan);

    DevelopmentPlanView toView(DevelopmentPlan plan);
}
