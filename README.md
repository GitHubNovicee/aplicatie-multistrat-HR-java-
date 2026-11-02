# Sistem Integrat de Management al Resurselor Umane (HR Multistrat)

Acest proiect reprezintă o aplicație enterprise dezvoltată în Java (utilizând Maven și Spring) și structurată pe baza unei **Arhitecturi Multistrat (Layered Architecture)** și a principiilor **Domain-Driven Design (DDD)**. Sistemul acoperă întregul ciclu de viață al resurselor umane dintr-o organizație: de la recrutare și structură organizațională, până la gestiunea concediilor, formare profesională, evaluarea performanței și relațiile de muncă[cite: 1].

## Arhitectura Sistemului

Aplicația este organizată în pachetul de bază `org.hr` și separă strict responsabilitățile pe patru straturi principale[cite: 1]:

1. **Stratul de Domeniu (`org.hr.domain`)**: Conține entitățile de business, obiectele de tip valoare (Value Objects), enumerările de stare, excepțiile specifice domeniului și proiecțiile (Views)[cite: 1].
2. **Stratul de Servicii de Domeniu (`org.hr.services`)**: Încapsulează logica pură de business, împărțită riguros în servicii de calcul (`*ComputingService`), servicii de validare (`*ValidatingService`) și servicii transversale (audit, notificări, controlul accesului, calendarul sărbătorilor legale din România)[cite: 1].
3. **Stratul de Aplicație (`org.hr.application`)**: Orchestrează fluxurile de lucru complexe (Workflows) prin coordonarea repozitoriilor, fabricilor de entități și serviciilor de domeniu[cite: 1].
4. **Stratul de Infrastructură (`org.hr.infrastructure`)**: Oferă suportul tehnic pentru persistența datelor (implementări de tip `InMemoryEntityRepositoryBase`), crearea instanțelor prin șablonul Factory (`*EntityFactoryBase`) și generarea datelor de test (`HrSampleDataFactoryBase`)[cite: 1].

## Module Funcționale (Sub-domenii)

### 1. Organizație și Structură (`domain.organization`)
Gestionează structura internă a companiei și identitatea utilizatorilor[cite: 1]:
* **Entități și Modele**: `Department`, `JobPosition`, `Employee`, `UserAccount`, `Role` și proiecția `EmployeeProfileView`[cite: 1].
* **Fluxuri și Operațiuni**: Administrarea ierarhiei organizaționale, validarea datelor angajaților și calculul indicatorilor specifici prin `OrganizationWorkflowServiceImpl`, `EmployeeComputingServiceImpl` și `EmployeeValidatingServiceImpl`[cite: 1].

### 2. Recrutare și Selecție (`domain.recruitment`)
Acoperă întregul proces de atragere și angajare a candidaților[cite: 1]:
* **Entități și Modele**: `RecruitmentRequest`, `JobOpening`, `Candidate`, `Application`, `Interview`, `InterviewFeedback`, `Offer` și proiecția `ApplicationScoreView`[cite: 1].
* **Stări și Recomandări**: `RequestStatus`, `OpeningStatus`, `ApplicationStatus`, `InterviewStatus`, `OfferStatus`, `Recommendation`[cite: 1].
* **Fluxuri și Operațiuni**: De la deschiderea unei cereri de recrutare până la programarea interviurilor, calculul scorurilor de aplicare și emiterea ofertei finale prin `RecruitmentWorkflowServiceImpl`[cite: 1].

### 3. Gestiunea Concediilor (`domain.leave`)
Administrează absențele și soldurile de zile libere ale angajaților[cite: 1]:
* **Entități și Modele**: `LeaveRequest`, `LeaveBalance`, `LeaveType`, `LeaveStatus` și proiecția `LeaveBalanceView`[cite: 1].
* **Specific Național**: Include clasa `RomanianLegalHolidayCalendar` (`IHolidayCalendar`), care exclude automat zilele de sărbătoare legală din România și weekendurile la calcularea zilelor lucrătoare de concediu[cite: 1].
* **Fluxuri și Operațiuni**: Înregistrarea cererilor, verificarea suprapunerilor sau a soldului disponibil (`LeaveValidatingServiceImpl`) și actualizarea balanței (`LeaveComputingServiceImpl`, `LeaveWorkflowServiceImpl`)[cite: 1].

### 4. Formare Profesională și Competențe (`domain.training`)
Urmărește dezvoltarea abilităților tehnice și profesionale[cite: 1]:
* **Entități și Modele**: `Competency`, `EmployeeCompetency`, `ProficiencyLevel`, `TrainingNeed`, `Course`, `TrainingSession`, `Enrollment` și `TrainingSessionView`[cite: 1].
* **Fluxuri și Operațiuni**: Identificarea nevoilor de training, organizarea sesiunilor de curs, înscrierea angajaților și actualizarea nivelului de competență prin `TrainingWorkflowServiceImpl`[cite: 1].

### 5. Dezvoltare și Performanță (`domain.development`)
Permite stabilirea și urmărirea obiectivelor individuale pe termen mediu și lung[cite: 1]:
* **Entități și Modele**: `DevelopmentPlan`, `Objective`, `PlanStatus`, `ObjectiveStatus` și `DevelopmentPlanView`[cite: 1].
* **Fluxuri și Operațiuni**: Crearea planurilor de dezvoltare, evaluarea progresului pe obiective și validarea stărilor prin `DevelopmentWorkflowServiceImpl`[cite: 1].

### 6. Relații de Muncă și Cazuistică HR (`domain.relations`)
Gestionează sesizările, investigațiile interne și cazurile disciplinare sau administrative[cite: 1]:
* **Entități și Modele**: `EmployeeCase`, `CaseAction`, `CaseType`, `CaseStatus` și obiectul valoare `CaseNumber`[cite: 1].
* **Fluxuri și Operațiuni**: Deschiderea tichetelor/cazurilor HR, adăugarea acțiunilor rezolutive și închiderea cazurilor prin `EmployeeCaseWorkflowServiceImpl`[cite: 1].

### 7. Componente Comune, Audit și Notificări (`domain.common`, `domain.notification`)
* **Elemente de Bază**: `BaseEntity` (entitate rădăcină), `DateRange`, `ContactInfo`, `HrAuditView`[cite: 1].
* **Gestiunea Excepțiilor**: `BusinessValidationException`, `EntityNotFoundException`, `AccessDeniedBusinessException`, `ConcurrentUpdateException`[cite: 1].
* **Servicii Transversale**:
    * `AccessControlServiceImpl`: Verificarea permisiunilor și rolurilor[cite: 1].
    * `HrAuditingServiceImpl`: Jurnalizarea operațiunilor critice din sistem[cite: 1].
    * `NotificationServiceImpl`: Trimiterea și gestionarea notificărilor interne (`Notification`, `NotificationType`) către utilizatori[cite: 1].

## Structura Directorului

```text
multistrat/
├── pom.xml                                 # Configurația Maven și dependențele proiectului
├── build/diagrams/                         # Diagrame de arhitectură și clase în format Mermaid (.mermaid)
└── src/
    ├── main/
    │   ├── java/org/hr/
    │   │   ├── HrApplication.java          # Punctul de intrare în aplicație
    │   │   ├── config/                     # Configurații Spring (AppScanConfig)
    │   │   ├── domain/                     # Entități, Value Objects, Enums, Views, Exceptions
    │   │   ├── services/                   # Servicii de calcul, validare, audit, securitate
    │   │   ├── application/                # Servicii de workflow (orchestrare use-cases)
    │   │   └── infrastructure/             # Repozitorii in-memory și Factories pentru entități
    │   └── resources/
    │       └── application.properties      # Proprietăți de configurare ale aplicației
    └── test/
        └── java/org/hr/                    # Suită completă de teste unitare și de integrare
```

## Șabloane de Proiectare (Design Patterns) Utilizate

* **Repository Pattern**: Abstractizează accesul la date prin interfețe dedicate (`IEntityRepository`, `IEmployeeEntityRepository` etc.) și implementări generice în memorie (`InMemoryEntityRepositoryBase`)[cite: 1].
* **Factory Pattern**: Centralizează logica de instanțiere și inițializare validă a agregatelor complexe (`EmployeeEntityFactoryBase`, `RecruitmentEntityFactoryBase`, `LeaveEntityFactoryBase`, `TrainingEntityFactoryBase`, `DevelopmentPlanEntityFactoryBase`, `EmployeeCaseEntityFactoryBase`)[cite: 1].
* **Separation of Concerns (SoC) în Servicii**: Logica de domeniu este împărțită între:
    * *Computing Services* (calculează indicatori, zile libere, scoruri, procente de îndeplinire fără a modifica starea externă)[cite: 1].
    * *Validating Services* (aplică reguli de validare complexe și aruncă `BusinessValidationException`)[cite: 1].
    * *Workflow Services* (coordonează tranzacțiile, apelează validările, salvează în repozitorii și declanșează notificări sau înregistrări de audit)[cite: 1].

## Testare și Calitatea Codului

Proiectul dispune de o suită de teste structurată progresiv (în `src/test/java/org/hr`), susținută de clasa utilitară `HrTestSupport`[cite: 1]:

1. **`Test1_ComputingDomainServices`**: Testează calculele matematice și de business (zile de concediu conform calendarului românesc, scoruri de recrutare, statistici training/dezvoltare)[cite: 1].
2. **`Test2_ValidatingDomainServices`**: Verifică regulile de validare și declanșarea corectă a excepțiilor de business[cite: 1].
3. **`Test3_RecruitmentWorkflowService`**: Testează cap-coadă fluxul de recrutare (cerere -> post vacant -> aplicare -> interviu -> ofertă)[cite: 1].
4. **`Test4_LeaveWorkflowService`**: Testează fluxul de solicitare, aprobare/respingere și recalculare a soldului de concediu[cite: 1].
5. **`Test5_TrainingWorkflowService`**: Testează planificarea sesiunilor de curs, înrolarea și dobândirea competențelor[cite: 1].
6. **`Test6_DevelopmentAndCaseWorkflowServices`**: Testează fluxurile pentru planurile de dezvoltare individuală și gestionarea cazurilor HR[cite: 1].
7. **`Test7_AuditNotificationsAndRepositories`**: Testează infrastructura de persistență, concurența, serviciul de notificări și jurnalul de audit[cite: 1].

## Cerințe și Rulare

### Precondiții
* **Java JDK** (versiunea 17 sau superioară recomandată)
* **Apache Maven** 3.8+[cite: 1]

### Comenzi Maven

Pentru compilarea proiectului[cite: 1]:
```bash
mvn clean compile
```

Pentru rularea întregii suite de teste[cite: 1]:
```bash
mvn test
```

Pentru rularea unei singure clase de test (ex. fluxul de concedii)[cite: 1]:
```bash
mvn test -Dtest=Test4_LeaveWorkflowService
```

Pentru împachetarea aplicației[cite: 1]:
```bash
mvn package
```