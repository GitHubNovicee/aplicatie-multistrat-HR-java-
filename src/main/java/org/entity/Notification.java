package org.entity;

import jakarta.persistence.*;
import lombok.*;
import org.enums.*;
import java.math.BigDecimal;
import java.time.*;
import java.util.*;

@Getter @Setter @NoArgsConstructor
@Entity
public class Notification extends BaseEntity {
    @ManyToOne(fetch = FetchType.LAZY, optional = false) private UserAccount recipient;
    @Enumerated(EnumType.STRING) @Column(nullable = false) private NotificationType type;
    @Column(nullable = false) private String message;
    private String referenceType; // e.g. LEAVE_REQUEST
    private Long referenceId;
    @Column(name = "is_read") private boolean read = false;
}
