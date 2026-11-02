package org.hr.infrastructure;

import org.hr.domain.notification.Notification;
import org.springframework.stereotype.Repository;

//SDI Bean Component
@Repository
public class NotificationEntityRepositoryBase extends InMemoryEntityRepositoryBase<Notification> implements INotificationEntityRepository {
    public NotificationEntityRepositoryBase() {
        super("Notification");
    }
}
