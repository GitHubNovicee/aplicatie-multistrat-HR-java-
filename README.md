# Aplicația multistrat HR – Sprint 2: Domain Implementation

Implementare Spring Boot (în stilul modelului `msd.t2_c4.springboot_domain`) a domeniului HR din Sprint 1.
Persistența este **in-memory** (repository-uri proprii), ca în model; JPA/Hibernate din Sprint 1 nu mai este folosit.

## Structura pachetelor (`org.hr`)
| Pachet | Conținut |
|---|---|
| `HrApplication` | punct de intrare Spring Boot |
| `config.AppScanConfig` | clasa de configurare Spring: beanuri `Clock` și `IHolidayCalendar` |
| `domain.common` | `BaseEntity`, Value Objects (`ContactInfo`, `DateRange`, `CaseNumber`), excepții, `HrAuditView` |
| `domain.organization / recruitment / leave / training / development / relations / notification` | entități, enum-uri, view-uri |
| `infrastructure` | `IEntityRepository` + repository-uri in-memory, **entity factories**, date demo (`HrSampleDataFactoryBase`) |
| `services` | servicii de domeniu: **computing**, **validating**, audit, notificări, control acces |
| `application` | **workflow services** (cazurile de utilizare UC-01…UC-28) |

## Agregate (rădăcină → entități componentă)
- `Employee`, `Department`, `JobPosition`, `UserAccount`, `RecruitmentRequest`, `JobOpening`, `Candidate`, `Application`, `Offer`, `LeaveBalance`, `LeaveRequest`, `Competency`, `EmployeeCompetency`, `TrainingNeed`, `Course`, `Notification` – rădăcini simple
- `Interview` → `InterviewFeedback` (VR-21)
- `TrainingSession` → `Enrollment` (VR-23, CR-05)
- `DevelopmentPlan` → `Objective` (VR-29)
- `EmployeeCase` → `CaseAction` (VR-29)

Componentele nu au referință înapoi la rădăcină și nu au repository propriu (se salvează prin rădăcină).

## Reguli → implementare
| Regulă | Unde |
|---|---|
| CR-01, CR-02, CR-03 | `LeaveComputingServiceImpl` (+ `RomanianLegalHolidayCalendar`) |
| CR-04 | `EmployeeCaseEntityFactoryBase.nextCaseNumber`, VO `CaseNumber` |
| CR-05 | `TrainingComputingServiceImpl` |
| CR-06, CR-07, CR-10 | `RecruitmentComputingServiceImpl` |
| CR-08 | `EmployeeComputingServiceImpl` |
| CR-09 | `DevelopmentComputingServiceImpl` |
| CR-11, RQ-12 | `InMemoryEntityRepositoryBase` (createdAt/updatedAt/version + respingere versiune veche) |
| VR-20, 21, 22, 27 | `RecruitmentValidatingServiceImpl` |
| VR-24, 25, 26 | `LeaveValidatingServiceImpl` (VR-24 și pentru competențe: `TrainingValidatingServiceImpl`) |
| VR-23 | `TrainingValidatingServiceImpl` |
| VR-28 | `EmployeeValidatingServiceImpl.validateEmployeeIsActive` + `EmployeeEntityRepositoryBase.remove` (fără ștergere fizică) |
| VR-29 | structura agregatelor + `DevelopmentPlanValidatingServiceImpl`, `EmployeeCaseValidatingServiceImpl` |
| VR-30 | `EmployeeCaseValidatingServiceImpl.validateResolution` |
| JavaBean Validation | adnotări pe toate entitățile și VO-urile (`@NotBlank`, `@Size`, `@Min/@Max`, `@Email`, `@Valid`, `@AssertTrue`…) |
| RQ-11 / UC-28 | `NotificationServiceImpl`, `RecruitmentWorkflowServiceImpl.expireOverdueOffers` |
| Audit | `HrAuditingServiceImpl` (apelat din toate workflow-urile) |
| Permisiuni pe roluri | `AccessControlServiceImpl` |

## Teste (`src/test/java`)
`Test1_ComputingDomainServices`, `Test2_ValidatingDomainServices`, `Test3_RecruitmentWorkflowService`,
`Test4_LeaveWorkflowService`, `Test5_TrainingWorkflowService`, `Test6_DevelopmentAndCaseWorkflowServices`,
`Test7_AuditNotificationsAndRepositories`. Rulare: `mvn test`.

## Decizii / presupuneri
- Parolele: câmpul `passwordHash` se primește gata hash-uit; la angajare se setează `{reset-required}` (hashing real – Sprint ulterior).
- Concediile cu sold (ANNUAL) nu pot traversa doi ani calendaristici (cererea se împarte); celelalte tipuri nu au sold.
- Candidaturile se acceptă doar pentru anunțuri `PUBLISHED`; interviurile se programează doar în viitor.
- Rezolvarea unui caz cere starea `IN_PROGRESS` (cel puțin o acțiune înregistrată).
- Seed demo: utilizatorii `admin`, `recruiter`, `trainer`, `emprel`, `manager`, `interviewer`, `engineer1..4`.
