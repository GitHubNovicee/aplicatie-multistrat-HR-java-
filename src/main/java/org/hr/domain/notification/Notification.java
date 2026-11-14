package org.hr.domain.notification;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hr.domain.common.BaseEntity;
import org.hr.domain.organization.UserAccount;

/** RQ-11: notificare pentru termene, aprobari, interviuri, evaluari. */
@Entity
@Getter @Setter @NoArgsConstructor
public class Notification extends BaseEntity {
    @NotNull(message = "Recipient is required!")
    @ManyToOne(optional = false)
    private UserAccount recipient;
    @NotNull(message = "Notification type is required!")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private NotificationType type;
    @NotBlank(message = "Message is required!")
    @Column(nullable = false, length = 2000)
    private String message;
    /** ex. LEAVE_REQUEST */
    private String referenceType;
    private Long referenceId;
    @Column(name = "is_read", nullable = false)
    private boolean read = false;
}
