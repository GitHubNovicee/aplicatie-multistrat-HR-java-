package org.hr.services;

import org.hr.domain.common.HrAuditView;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.Instant;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.logging.Logger;
import java.util.stream.Collectors;

@Service
public class HrAuditingServiceImpl implements IHrAuditingService {
    private static final Logger logger = Logger.getLogger(HrAuditingServiceImpl.class.getName());

    private final List<HrAuditView> trail = new CopyOnWriteArrayList<>();
    private final Clock clock;

    public HrAuditingServiceImpl(Clock clock) {
        this.clock = clock;
    }

    @Override
    public void audit(String entityType, Long entityId, HrAuditView.AuditOperation operation, String details) {
        HrAuditView view = new HrAuditView(entityType, entityId, operation, details, Instant.now(clock));
        trail.add(view);
        logger.info("AUDIT INFO: " + view);
    }

    @Override
    public List<HrAuditView> getAuditTrail() {
        return List.copyOf(trail);
    }

    @Override
    public List<HrAuditView> getAuditTrailOf(String entityType, Long entityId) {
        return trail.stream()
                .filter(a -> a.getEntityType().equals(entityType) && a.getEntityId().equals(entityId))
                .collect(Collectors.toList());
    }
}
