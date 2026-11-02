package org.hr.services;

import org.hr.domain.organization.Employee;
import org.hr.domain.organization.UserAccount;

import java.util.Set;

/** Domain Validation Service - modulul Organizatie / Conturi (RQ-01, RQ-03, VR-28). */
public interface IEmployeeValidatingService {
    /** Violari bean-validation + reguli de business; multime goala daca entitatea este valida. */
    Set<String> validate(Employee employee);

    void validateWithException(Employee employee);

    /** VR-28: un angajat arhivat nu poate depune cereri sau primi sesiuni noi. */
    void validateEmployeeIsActive(Employee employee);

    Set<String> validate(UserAccount account);

    void validateWithException(UserAccount account);
}
