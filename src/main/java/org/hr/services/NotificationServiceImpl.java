package org.hr.services;

import org.hr.domain.development.ObjectiveStatus;
import org.hr.domain.development.PlanStatus;
import org.hr.domain.notification.Notification;
import org.hr.domain.notification.NotificationType;
import org.hr.domain.organization.Employee;
import org.hr.domain.organization.Role;
import org.hr.domain.organization.UserAccount;
import org.hr.domain.recruitment.OfferStatus;
import org.hr.infrastructure.*;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
public class NotificationServiceImpl implements INotificationService {
    private final INotificationEntityRepository notificationRepository;
    private final IUserAccountEntityRepository accountRepository;
    private final IDevelopmentPlanEntityRepository planRepository;
    private final IOfferEntityRepository offerRepository;
    private final Clock clock;

    public NotificationServiceImpl(INotificationEntityRepository notificationRepository,
                                   IUserAccountEntityRepository accountRepository,
                                   IDevelopmentPlanEntityRepository planRepository,
                                   IOfferEntityRepository offerRepository,
                                   Clock clock) {
        this.notificationRepository = notificationRepository;
        this.accountRepository = accountRepository;
        this.planRepository = planRepository;
        this.offerRepository = offerRepository;
        this.clock = clock;
    }

    private Notification create(UserAccount recipient, NotificationType type, String message,
                                String referenceType, Long referenceId) {
        Notification notification = new Notification();
        notification.setId(notificationRepository.getNextID());
        notification.setRecipient(recipient);
        notification.setType(type);
        notification.setMessage(message);
        notification.setReferenceType(referenceType);
        notification.setReferenceId(referenceId);
        return notificationRepository.add(notification);
    }

    @Override
    public Notification notifyEmployee(Employee recipient, NotificationType type, String message,
                                       String referenceType, Long referenceId) {
        if (recipient == null)
            return null;
        return accountRepository
                .findFirst(a -> a.getEmployee().equals(recipient) && a.isEnabled())
                .map(account -> create(account, type, message, referenceType, referenceId))
                .orElse(null);
    }

    @Override
    public List<Notification> notifyRole(Role role, NotificationType type, String message,
                                         String referenceType, Long referenceId) {
        List<Notification> created = new ArrayList<>();
        for (UserAccount account : accountRepository.find(a -> a.isEnabled() && a.hasRole(role)))
            created.add(create(account, type, message, referenceType, referenceId));
        return created;
    }

    @Override
    public List<Notification> getNotificationsOf(Long employeeId) {
        return notificationRepository.find(n -> n.getRecipient().getEmployee().getId().equals(employeeId));
    }

    @Override
    public Notification markAsRead(Long notificationId) {
        Notification notification = notificationRepository.requireById(notificationId);
        notification.setRead(true);
        return notificationRepository.add(notification);
    }

    private boolean alreadyNotified(UserAccount account, String referenceType, Long referenceId) {
        return notificationRepository.findFirst(n -> n.getRecipient().equals(account)
                && n.getType() == NotificationType.DEADLINE
                && referenceType.equals(n.getReferenceType())
                && referenceId.equals(n.getReferenceId())).isPresent();
    }

    @Override
    public int generateDeadlineNotifications(int daysAhead) {
        LocalDate today = LocalDate.now(clock);
        LocalDate limit = today.plusDays(daysAhead);
        int created = 0;

        // obiective ale planurilor ACTIVE, nefinalizate, cu termenul in fereastra
        for (var plan : planRepository.find(p -> p.getStatus() == PlanStatus.ACTIVE)) {
            for (var objective : plan.getObjectives()) {
                boolean open = objective.getStatus() == ObjectiveStatus.NOT_STARTED
                        || objective.getStatus() == ObjectiveStatus.IN_PROGRESS;
                boolean inWindow = !objective.getDueDate().isBefore(today) && !objective.getDueDate().isAfter(limit);
                if (!open || !inWindow)
                    continue;
                UserAccount account = accountRepository
                        .findFirst(a -> a.getEmployee().equals(plan.getEmployee()) && a.isEnabled()).orElse(null);
                // obiectivele sunt entitati componenta (id local planului): tipul referintei include id-ul planului
                String referenceType = "OBJECTIVE_OF_PLAN_" + plan.getId();
                if (account == null || alreadyNotified(account, referenceType, objective.getId()))
                    continue;
                create(account, NotificationType.DEADLINE,
                        "Objective '" + objective.getTitle() + "' is due on " + objective.getDueDate(),
                        referenceType, objective.getId());
                created++;
            }
        }

        // oferte SENT care expira in fereastra -> recrutori
        for (var offer : offerRepository.find(o -> o.getStatus() == OfferStatus.SENT
                && !o.getValidUntil().isBefore(today) && !o.getValidUntil().isAfter(limit))) {
            for (UserAccount account : accountRepository.find(a -> a.isEnabled() && a.hasRole(Role.HR_RECRUITER))) {
                if (alreadyNotified(account, "OFFER", offer.getId()))
                    continue;
                create(account, NotificationType.DEADLINE,
                        "Offer #" + offer.getId() + " is valid until " + offer.getValidUntil(),
                        "OFFER", offer.getId());
                created++;
            }
        }
        return created;
    }
}
