package org.hr.application;

import org.hr.domain.common.BusinessValidationException;
import org.hr.domain.common.EntityNotFoundException;
import org.hr.domain.common.HrAuditView.AuditOperation;
import org.hr.domain.organization.Employee;
import org.hr.domain.organization.Role;
import org.hr.domain.training.*;
import org.hr.infrastructure.*;
import org.hr.services.*;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Service
public class TrainingWorkflowServiceImpl implements ITrainingWorkflowService {
    private final IEmployeeEntityRepository employeeRepository;
    private final ICompetencyEntityRepository competencyRepository;
    private final ICourseEntityRepository courseRepository;
    private final ITrainingSessionEntityRepository sessionRepository;
    private final ITrainingNeedEntityRepository needRepository;
    private final ITrainingEntityFactory factory;
    private final ITrainingValidatingService validator;
    private final ITrainingComputingService computingService;
    private final IAccessControlService accessControl;
    private final IHrAuditingService auditingService;
    private final Clock clock;

    public TrainingWorkflowServiceImpl(IEmployeeEntityRepository employeeRepository,
                                       ICompetencyEntityRepository competencyRepository,
                                       ICourseEntityRepository courseRepository,
                                       ITrainingSessionEntityRepository sessionRepository,
                                       ITrainingNeedEntityRepository needRepository,
                                       ITrainingEntityFactory factory, ITrainingValidatingService validator,
                                       ITrainingComputingService computingService, IAccessControlService accessControl,
                                       IHrAuditingService auditingService, Clock clock) {
        this.employeeRepository = employeeRepository;
        this.competencyRepository = competencyRepository;
        this.courseRepository = courseRepository;
        this.sessionRepository = sessionRepository;
        this.needRepository = needRepository;
        this.factory = factory;
        this.validator = validator;
        this.computingService = computingService;
        this.accessControl = accessControl;
        this.auditingService = auditingService;
        this.clock = clock;
    }

    private void requireCoordinator(Long id) {
        accessControl.requireAnyRole(id, Role.HR_TRAINING_COORDINATOR, Role.HR_ADMIN);
    }

    @Override
    public Long createCourse(Long coordinatorId, String code, String title, int durationHours, Set<Long> competencyIds) {
        requireCoordinator(coordinatorId);
        Set<Competency> competencies = new HashSet<>();
        for (Long id : competencyIds)
            competencies.add(competencyRepository.requireById(id));
        Course course = factory.buildCourse(code, title, durationHours, competencies);
        validator.validateCourse(course);
        courseRepository.add(course);
        auditingService.audit("Course", course.getId(), AuditOperation.CREATED, code);
        return course.getId();
    }

    @Override
    public void setCourseActive(Long coordinatorId, Long courseId, boolean active) {
        requireCoordinator(coordinatorId);
        Course course = courseRepository.requireById(courseId);
        course.setActive(active);
        courseRepository.add(course);
        auditingService.audit("Course", courseId, AuditOperation.UPDATED, "active=" + active);
    }

    @Override
    public Long planSession(Long coordinatorId, Long courseId, LocalDateTime startsAt, LocalDateTime endsAt,
                            String location, String trainer, int capacity) {
        requireCoordinator(coordinatorId);
        TrainingSession session = factory.buildTrainingSession(courseRepository.requireById(courseId), startsAt, endsAt,
                location, trainer, capacity, employeeRepository.requireById(coordinatorId));
        validator.validateSession(session);
        sessionRepository.add(session);
        auditingService.audit("TrainingSession", session.getId(), AuditOperation.CREATED, "capacity " + capacity);
        return session.getId();
    }

    @Override
    public Long enrollEmployee(Long actorId, Long sessionId, Long employeeId) {
        if (!actorId.equals(employeeId))
            requireCoordinator(actorId);
        TrainingSession session = sessionRepository.requireById(sessionId);
        Employee employee = employeeRepository.requireById(employeeId);
        validator.validateEnrollment(session, employee);                  // VR-23, VR-28, CR-05
        Enrollment enrollment = session.addEnrollment(factory.buildEnrollment(employee));
        computingService.computeAvailableSeatsOf(session);
        sessionRepository.add(session);
        needRepository.find(n -> n.getEmployee().equals(employee) && n.getStatus() == TrainingNeedStatus.IDENTIFIED
                && n.getSuggestedCourse() != null && n.getSuggestedCourse().equals(session.getCourse()))
                .forEach(n -> {
                    n.setStatus(TrainingNeedStatus.PLANNED);
                    needRepository.add(n);
                });
        auditingService.audit("TrainingSession", sessionId, AuditOperation.UPDATED, "enrolled " + employee.getEmployeeCode());
        return enrollment.getId();
    }

    private Enrollment enrollmentOf(TrainingSession session, Employee employee) {
        return session.findEnrollmentOf(employee)
                .orElseThrow(() -> new EntityNotFoundException("Enrollment", employee.getEmployeeCode()));
    }

    @Override
    public void cancelEnrollment(Long actorId, Long sessionId, Long employeeId) {
        if (!actorId.equals(employeeId))
            requireCoordinator(actorId);
        TrainingSession session = sessionRepository.requireById(sessionId);
        Enrollment enrollment = enrollmentOf(session, employeeRepository.requireById(employeeId));
        if (enrollment.getStatus() != EnrollmentStatus.ENROLLED)
            throw new BusinessValidationException("Only ENROLLED enrollments can be cancelled!");
        enrollment.setStatus(EnrollmentStatus.CANCELLED);
        computingService.computeAvailableSeatsOf(session);                // seat freed (CR-05)
        sessionRepository.add(session);
        auditingService.audit("TrainingSession", sessionId, AuditOperation.UPDATED, "enrollment cancelled");
    }

    private TrainingSession sessionIn(Long sessionId, SessionStatus... allowed) {
        TrainingSession session = sessionRepository.requireById(sessionId);
        for (SessionStatus s : allowed)
            if (session.getStatus() == s)
                return session;
        throw new BusinessValidationException("Operation not allowed for a " + session.getStatus() + " session!");
    }

    @Override
    public SessionStatus startSession(Long coordinatorId, Long sessionId) {
        requireCoordinator(coordinatorId);
        TrainingSession session = sessionIn(sessionId, SessionStatus.PLANNED);
        session.setStatus(SessionStatus.ONGOING);
        sessionRepository.add(session);
        auditingService.audit("TrainingSession", sessionId, AuditOperation.UPDATED, "ONGOING");
        return session.getStatus();
    }

    @Override
    public void recordResult(Long coordinatorId, Long sessionId, Long employeeId, boolean attended,
                             BigDecimal score, boolean certified) {
        requireCoordinator(coordinatorId);
        TrainingSession session = sessionIn(sessionId, SessionStatus.ONGOING, SessionStatus.COMPLETED);
        Employee employee = employeeRepository.requireById(employeeId);
        Enrollment enrollment = enrollmentOf(session, employee);
        if (enrollment.getStatus() != EnrollmentStatus.ENROLLED)
            throw new BusinessValidationException("A result can be recorded only for ENROLLED participants!");
        if (certified && !attended)
            throw new BusinessValidationException("Only participants who attended can be certified!");
        enrollment.setStatus(attended ? EnrollmentStatus.ATTENDED : EnrollmentStatus.ABSENT);
        enrollment.setResultScore(attended ? score : null);
        if (certified) {
            enrollment.setCertified(true);
            enrollment.setCertificateNumber(String.format("CERT-%s-%d-%d",
                    session.getCourse().getCode(), session.getId(), employee.getId()));
            enrollment.setCertifiedOn(LocalDate.now(clock));
        }
        validator.validateSession(session);                               // bean validation pe scor
        computingService.computeAvailableSeatsOf(session);
        sessionRepository.add(session);
        if (certified)
            needRepository.find(n -> n.getEmployee().equals(employee)
                    && session.getCourse().getTargetCompetencies().contains(n.getCompetency())
                    && (n.getStatus() == TrainingNeedStatus.PLANNED || n.getStatus() == TrainingNeedStatus.IDENTIFIED))
                    .forEach(n -> {
                        n.setStatus(TrainingNeedStatus.FULFILLED);
                        needRepository.add(n);
                    });
        auditingService.audit("TrainingSession", sessionId, AuditOperation.UPDATED, "result for " + employee.getEmployeeCode());
    }

    @Override
    public SessionStatus completeSession(Long coordinatorId, Long sessionId) {
        requireCoordinator(coordinatorId);
        TrainingSession session = sessionIn(sessionId, SessionStatus.PLANNED, SessionStatus.ONGOING);
        session.setStatus(SessionStatus.COMPLETED);
        sessionRepository.add(session);
        auditingService.audit("TrainingSession", sessionId, AuditOperation.COMPLETED, "");
        return session.getStatus();
    }

    @Override
    public SessionStatus cancelSession(Long coordinatorId, Long sessionId) {
        requireCoordinator(coordinatorId);
        TrainingSession session = sessionIn(sessionId, SessionStatus.PLANNED);
        session.setStatus(SessionStatus.CANCELLED);
        session.getEnrollments().stream().filter(e -> e.getStatus() == EnrollmentStatus.ENROLLED)
                .forEach(e -> e.setStatus(EnrollmentStatus.CANCELLED));
        computingService.computeAvailableSeatsOf(session);
        sessionRepository.add(session);
        auditingService.audit("TrainingSession", sessionId, AuditOperation.CANCELLED, "");
        return session.getStatus();
    }

    @Override
    public TrainingSessionView getSessionSummary(Long sessionId) {
        return computingService.toView(sessionRepository.requireById(sessionId));
    }
}
