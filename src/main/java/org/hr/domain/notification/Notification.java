package org.hr.domain.notification;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hr.domain.common.BaseEntity;
import org.hr.domain.organization.UserAccount;

/** RQ-11: notificare pentru termene, aprobari, interviuri, evaluari. */
@Getter @Setter @NoArgsConstructor
public class Notification extends BaseEntity {
    @NotNull(message = "Recipient is required!")
    private UserAccount recipient;
    @NotNull(message = "Notification type is required!")
    private NotificationType type;
    @NotBlank(message = "Message is required!")
    private String message;
    /** ex. LEAVE_REQUEST */
    private String referenceType;
    private Long referenceId;
    private boolean read = false;
}
