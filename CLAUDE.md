# CLAUDE.md — Stellar Backend

## Identidad del proyecto

Backend de **Stellar**, una plataforma SaaS edtech para docentes de educación básica, media superior y superior en México. Monolito modular con Spring Boot y Clean Architecture (sin hexagonal).

**Estado actual**: Todos los módulos están implementados (Kernel, Identity, Academic, Analytics). Este documento es la referencia arquitectónica y de convenciones del proyecto.

---

## Stack tecnológico

| Capa | Tecnología | Versión / Notas |
|------|-----------|-----------------|
| Lenguaje | Java | 25 LTS |
| Framework | Spring Boot | 3.5.x |
| Build | Maven | 3.9+ |
| Base de datos | PostgreSQL | 16+ (Amazon RDS en producción) |
| Migraciones DB | Flyway | Scripts versionados |
| Caché | Redis | 7+ (Amazon ElastiCache en producción) |
| Seguridad | Spring Security + JWT | jjwt 0.12+ |
| Email | AWS SES | Via AWS SDK v2 |
| Cloud | AWS | RDS, ElastiCache, SES, S3, CloudWatch |
| Testing | JUnit 5 + Mockito + Testcontainers | — |
| Docs API | SpringDoc OpenAPI | 2.x |

---

## Arquitectura: Monolito modular

### Principios

1. **Un solo deployable** — un JAR ejecutable con todos los módulos.
2. **4 módulos**: Kernel, Identity, Academic, Analytics.
3. **Clean Architecture directa**: controller → service → repository → entity. Sin puertos, sin adaptadores.
4. **Kernel robusto** — absorbe toda infraestructura transversal.
5. **Comunicación entre módulos** — vía interfaces públicas de services o eventos internos (ApplicationEventPublisher).
6. **Pragmatismo sobre pureza** — si funciona con una clase simple, no crear tres capas de abstracción.

### Módulos

| Módulo | Responsabilidad |
|--------|----------------|
| **Kernel** | Seguridad, multi-tenancy, eventos, excepciones, paginación, email (SES), auditoría, config AWS |
| **Identity** | Auth (validate → login en dos pasos), users, tenants, roles, permissions, invitaciones, verificación de email, reset de contraseña |
| **Academic** | Grupos, materias, estudiantes, inscripciones, calificaciones (ponderadas), asistencia (4 estados), períodos académicos |
| **Analytics** | Alertas de riesgo (6 factores), reportes, notificaciones (in-app + email), estadísticas de dashboard |

### Estructura de cada módulo

```
modulo/
├── controller/
├── service/
├── repository/      # Spring Data JPA directo
├── entity/          # Entidades JPA
├── dto/
│   ├── request/
│   └── response/
└── event/
```

### Reglas de comunicación

```
PERMITIDO:
  Academic → consulta datos de Identity (usuarios) via interface pública
  Analytics → consume eventos de Academic (GradeRecordedEvent, AttendanceRecordedEvent)
  Analytics → lee datos de Academic via repositorios (read-only queries)
  Cualquier módulo → usa servicios del Kernel

PROHIBIDO:
  Identity NO conoce Academic ni Analytics
  Academic NO conoce Analytics
  Nunca imports cruzados entre services de distintos módulos
```

---

## Estructura de paquetes

```
com.academic.stellar/
├── StellarApplication.java
│
├── kernel/
│   ├── security/           # SecurityConfig, JwtTokenProvider, JwtAuthenticationFilter, CurrentUser, CurrentUserResolver
│   ├── multitenancy/       # TenantContext, TenantFilter
│   ├── persistence/        # AuditableEntity, TenantAwareEntity
│   ├── exception/          # GlobalExceptionHandler, BusinessException, ResourceNotFoundException, UnauthorizedException, ForbiddenException, ErrorResponse
│   ├── pagination/         # PagedResponse
│   ├── event/              # DomainEvent, EventPublisher
│   ├── email/              # EmailService, EmailTemplate, SesEmailService
│   ├── aws/                # AwsConfig, SesConfig
│   ├── audit/              # AuditLogger, AuditableEntity
│   ├── validation/         # ValidationUtils
│   └── config/             # AppProperties, AuditConfig, JacksonConfig, CorsConfig, RedisConfig, WebMvcConfig
│
├── identity/
│   ├── controller/         # AuthController, UserController, TenantController, InvitationController, RoleController, PermissionController
│   ├── service/            # AuthService, UserService, TenantService, InvitationService, RoleService, EmailVerificationService, PasswordResetService
│   ├── repository/         # UserRepository, TenantRepository, RoleRepository, PermissionRepository, RefreshTokenRepository, EmailVerificationTokenRepository, PasswordResetTokenRepository
│   ├── entity/             # User, Tenant, Role, Permission, RefreshToken, EmailVerificationToken, PasswordResetToken
│   ├── dto/request/        # LoginRequest, RegisterRequest, RefreshTokenRequest, ForgotPasswordRequest, ResetPasswordRequest, CreateUserRequest, UpdateUserRequest, CreateRoleRequest
│   ├── dto/response/       # AuthResponse, UserResponse, TenantResponse, RoleResponse
│   └── event/              # UserRegisteredEvent, TenantCreatedEvent
│
├── academic/
│   ├── controller/         # GroupController, SubjectController, StudentController, AssistanceController, GradeController
│   ├── service/            # GroupService, SubjectService, StudentService, AssistanceService, GradeService, AverageCalculationService
│   ├── repository/         # GroupRepository, SubjectRepository, StudentRepository, AssistanceRepository, EnrollmentRepository, GradeRepository
│   ├── entity/             # Group, Subject, Student, Enrollment, Grade, Assistance, AcademicPeriod, EvaluationPeriod, TenantAcademicConfig, PeriodType, AttendanceStatus
│   ├── dto/...
│   └── event/              # GradeRecordedEvent, AttendanceRecordedEvent, StudentEnrolledEvent
│
└── analytics/
    ├── controller/         # AlertController, ReportController, NotificationController, RiskWeightsController, StatsController
    ├── service/            # RiskDetectionService, RiskCalculationEngine, AlertService, NotificationService, ReportGenerationService, AcademicStatsService, RiskEventListener
    ├── repository/         # AlertRepository, InAppNotificationRepository, NotificationPreferenceRepository, RiskWeightsRepository, ReportRepository
    ├── entity/             # AcademicAlert, AlertStatus, RiskLevel, RiskWeights, InAppNotification, NotificationPreference, EmailFrequency, Report, ReportType, ReportStatus
    ├── dto/...
    └── event/              # AlertTriggeredEvent
```

---

## Convenciones de código

- **Idioma del código**: inglés. Mensajes al usuario final: español.
- **Java Records** para DTOs. Bean Validation en requests.
- **Lombok**: @Getter, @RequiredArgsConstructor, @NoArgsConstructor, @Slf4j.
- **Entidades**: sin setters públicos. Factory methods + métodos de negocio semánticos. Constructor protected para JPA.
- **Repositories**: Spring Data JPA directo. SIEMPRE filtrar por tenant_id.
- **Services**: @Transactional(readOnly=true) en clase, @Transactional en métodos de escritura.
- **Controllers**: delgados, versionados /api/v1/, documentados con SpringDoc.
- **Errores**: todo pasa por GlobalExceptionHandler. Nunca stacktraces al cliente.
- **Multi-tenancy**: columna discriminadora tenant_id. TenantContext via ThreadLocal. Toda entidad de negocio extiende TenantAwareEntity.
- **Email**: EmailService interface en kernel. ConsoleEmailService (dev) / SesEmailService (prod).
- **Tests**: JUnit 5 + Mockito para unit, Testcontainers para integration. Naming: should_X_when_Y().
- **BigDecimal** para todas las calificaciones — nunca float/double para scores.
- **Los eventos usan @TransactionalEventListener** con phase=AFTER_COMMIT.
- **Queries pesados** (promedios masivos, estadísticas) deben usar queries nativos de PostgreSQL.

---

## API Endpoints

### Identity Module

```
# Auth — flujo en dos pasos: validate → login
POST   /api/v1/auth/register          # Registro: crea tenant + admin user
POST   /api/v1/auth/validate          # Paso 1: Validar credenciales → { valid, available tenants }
POST   /api/v1/auth/login             # Paso 2: Login con tenant seleccionado → { accessToken, refreshToken }
GET    /api/v1/auth/me                # Perfil del usuario autenticado
GET    /api/v1/auth/me/permissions    # Permisos del usuario autenticado
GET    /api/v1/auth/me/tenants        # Tenants asociados al usuario
POST   /api/v1/auth/refresh           # Renueva tokens
POST   /api/v1/auth/switch-tenant     # Cambia tenant activo (nuevos tokens)
POST   /api/v1/auth/logout            # Invalida refresh token
POST   /api/v1/auth/verify-email      # Verificar email con token
POST   /api/v1/auth/forgot-password   # Solicitar reset de contraseña
POST   /api/v1/auth/reset-password    # Resetear contraseña

# Invitaciones
POST   /api/v1/invitations/external   # Invitar usuario externo al tenant
POST   /api/v1/invitations/internal   # Crear invitación interna (sin email)
POST   /api/v1/invitations/accept     # Aceptar invitación con token
POST   /api/v1/invitations/revoke     # Revocar invitación
GET    /api/v1/invitations            # Listar invitaciones del tenant
POST   /api/v1/invitations/register   # Registrar usuario a partir de invitación

# Users
GET    /api/v1/users                  # Listar usuarios del tenant (paginado)
GET    /api/v1/users/{id}             # Obtener usuario
POST   /api/v1/users                  # Crear usuario
PUT    /api/v1/users/{id}             # Actualizar usuario
DELETE /api/v1/users/{id}             # Soft delete

# Tenant
GET    /api/v1/tenants/current        # Info del tenant actual
PUT    /api/v1/tenants/current        # Actualizar tenant

# Roles y permisos
GET    /api/v1/roles                  # Listar roles
POST   /api/v1/roles                  # Crear rol
PUT    /api/v1/roles/{id}             # Actualizar rol
GET    /api/v1/permissions            # Listar permisos
POST   /api/v1/permissions            # Crear permiso
PUT    /api/v1/permissions/{id}       # Actualizar permiso
```

### Academic Module

```
# Grupos
GET    /api/v1/groups                 # Listar grupos
POST   /api/v1/groups                 # Crear grupo
GET    /api/v1/groups/{id}            # Detalle con estudiantes

# Materias
GET    /api/v1/subjects               # Listar materias
POST   /api/v1/subjects               # Crear materia

# Estudiantes
GET    /api/v1/students               # Listar estudiantes (paginado)
POST   /api/v1/students               # Registrar estudiante
GET    /api/v1/students/{id}          # Perfil completo
GET    /api/v1/students/{id}/dashboard # Dashboard individual

# Asistencia (nota: usa /assistance en el backend)
POST   /api/v1/assistance             # Registrar asistencia
POST   /api/v1/assistance/batch       # Pase de lista completo
GET    /api/v1/assistance/student/{id} # Historial por estudiante
GET    /api/v1/assistance/subject/{id} # Historial por materia
GET    /api/v1/assistance/group/{id}   # Historial por grupo

# Calificaciones
POST   /api/v1/grades                 # Registrar calificación
POST   /api/v1/grades/batch           # Registro masivo
GET    /api/v1/grades/student/{id}    # Calificaciones de un estudiante
GET    /api/v1/grades/subject/{id}    # Calificaciones de una materia
GET    /api/v1/grades/student/{studentId}/subject/{subjectId}/average  # Promedio ponderado

# Períodos académicos
POST   /api/v1/academic/periods       # Crear ciclo con períodos de evaluación
GET    /api/v1/academic/periods       # Listar ciclos
GET    /api/v1/academic/periods/active # Ciclo activo
PUT    /api/v1/academic/periods/{id}/activate # Activar ciclo

# Configuración académica
GET    /api/v1/academic/config        # Config del tenant
PUT    /api/v1/academic/config        # Actualizar config

# Inscripciones
POST   /api/v1/students/enrollments               # Inscribir estudiante
GET    /api/v1/students/{id}/enrollments           # Inscripciones de un estudiante
DELETE /api/v1/students/enrollments/{enrollmentId}  # Cancelar inscripción
```

### Analytics Module

```
# Alertas de riesgo
GET    /api/v1/alerts                 # Listar activas (paginado, filtrable)
GET    /api/v1/alerts/student/{id}    # Alertas de un estudiante
GET    /api/v1/alerts/summary         # Conteo por nivel
PUT    /api/v1/alerts/{id}/dismiss    # Descartar con motivo
POST   /api/v1/alerts/evaluate/{studentId}  # Re-evaluación manual
POST   /api/v1/alerts/evaluate-all    # Evaluación masiva

# Pesos de riesgo
GET    /api/v1/risk/weights           # Pesos actuales
PUT    /api/v1/risk/weights           # Actualizar (deben sumar 1.0)

# Notificaciones
GET    /api/v1/notifications          # In-app del usuario autenticado
GET    /api/v1/notifications/unread-count
PUT    /api/v1/notifications/{id}/read
PUT    /api/v1/notifications/read-all
GET    /api/v1/notifications/preferences
PUT    /api/v1/notifications/preferences

# Reportes
POST   /api/v1/reports/generate       # Solicitar generación
GET    /api/v1/reports                # Listar reportes
GET    /api/v1/reports/{id}           # Estado/detalle
GET    /api/v1/reports/{id}/download  # Descargar (redirect a S3 presigned URL)

# Dashboard y estadísticas
GET    /api/v1/dashboard              # Dashboard principal
GET    /api/v1/dashboard/risk-summary # Resumen de riesgo
GET    /api/v1/stats/group/{groupId}
GET    /api/v1/stats/group/{groupId}/subject/{subjectId}
```

---

## Modelo de datos clave

### Multi-tenancy
- Columna discriminadora `tenant_id` en toda tabla de negocio.
- `TenantContext` (ThreadLocal) se setea en `TenantFilter` desde el JWT.
- La entidad `Tenant` NO tiene `tenant_id` (es la raíz).

### Períodos académicos
- Dos niveles: `AcademicPeriod` (ciclo) → `EvaluationPeriod` (parciales dentro del ciclo).
- Cada `EvaluationPeriod` tiene un peso (`weight`). La suma de pesos DEBE ser 100%.
- Tipo de período configurable por tenant: SEMESTER, TRIMESTER, QUARTER.

### Calificaciones
- `Grade` = una calificación de un alumno en un `EvaluationPeriod`.
- Unique constraint: `(enrollment_id, evaluation_period_id)`.
- Promedio ponderado: `Σ (score × weight / 100)`.
- Siempre `BigDecimal`, nunca float/double.
- Escala configurable por tenant (0-10, 5-10, 0-100).

### Asistencia
- 4 estados: `PRESENT`, `ABSENT`, `LATE`, `JUSTIFIED`.
- `JUSTIFIED` requiere `justificationReason`.
- `PRESENT` y `LATE` cuentan como asistencia. Solo `ABSENT` cuenta como falta.
- Unique constraint: `(enrollment_id, date)`.

### Algoritmo de riesgo
- 6 factores ponderados: promedio bajo (30%), tendencia negativa (15%), baja asistencia (20%), faltas consecutivas (10%), materias reprobadas (15%), urgencia temporal (10%).
- Score final 0-100. Niveles: LOW (0-24), MEDIUM (25-49), HIGH (50-74), CRITICAL (75-100).
- Pesos configurables por tenant (deben sumar 1.0).
- Se recalcula automáticamente al registrar calificación o ausencia (via eventos).

### Notificaciones
- Dos canales: in-app (siempre) y email (via SES).
- Preferencias por docente: nivel mínimo para notificar, canales habilitados, frecuencia de email (IMMEDIATE, DAILY_DIGEST, WEEKLY_DIGEST).

---

## Reglas para el asistente

- Antes de escribir código, explica qué vas a hacer y por qué.
- Genera código completo — no TODOs ni placeholders.
- Pregunta si algo no está claro.
- Cada archivo debe compilar.
- Escribe migraciones Flyway para cambios de schema.
- Sigue SOLID.
- Nunca expongas stacktraces al cliente.
- Usa TenantContext para obtener tenant — nunca del request.
- Respeta la estructura de paquetes existente — `entity/` no `model/`.