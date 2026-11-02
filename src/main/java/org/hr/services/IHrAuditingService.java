package org.hr.services;

import org.hr.domain.common.HrAuditView;

import java.util.List;

/** Audit Service (RQ-12): jurnal al operatiilor de business executate de workflow services. */
public interface IHrAuditingService {
    void audit(String entityType, Long entityId, HrAuditView.AuditOperation operation, String details);

    List<HrAuditView> getAuditTrail();

    List<HrAuditView> getAuditTrailOf(String entityType, Long entityId);
}
