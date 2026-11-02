package org.hr.infrastructure;

import org.hr.domain.recruitment.Application;
import org.springframework.stereotype.Repository;

//SDI Bean Component
@Repository
public class ApplicationEntityRepositoryBase extends InMemoryEntityRepositoryBase<Application> implements IApplicationEntityRepository {
    public ApplicationEntityRepositoryBase() {
        super("Application");
    }
}
