package org.hr.infrastructure;

import org.hr.domain.notification.Notification;
import org.hr.domain.organization.UserAccount;

import java.util.List;

public interface INotificationEntityRepository extends IEntityRepository<Notification> {
    List<Notification> findByRecipient(UserAccount recipient);

    List<Notification> findByRecipientAndReadFalse(UserAccount recipient);

    long countByRecipientAndReadFalse(UserAccount recipient);
}
