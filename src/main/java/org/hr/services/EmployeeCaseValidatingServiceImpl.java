package org.hr.services;

import jakarta.validation.Validator;
import org.hr.domain.organization.Employee;
import org.hr.domain.relations.CaseStatus;
import org.hr.domain.relations.EmployeeCase;
import org.hr.infrastructure.IEmployeeCaseEntityRepository;
import org.springframework.stereotype.Service;

import java.util.Set;
import java.util.TreeSet;

@Service
public class EmployeeCaseValidatingServiceImpl extends ValidatingServiceSupport implements IEmployeeCaseValidatingService {
    private final IEmployeeCaseEntityRepository caseRepository;
    private final IEmployeeValidatingService employeeValidatingService;

    public EmployeeCaseValidatingServiceImpl(Validator validator,
                                             IEmployeeCaseEntityRepository caseRepository,
                                             IEmployeeValidatingService employeeValidatingService) {
        super(validator);
        this.caseRepository = caseRepository;
        this.employeeValidatingService = employeeValidatingService;
    }

    @Override
    public void validateCaseSubmission(EmployeeCase employeeCase) {
        Set<String> violations = new TreeSet<>(beanViolations(employeeCase));
        if (employeeCase.getCaseNumber() != null && caseRepository.findFirst(c ->
                c.getCaseNumber().equals(employeeCase.getCaseNumber()) && !c.equals(employeeCase)).isPresent())
            violations.add("caseNumber: Case number " + employeeCase.getCaseNumber() + " is already used!");
        throwIfAny(violations);
        employeeValidatingService.validateEmployeeIsActive(employeeCase.getSubmittedBy());   // VR-28
    }

    @Override
    public void validateCase(EmployeeCase employeeCase) {
        throwIfAny(beanViolations(employeeCase));
    }

    @Override
    public void validateResolution(EmployeeCase employeeCase, Employee resolver, String resolution) {
        Set<String> violations = new TreeSet<>();
        if (employeeCase.getAssignedTo() == null || !employeeCase.getAssignedTo().equals(resolver))
            violations.add("VR-30: The case can be resolved only by the assigned HR responsible!");
        if (resolution == null || resolution.isBlank())
            violations.add("VR-30: A resolution is mandatory when resolving a case!");
        if (employeeCase.getStatus() != CaseStatus.IN_PROGRESS)
            violations.add("status: Only IN_PROGRESS cases can be resolved!");
        throwIfAny(violations);
    }
}
