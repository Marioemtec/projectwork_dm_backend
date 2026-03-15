# ClinicaPlus - Sistema di Prenotazione e Gestione Visite Mediche

## Descrizione Progetto

ClinicaPlus è un'applicazione full-stack API-based sviluppata per un'organizzazione del settore sanitario (clinica privata multi-specialistica). L'applicazione fornisce un completo sistema di prenotazione visite mediche e gestione referti medici.

SITO WEB IN PRODUZIONE --> https://projectwork-dm-frontend-1.onrender.com

N.B. Puo' richiedere fino a 50 sec di caricamento in caso di servizi in stadby dato che viene utilizzata la versione free di hosting

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
- **Backend**: http://localhost:8080/api o in produzione https://projectwork-dm-backend.onrender.com/api
- **Swagger API Docs**: http://localhost:8080/api/swagger-ui.html o in produzione https://projectwork-dm-backend.onrender.com/api/swagger-ui.html

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
│ role (PATIENT,   │
│       DOCTOR)    │
│ active           │
│ createdAt        │
│ updatedAt        │
└───────┬──────────┘
       │
   ┌────┴────┐
   │         │
┌──▼──────┐ ┌▼──────────┐
│PATIENT  │ │DOCTOR     │
├─────────┤ ├───────────┤
│id (PK)  │ │id (PK)    │
│user_id  │ │user_id    │
│taxCode  │ │specializ. │
│DOB      │ │licenseNum │
│bloodType│ │biography  │
│medHist. │ │available  │
└───┬─────┘ └─────┬─────┘
    │             │
    │      ┌──────▼──────────────┐
    │      │   MEDICAL_SERVICE   │
    │      ├─────────────────────┤
    │      │ id (PK)             │
    │      │ doctor_id (FK)      │
    │      │ name                │
    │      │ description         │
    │      │ price               │
    │      │ durationMinutes     │
    │      │ active              │
    │      │ createdAt           │
    │      └─────────┬───────────┘
    │                │
┌───▼────────────────▼────┐
│      APPOINTMENT        │
├─────────────────────────┤
│ id (PK)                 │
│ patient_id (FK)         │
│ doctor_id (FK)          │
│ service_id (FK, NULL)   │
│ appointmentDateTime     │
│ durationMinutes         │
│ status (ENUM)           │
│ notes                   │
│ createdAt, updatedAt    │
└─────────────┬───────────┘
            │
     ┌───────▼───────────┐
     │   MEDICAL_REPORT   │
     ├────────────────────┤
     │ id (PK)            │
     │ appointment_id (FK)│
     │ doctor_id (FK)     │
     │ title              │
     │ diagnosis          │
     │ prescription       │
     │ notes              │
     │ createdAt, updatedAt
     └────────────────────┘
```

## Documento UML (Diagramma delle Classi)

```
┌───────────────────────────┐
│           User            │
├───────────────────────────┤
│ -id: Long                 │
│ -email: String            │
│ -password: String         │
│ -firstName: String        │
│ -lastName: String         │
│ -phoneNumber: String      │
│ -role: UserRole           │
│ -active: Boolean          │
└───────────────▲───────────┘
          │
    ┌────────┴────────┐
┌──────▼───────┐ ┌───────▼──────┐
│   Patient    │ │    Doctor    │
└──────────────┘ └───────┬──────┘
                │ 1:N
            ┌──────▼────────────┐
            │  MedicalService   │
            └──────┬────────────┘
                │ 1:N
┌──────────────┐ N:1 ┌───▼────────────┐ N:1 ┌──────────────┐
│   Patient    │─────│  Appointment   │─────│    Doctor    │
└──────────────┘     ├────────────────┤     └──────────────┘
            │ -service:      │
            │   MedicalService?
            │ -status:        │
            │   AppointmentStatus
            └──────┬─────────┘
                │ 1:N
            ┌──────▼─────────┐
            │  MedicalReport │
            └────────────────┘
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

## Guida all'Installazione

### Prerequisiti
- Java 21 JDK
- Node.js 18+
- Docker e Docker Compose
- Git

### Passo 1: Avviare il Database

```bash
cd PW
docker-compose up -d
```

Verifica che PostgreSQL sia in esecuzione:
```bash
docker ps
```

### Passo 2: Build e Avvio Backend

```bash
cd projectwork_dm_backend

# Compilare il progetto
./gradlew build

# Avviare l'applicazione
./gradlew bootRun
```

## Test Funzionale

### Account Test e usabili in produzione(Sito live)

**Paziente:**
- Email: `patient1@clinicaplus.com`
- Password: `Password123`

**Medico:**
- Email: `dottore.giovanni@clinicaplus.com`
- Password: `Password123`

### Limitazioni Attuali
- Token refresh non implementato
- Gestione email non implementata
- Notifiche real-time non implementate
