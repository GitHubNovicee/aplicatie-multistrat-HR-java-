package org.hr.services;

import org.hr.domain.development.DevelopmentPlan;
import org.hr.domain.development.DevelopmentPlanView;
import org.hr.domain.development.Objective;
import org.hr.domain.development.ObjectiveStatus;
import org.springframework.stereotype.Service;

@Service
public class DevelopmentComputingServiceImpl implements IDevelopmentComputingService {

    private static long notCancelled(DevelopmentPlan plan) {
        return plan.getObjectives().stream().filter(o -> o.getStatus() != ObjectiveStatus.CANCELLED).count();
    }

    private static long achieved(DevelopmentPlan plan) {
        return plan.getObjectives().stream().filter(o -> o.getStatus() == ObjectiveStatus.ACHIEVED).count();
    }

    @Override
    public double computeProgress(DevelopmentPlan plan) {
        long total = notCancelled(plan);
        if (total == 0)
            return 0.0;
        return Math.round(achieved(plan) * 10000.0 / total) / 100.0;
    }

    @Override
    public DevelopmentPlan computeProgressOf(DevelopmentPlan plan) {
        plan.setProgress(computeProgress(plan));
        return plan;
    }

    @Override
    public DevelopmentPlanView toView(DevelopmentPlan plan) {
        computeProgressOf(plan);
        return new DevelopmentPlanView(
                plan.getId(), plan.getEmployee().getFullName(), plan.getTitle(), plan.getStatus(),
                plan.getObjectives().size(), (int) notCancelled(plan), (int) achieved(plan), plan.getProgress());
    }
}
