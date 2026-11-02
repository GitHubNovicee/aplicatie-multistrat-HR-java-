package org.hr.domain.common;

import lombok.Value;

import java.time.Instant;

/** Domain Entity View pentru auditul fluxurilor de business (analog ProjectAuditView). */
@Value
public class HrAuditView {
    String entityType;
    Long entityId;
    AuditOperation operation;
    String details;
    Instant at;

    public enum AuditOperation {
        CREATED, UPDATED, SUBMITTED, APPROVED, REJECTED, CANCELLED, PUBLISHED,
        SCHEDULED, COMPLETED, HIRED, ARCHIVED, ASSIGNED, RESOLVED, CLOSED, EXPIRED
    }
}
