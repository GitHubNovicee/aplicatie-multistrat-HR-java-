package org.hr.domain.organization;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hr.domain.common.BaseEntity;

import java.util.HashSet;
import java.util.Set;

/** RQ-03: cont de utilizator per angajat, cu unul sau mai multe roluri si posibilitate de dezactivare. */
@Getter @Setter @NoArgsConstructor
public class UserAccount extends BaseEntity {
    @NotBlank(message = "Username is required!")
    @Size(min = 3, max = 50, message = "Username must have between 3 and 50 characters!")
    private String username;
    @NotBlank(message = "Password hash is required!")
    private String passwordHash;
    @NotNull(message = "Employee is required!")
    private Employee employee;
    @NotEmpty(message = "An account must have at least one role!")
    private Set<Role> roles = new HashSet<>();
    private boolean enabled = true;

    public boolean hasRole(Role role) {
        return roles.contains(role);
    }
}
