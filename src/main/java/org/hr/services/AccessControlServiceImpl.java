package org.hr.services;

import org.hr.domain.common.AccessDeniedBusinessException;
import org.hr.domain.organization.Employee;
import org.hr.domain.organization.Role;
import org.hr.domain.organization.UserAccount;
import org.hr.infrastructure.IUserAccountEntityRepository;
import org.springframework.stereotype.Service;

import java.util.Arrays;

@Service
public class AccessControlServiceImpl implements IAccessControlService {
    private final IUserAccountEntityRepository accountRepository;

    public AccessControlServiceImpl(IUserAccountEntityRepository accountRepository) {
        this.accountRepository = accountRepository;
    }

    @Override
    public UserAccount requireActiveAccount(Long employeeId) {
        return accountRepository
                .findFirst(a -> a.getEmployee().getId().equals(employeeId) && a.isEnabled())
                .orElseThrow(() -> new AccessDeniedBusinessException(
                        "Employee " + employeeId + " has no active user account!"));
    }

    @Override
    public boolean hasAnyRole(Long employeeId, Role... roles) {
        return accountRepository
                .findFirst(a -> a.getEmployee().getId().equals(employeeId) && a.isEnabled()
                        && Arrays.stream(roles).anyMatch(a::hasRole))
                .isPresent();
    }

    @Override
    public void requireAnyRole(Long employeeId, Role... roles) {
        requireActiveAccount(employeeId);
        if (!hasAnyRole(employeeId, roles))
            throw new AccessDeniedBusinessException(
                    "Employee " + employeeId + " needs one of the roles " + Arrays.toString(roles) + "!");
    }

    @Override
    public boolean isManagerOf(Long actorId, Employee subject) {
        return subject.getManager() != null
                && subject.getManager().getId().equals(actorId)
                && hasAnyRole(actorId, Role.MANAGER);
    }

    @Override
    public void requireManagerOfOrAnyRole(Long actorId, Employee subject, Role... roles) {
        if (isManagerOf(actorId, subject))
            return;
        requireAnyRole(actorId, roles);
    }
}
