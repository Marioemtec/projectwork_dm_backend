# ClinicaPlus - Sistema di Prenotazione e Gestione Visite Mediche

## Descrizione Progetto

ClinicaPlus è un'applicazione full-stack API-based sviluppata per un'organizzazione del settore sanitario (clinica privata multi-specialistica). L'applicazione fornisce un completo sistema di prenotazione visite mediche e gestione referti medici.

**SITO WEB DEL PROGETTO IN PRODUZIONE** --> https://projectwork-dm-frontend-1.onrender.com

**DEMO DEL PROGETTO VIDEO** --> https://youtu.be/TBockHQ5K6o

N.B. Puo' richiedere fino a 50 sec di caricamento in caso di servizi in stadby dato che viene utilizzata la versione free di host del servizio backend.

### Caratteristiche Principali
- Autenticazione e registrazione utenti (pazienti e medici)
- Sistema di prenotazione appuntamenti
- Gestione prestazioni mediche del medico (nome, prezzo, durata)
- Gestione referti medici
- Download referti in PDF
- Visualizzazione elenco medici disponibili
- Dashboard personalizzata con statistiche per pazienti e medici
- Storico appuntamenti separato dopo caricamento referto
- API RESTful ben documentata (Swagger/OpenAPI)

## Architettura Tecnica

### Backend
- **Framework**: Spring Boot 3.2
- **Linguaggio**: Java 21
- **Build Tool**: Gradle
- **Database**: PostgreSQL 15
- **Autenticazione**: JWT (JSON Web Tokens)
- **Documentazione API**: OpenAPI 3.0 (Swagger)

### Frontend
Repo di frontend --> https://github.com/Marioemtec/projectwork_dm_frontend

### Database
- PostgreSQL 15 (via Docker) per i test in locale e via Supabase per la produzione.

### Documentazione API
- **Swagger API Docs**: http://localhost:8080/api/swagger-ui.html o in produzione https://projectwork-dm-backend.onrender.com/api/swagger-ui.html

### Profili applicazione Spring:
[Profilo locale](src/main/resources/application-local.yml)

[Profilo produzione](src/main/resources/application-prod.yml)


## Modello Dati (ER Diagram)

```
┌──────────────────┐
│       USER       │
├──────────────────┤
│ id (PK)          │
│ email (UNIQUE)   │
│ password         │
│ firstName        │
│ lastName         │
│ phoneNumber      │
│ role             │
│ active           │
│ createdAt        │
│ updatedAt        │
└───────┬──────────┘
       │
   ┌────┴─────┐
   │          │
┌──▼──────┐ ┌─▼─────────┐
│PATIENT  │ │ DOCTOR    │
├─────────┤ ├───────────┤
│id (PK)  │ │id (PK)    │
│user_id  │ │user_id    │
│taxCode  │ │specializ. │
│DOB      │ │licenseNum │
│bloodType│ │biography  │
│medHist. │ │available  │
└────┬────┘ └────┬──────┘
    │           │
    │      ┌────▼─────────────────────┐
    │      │     MEDICAL_SERVICE      │
    │      ├──────────────────────────┤
    │      │ id (PK)                  │
    │      │ doctor_id (FK)           │
    │      │ institute_id (FK)        │
    │      │ name                     │
    │      │ description              │
    │      │ price                    │
    │      │ durationMinutes          │
    │      │ active                   │
    │      │ createdAt                │
    │      └──────────┬───────────────┘
    │                 │ 1:N
    │       ┌─────────▼────────────────────────┐
    │       │   MEDICAL_SERVICE_AVAILABILITY   │
    │       ├──────────────────────────────────┤
    │       │ id (PK)                          │
    │       │ medical_service_id (FK)          │
    │       │ location                         │
    │       │ dayOfWeek                        │
    │       │ startTime                        │
    │       │ endTime                          │
    │       └──────────────────────────────────┘
    │
┌────▼──────────────────────────┐
│         APPOINTMENT           │
├───────────────────────────────┤
│ id (PK)                       │
│ patient_id (FK)               │
│ doctor_id (FK)                │
│ service_id (FK, NULL)         │
│ institute_id (FK)             │
│ appointmentDateTime           │
│ durationMinutes               │
│ location                      │
│ status (ENUM)                 │
│ notes                         │
│ createdAt                     │
│ updatedAt                     │
└──────────────┬────────────────┘
            │
    ┌─────────▼───────────┐
    │    MEDICAL_REPORT   │
    ├─────────────────────┤
    │ id (PK)             │
    │ appointment_id (FK) │
    │ doctor_id (FK)      │
    │ title               │
    │ diagnosis           │
    │ prescription        │
    │ notes               │
    │ createdAt           │
    │ updatedAt           │
    └─────────────────────┘

┌──────────────────┐
│    INSTITUTE     │
├──────────────────┤
│ id (PK)          │
│ name (UNIQUE)    │
│ city             │
│ address          │
│ phone            │
│ description      │
│ active           │
│ createdAt        │
└──────────────────┘
```

## Documento UML (Diagramma delle Classi)

```
┌──────────────────────────────┐
│             User             │
├──────────────────────────────┤
│ -id: Long                    │
│ -email: String               │
│ -password: String            │
│ -firstName: String           │
│ -lastName: String            │
│ -phoneNumber: String         │
│ -role: UserRole              │
│ -active: Boolean             │
└───────────────▲──────────────┘
                │
        ┌───────┴─────────┐
┌───────▼────────┐   ┌────▼─────────┐
│    Patient     │   │    Doctor    │
└───────┬────────┘   └────┬─────────┘
        │                 │ 1..*
        │                 ▼
        │         ┌────────────────────────┐
        │         │     MedicalService     │
        │         ├────────────────────────┤
        │         │ -durationMinutes: Int  │
        │         │ -price: BigDecimal     │
        │         │ -active: Boolean       │
        │         └──────┬─────────┬───────┘
        │                │         │ 1..*
        │                │         ▼
        │                │   ┌─────────────────────────────┐
        │                │   │ MedicalServiceAvailability  │
        │                │   ├─────────────────────────────┤
        │                │   │ -location: String          │
        │                │   │ -dayOfWeek: DayOfWeek      │
        │                │   │ -startTime: LocalTime      │
        │                │   │ -endTime: LocalTime        │
        │                │   └─────────────────────────────┘
        │                │
        │                │ *..1
        │                ▼
        │         ┌────────────────────────┐
        │         │       Institute        │
        │         ├────────────────────────┤
        │         │ -name: String          │
        │         │ -city: String          │
        │         │ -address: String       │
        │         │ -active: Boolean       │
        │         └──────────▲─────────────┘
        │                    │ *..1
        ▼                    │
┌────────────────────────────┴───────┐
│            Appointment             │
├────────────────────────────────────┤
│ -appointmentDateTime: LocalDateTime│
│ -durationMinutes: Integer          │
│ -location: String                  │
│ -status: AppointmentStatus         │
│ -notes: String                     │
└───────────────┬───────────────┬────┘
                │               │
              * │               │ *
                ▼               ▼
            Patient          Doctor

Appointment *..1 MedicalService
Appointment 1..* MedicalReport
```
## Relazioni DB
![supabase schema.png](img/supabase%20schema.png)

## Design Patterns Utilizzati

### Backend
- **MVC (Model-View-Controller)**: Separazione tra controller, service e repository
- **Dependency Injection**: Utilizzato da Spring framework
- **Repository Pattern**: Data access layer
- **DTO Pattern**: Trasferimento dati tra layer
- **JWT Token Pattern**: Autenticazione stateless
- **Exception Handling**: Gestione globale delle eccezioni

## Snippet Interessanti

### 1. JWT Token Generation 
```java
public String generateToken(String email, String role) {
    Map<String, Object> claims = new HashMap<>();
    claims.put("role", role);
    return createToken(claims, email);
}
```

**Considerazioni :**
- Mostra come viene costruita l'autenticazione stateless tramite JWT.
- Evidenzia l'uso dei claim (es. `role`) per portare nel token informazioni utili all'autorizzazione.
- E un punto critico di sicurezza: da qui dipende la corretta identificazione dell'utente su ogni chiamata API.

### 2. Authentication Filter 
```java
public JwtAuthenticationFilter extends OncePerRequestFilter {
    @Override
    protected void doFilterInternal(HttpServletRequest request, 
                                   HttpServletResponse response, 
                                   FilterChain filterChain) {
        String jwt = extractJwtFromRequest(request);
        if (jwtTokenProvider.validateToken(jwt)) {
            String email = jwtTokenProvider.extractEmail(jwt);
        }
    }
}
```

**Considerazioni :**
- Rappresenta il punto in cui ogni request viene controllata prima di arrivare ai controller.
- Fa vedere il flusso completo: estrazione token, validazione, recupero identita.
- Collega sicurezza e logica applicativa: se questo passaggio e robusto, tutte le endpoint protette ereditano protezione coerente.

### 3. Appointment Service
```java
public AppointmentDTO createAppointment(Long patientId, Long doctorId, 
                                       LocalDateTime appointmentDateTime, 
                                       Integer durationMinutes) {
    Patient patient = patientRepository.findById(patientId)
        .orElseThrow(() -> new ResourceNotFoundException(...));
    Doctor doctor = doctorRepository.findById(doctorId)
        .orElseThrow(() -> new ResourceNotFoundException(...));
    
    Appointment appointment = Appointment.builder()
        .patient(patient)
        .doctor(doctor)
        .appointmentDateTime(appointmentDateTime)
        .durationMinutes(durationMinutes)
        .status(AppointmentStatus.SCHEDULED)
        .build();
    
    return mapToDTO(appointmentRepository.save(appointment));
}
```

**Considerazioni :**
- E un ottimo esempio del pattern service: validazione input, accesso repository, costruzione entita, mapping DTO.
- Mostra come viene centralizzata la business logic della prenotazione in un unico punto.
- E il cuore funzionale dell'app: la prenotazione e il caso d'uso principale lato paziente/medico.

### 4. Institute Resolution Pattern 
```java
private Institute resolveInstituteForAppointment(MedicalService service, String normalizedLocation) {
    if (service != null && service.getInstitute() != null) {
        return service.getInstitute();
    }
    
    String preferredInstituteName = (normalizedLocation != null && !normalizedLocation.isBlank())
        ? normalizedLocation.trim()
        : "Sede Centro";
    
    return instituteRepository.findByName(preferredInstituteName)
        .or(() -> instituteRepository.findFirstByActiveTrueOrderByIdAsc())
        .orElseGet(() -> instituteRepository.save(Institute.builder()
            .name(preferredInstituteName)
            .city("N/D")
            .address(preferredInstituteName)
            .active(true)
            .description("Sede creata automaticamente...")
            .build()));
}
```

**Considerazioni :**
- Dimostra il pattern di fallback a 3 livelli: usare l'istituto del servizio → cercare per nome della location → creare default automatico.
- Garantisce che ogni appuntamento avra sempre un istituto valido (nessun null), evitando violazioni di vincoli referenziali.
- Mostra robustezza architetturale: gestisce scenari incomplete (servizio senza istituto, location non trovata) in modo intelligente e riepibile.
- E critico per un'app multi-sede: assicura che tutti i dati rimangono consistenti anche in caso di configurazioni incomplete.

## Guida all'Installazione

### Prerequisiti
- Java 21 JDK
- Node.js 18+
- Docker e Docker Compose
- Git

### Passo 1: Avviare il Database

Partendo dalla root di progetto, spostarsi all'interno della folder database-docker/docker-compose.yml ed esguire il comando
```bash
cd PW
docker-compose up -d
```

Verifica che PostgreSQL sia in esecuzione:
```bash
docker ps
```

### Passo 2: Build e Avvio Backend
Ritornando nelal root di progetto dove è presente il wrapper di gradle:
```bash
cd projectwork_dm_backend

# Compilare il progetto
./gradlew build

# Avviare l'applicazione
./gradlew bootRun -Dspring.profiles.active=local
```

## Test Funzionale

### Account Test e usabili in produzione(Sito live)

**Paziente:**
- Email: `paziente1@clinicaplus.com`
- Password: `Password123!`

**Medico 1:**
- Email: `dottore.giovanni@clinicaplus.com`
- Password: `Password123!`

**Medico 2:**
- Email: `mario.rossa@clinicaplus.com`
- Password: `Password123!`

### Limitazioni Attuali
- Token refresh non implementato
- Gestione email non implementata
- Notifiche real-time non implementate
