package org.hr.infrastructure;

import org.hr.domain.common.CaseNumber;
import org.hr.domain.organization.Employee;
import org.hr.domain.relations.CaseAction;
import org.hr.domain.relations.CaseStatus;
import org.hr.domain.relations.CaseType;
import org.hr.domain.relations.EmployeeCase;
import org.springframework.stereotype.Component;

import java.time.Clock;
import java.time.Instant;

@Component
public class EmployeeCaseEntityFactoryBase implements IEmployeeCaseEntityFactory {
    private final IEmployeeCaseEntityRepository caseRepository;
    private final Clock clock;

    public EmployeeCaseEntityFactoryBase(IEmployeeCaseEntityRepository caseRepository, Clock clock) {
        this.caseRepository = caseRepository;
        this.clock = clock;
    }

    @Override
    public synchronized CaseNumber nextCaseNumber(int year) {
        int lastSequence = caseRepository.toCollection().stream()
                .map(EmployeeCase::getCaseNumber)
                .filter(n -> n.getYear() == year)
                .mapToInt(CaseNumber::getSequence)
                .max().orElse(0);
        return CaseNumber.of(year, lastSequence + 1);
    }

    @Override
    public EmployeeCase buildEmployeeCase(CaseType type, Employee submittedBy, String subject, String details) {
        EmployeeCase employeeCase = new EmployeeCase();
        employeeCase.setId(caseRepository.getNextID());
        employeeCase.setCaseNumber(nextCaseNumber(java.time.LocalDate.now(clock).getYear()));
        employeeCase.setType(type);
        employeeCase.setSubmittedBy(submittedBy);
        employeeCase.setSubject(subject);
        employeeCase.setDetails(details);
        employeeCase.setStatus(CaseStatus.OPEN);
        return employeeCase;
    }

    @Override
    public CaseAction buildCaseAction(Employee performedBy, String description) {
        CaseAction action = new CaseAction();
        action.setPerformedBy(performedBy);
        action.setDescription(description);
        action.setPerformedAt(Instant.now(clock));
        return action;
    }
}
