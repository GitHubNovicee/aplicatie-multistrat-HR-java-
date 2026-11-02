package org.hr.services;

import org.hr.HrTestSupport;
import org.hr.domain.common.CaseNumber;
import org.hr.domain.common.DateRange;
import org.hr.domain.development.*;
import org.hr.domain.leave.LeaveBalance;
import org.hr.domain.leave.LeaveRequest;
import org.hr.domain.organization.Employee;
import org.hr.domain.recruitment.Offer;
import org.hr.domain.recruitment.OfferStatus;
import org.hr.domain.training.*;
import org.hr.infrastructure.IDevelopmentPlanEntityFactory;
import org.hr.infrastructure.IEmployeeEntityFactory;
import org.hr.infrastructure.ILeaveEntityFactory;
import org.hr.infrastructure.ITrainingEntityFactory;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDate;
import java.time.Period;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/** COMPUTATION Business Service Tests (CR-01 .. CR-10). Strategy: Simple-Facade. */
public class Test1_ComputingDomainServices extends HrTestSupport {
    @Autowired private ILeaveComputingService leaveComputing;
    @Autowired private IEmployeeComputingService employeeComputing;
    @Autowired private ITrainingComputingService trainingComputing;
    @Autowired private IRecruitmentComputingService recruitmentComputing;
    @Autowired private IDevelopmentComputingService developmentComputing;
    @Autowired private ILeaveEntityFactory leaveFactory;
    @Autowired private ITrainingEntityFactory trainingFactory;
    @Autowired private IDevelopmentPlanEntityFactory planFactory;
    @Autowired private IEmployeeEntityFactory employeeFactory;
    @Autowired private IHolidayCalendar holidayCalendar;

    @Test
    void cr02_workingDaysExcludeWeekendsAndLegalHolidays() {
        // 6-17 aprilie 2026: 10 zile L-V, minus Vinerea Mare (10.04) si Lunea Pastelui (13.04) = 8
        assertEquals(8, leaveComputing.countWorkingDays(DateRange.of(LocalDate.of(2026, 4, 6), LocalDate.of(2026, 4, 17))));
        // doar weekend
        assertEquals(0, leaveComputing.countWorkingDays(DateRange.of(LocalDate.of(2026, 4, 4), LocalDate.of(2026, 4, 5))));
        // 11.03.2026 este miercuri: o singura zi lucratoare (capetele intervalului sunt incluse)
        assertEquals(1, leaveComputing.countWorkingDays(DateRange.of(LocalDate.of(2026, 3, 11), LocalDate.of(2026, 3, 11))));
        assertTrue(holidayCalendar.isHoliday(LocalDate.of(2026, 12, 1)));
        assertFalse(holidayCalendar.isHoliday(LocalDate.of(2026, 12, 2)));
    }

    @Test
    void cr02_computeWorkingDaysOfRequest() {
        Employee e = employee(idOf("engineer1"));
        LeaveRequest r = leaveFactory.buildLeaveRequest(e, org.hr.domain.leave.LeaveType.ANNUAL,
                LocalDate.of(2026, 4, 6), LocalDate.of(2026, 4, 17), "test");
        assertEquals(8, leaveComputing.computeWorkingDays(r).getWorkingDays());
    }

    @Test
    void cr01_cr03_balanceComputation() {
        Employee e = employee(idOf("engineer1"));
        LeaveBalance b = leaveFactory.buildLeaveBalance(e, org.hr.domain.leave.LeaveType.ANNUAL, 2030, 21);
        b.setUsedDays(5);
        assertEquals(16, leaveComputing.computeRemainingDays(b).getRemainingDays());       // CR-01

        LeaveRequest r = leaveFactory.buildLeaveRequest(e, org.hr.domain.leave.LeaveType.ANNUAL,
                LocalDate.of(2026, 4, 6), LocalDate.of(2026, 4, 8), "test");
        leaveComputing.computeWorkingDays(r);                                              // 3 zile
        leaveComputing.applyApproval(b, r);                                                // CR-03
        assertEquals(8, b.getUsedDays());
        assertEquals(13, b.getRemainingDays());
        leaveComputing.applyCancellation(b, r);
        assertEquals(5, b.getUsedDays());
        assertEquals(16, b.getRemainingDays());
        assertTrue(leaveComputing.isBalanceTracked(org.hr.domain.leave.LeaveType.ANNUAL));
        assertFalse(leaveComputing.isBalanceTracked(org.hr.domain.leave.LeaveType.SICK));
    }

    @Test
    void cr05_availableSeatsCountEnrolledAndAttendedOnly() {
        Course course = trainingFactory.buildCourse("CR05-X", "t", 8, Set.of());
        TrainingSession s = trainingFactory.buildTrainingSession(course, java.time.LocalDateTime.now().plusDays(3),
                java.time.LocalDateTime.now().plusDays(4), "room", "trainer", 5, null);
        Employee e = employee(idOf("engineer1"));
        for (EnrollmentStatus st : new EnrollmentStatus[]{EnrollmentStatus.ENROLLED, EnrollmentStatus.ATTENDED,
                EnrollmentStatus.ABSENT, EnrollmentStatus.CANCELLED}) {
            Enrollment en = s.addEnrollment(trainingFactory.buildEnrollment(e));
            en.setStatus(st);
        }
        assertEquals(3, trainingComputing.computeAvailableSeats(s));                      // 5 - (ENROLLED + ATTENDED)
        assertEquals(3, trainingComputing.computeAvailableSeatsOf(s).getAvailableSeats());
        assertEquals(2, trainingComputing.toView(s).getEnrolledCount());
    }

    @Test
    void cr08_seniorityUsesTerminationDateOrToday() {
        Employee e = employeeFactory.buildEmployee("A", "B", "sen@test.example", null,
                LocalDate.of(2020, 1, 15), engineering(), softwareEngineer(), null);
        e.setTerminationDate(LocalDate.of(2022, 3, 20));
        assertEquals(Period.of(2, 2, 5), employeeComputing.computeSeniority(e));
        e.setTerminationDate(null);
        e.setHireDate(LocalDate.now().minusYears(3));
        assertEquals(Period.ofYears(3), employeeComputing.computeSeniorityOf(e).getSeniority());
    }

    @Test
    void cr09_planProgress() {
        Employee e = employee(idOf("engineer1"));
        DevelopmentPlan plan = planFactory.buildDevelopmentPlan(e, "p", null, LocalDate.now(), LocalDate.now().plusMonths(6));
        assertEquals(0.0, developmentComputing.computeProgress(plan));                   // fara obiective
        ObjectiveStatus[] statuses = {ObjectiveStatus.ACHIEVED, ObjectiveStatus.ACHIEVED,
                ObjectiveStatus.CANCELLED, ObjectiveStatus.NOT_STARTED};
        for (ObjectiveStatus st : statuses) {
            Objective o = plan.addObjective(planFactory.buildObjective("o", null, null, LocalDate.now().plusDays(10)));
            o.setStatus(st);
        }
        assertEquals(66.67, developmentComputing.computeProgress(plan));                  // 2 / 3 neanulate
        assertEquals(3, developmentComputing.toView(plan).getActiveObjectives());
    }

    @Test
    void cr10_offerExpiry() {
        Offer offer = new Offer();
        offer.setValidUntil(LocalDate.now().minusDays(1));
        offer.setStatus(OfferStatus.SENT);
        assertTrue(recruitmentComputing.isOfferExpired(offer));
        offer.setValidUntil(LocalDate.now());
        assertFalse(recruitmentComputing.isOfferExpired(offer));                         // azi nu este expirata
        offer.setValidUntil(LocalDate.now().minusDays(1));
        offer.setStatus(OfferStatus.ACCEPTED);
        assertFalse(recruitmentComputing.isOfferExpired(offer));                         // doar SENT expira
    }

    @Test
    void cr04_caseNumberFormat() {
        assertEquals("ER-2026-0001", CaseNumber.of(2026, 1).toString());
        assertEquals(CaseNumber.of(2026, 42), CaseNumber.parse("ER-2026-0042"));
        assertThrows(IllegalArgumentException.class, () -> CaseNumber.parse("2026-1"));
    }
}
