package org.hr.services;

import org.hr.domain.notification.Notification;
import org.hr.domain.notification.NotificationType;
import org.hr.domain.organization.Employee;
import org.hr.domain.organization.Role;

import java.util.List;

/** UC-28 / RQ-11: notificari generate de sistem. */
public interface INotificationService {
    /** Creeaza notificarea pentru contul activ al angajatului; null daca angajatul nu are cont activ. */
    Notification notifyEmployee(Employee recipient, NotificationType type, String message,
                                String referenceType, Long referenceId);

    /** Notifica toate conturile active care au rolul dat. */
    List<Notification> notifyRole(Role role, NotificationType type, String message,
                                  String referenceType, Long referenceId);

    List<Notification> getNotificationsOf(Long employeeId);

    Notification markAsRead(Long notificationId);

    /**
     * RQ-11 (DEADLINE): notifica terminele care expira in urmatoarele {@code daysAhead} zile
     * (obiective ale planurilor ACTIVE si oferte SENT). Nu dubleaza notificarile deja create.
     * @return numarul notificarilor noi
     */
    int generateDeadlineNotifications(int daysAhead);
}
