package org.hr.services;

import org.hr.domain.organization.Employee;
import org.hr.domain.organization.Role;
import org.hr.domain.organization.UserAccount;

/** Verifica permisiunile pe roluri (matricea [PROPUS] din Sprint 1). */
public interface IAccessControlService {
    /** Contul activ al angajatului sau AccessDeniedBusinessException. */
    UserAccount requireActiveAccount(Long employeeId);

    boolean hasAnyRole(Long employeeId, Role... roles);

    /** @throws org.hr.domain.common.AccessDeniedBusinessException daca angajatul nu are niciunul dintre roluri */
    void requireAnyRole(Long employeeId, Role... roles);

    /** Actorul este managerul direct al angajatului si are rolul MANAGER. */
    boolean isManagerOf(Long actorId, Employee subject);

    /** Permis daca actorul este managerul direct al subiectului sau are oricare dintre rolurile date. */
    void requireManagerOfOrAnyRole(Long actorId, Employee subject, Role... roles);
}
