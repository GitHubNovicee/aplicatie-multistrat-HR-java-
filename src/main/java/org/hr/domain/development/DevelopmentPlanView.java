package org.hr.domain.development;

import lombok.Value;

/** Domain Entity View: progresul unui plan de dezvoltare (CR-09). */
@Value
public class DevelopmentPlanView {
    Long planId;
    String employeeName;
    String title;
    PlanStatus status;
    int totalObjectives;
    int activeObjectives;
    int achievedObjectives;
    /** procent 0..100 */
    double progress;
}
