package org.hr.infrastructure;

import org.hr.domain.organization.UserAccount;
import org.springframework.stereotype.Repository;

//SDI Bean Component
@Repository
public class UserAccountEntityRepositoryBase extends InMemoryEntityRepositoryBase<UserAccount> implements IUserAccountEntityRepository {
    public UserAccountEntityRepositoryBase() {
        super("UserAccount");
    }
}
