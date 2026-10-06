# Complaint Management System

A web-based complaint registration, tracking, and resolution platform built with Spring Boot and React.

## Architecture Overview

### Layered Architecture + MVC + Observer Pattern

```
┌─────────────────────────────────────────────────────┐
│           Frontend (React + React Router)           │
│                    [View/UI]                        │
└──────────────────────┬──────────────────────────────┘
                       │ REST API (JSON)
┌──────────────────────▼──────────────────────────────┐
│              Spring Boot Backend                    │
│  ┌──────────────────────────────────────────────┐   │
│  │ Controller Layer (HTTP Request/Response)     │   │
│  └──────────────┬───────────────────────────────┘   │
│  ┌──────────────▼───────────────────────────────┐   │
│  │ Service Layer (Business Logic, Transactions) │   │
│  │        │                                     │   │
│  │        ├─── ComplaintService                │   │
│  │        ├─── NotificationService             │   │
│  │        └─── Other Services...               │   │
│  └──────────────┬───────────────────────────────┘   │
│  ┌──────────────▼───────────────────────────────┐   │
│  │ Repository Layer (Spring Data JPA)          │   │
│  │  Entities ─── DTOs (mapped in Services)     │   │
│  └──────────────┬───────────────────────────────┘   │
│  ┌──────────────▼───────────────────────────────┐   │
│  │ Observer Pattern (Event-Driven)             │   │
│  │  ComplaintEventPublisher (Subject)          │   │
│  │    ├─ InAppNotificationObserver             │   │
│  │    ├─ AuditLogObserver                      │   │
│  │    └─ OverdueEscalationObserver             │   │
│  └──────────────────────────────────────────────┘   │
└──────────────────────┬──────────────────────────────┘
                       │
┌──────────────────────▼──────────────────────────────┐
│          MySQL Database (Flyway Migrations)        │
│  users | categories | priorities | complaints |    │
│  complaint_history | attachments | notifications   │
└───────────────────────────────────────────────────┘
```

**MVC Mapping:**
- **Model:** JPA entities and DTOs
- **View:** React components consuming JSON API
- **Controller:** Spring REST controllers handling HTTP

**Observer Pattern:**
- Decouples complaint state changes from notification logic
- Services publish `ComplaintEvent` instances
- Registered observers react independently (notification, audit, escalation)
- Adding a new observer requires zero changes to `ComplaintService`

## Tech Stack

**Backend:**
- Java 17+, Spring Boot 3.3, Spring Security with JWT
- MySQL 8, Spring Data JPA, Flyway migrations
- Swagger/OpenAPI documentation

**Frontend:**
- React 18, React Router 6, Vite
- Axios for HTTP, Recharts for admin charts
- Plain CSS with design tokens (no Tailwind/Bootstrap/Material)

**Deployment:**
- Docker & Docker Compose (local development)
- GitHub Actions CI/CD
- Cloud: Render/Railway backend, Vercel/Netlify frontend, managed MySQL

## Running Locally

### Prerequisites
- Docker & Docker Compose
- OR: JDK 17+, Node 20, MySQL 8

### With Docker Compose (Recommended)

```bash
cp .env.example .env
docker compose up --build
```

Access:
- **Frontend:** http://localhost
- **Backend API:** http://localhost:8080/api
- **Swagger UI:** http://localhost:8080/api/swagger-ui.html

Default admin login:
- **Email:** admin@cms.local
- **Password:** admin123

### Without Docker (Local Development)

**Backend:**
```bash
cd backend
mvn clean install
export JWT_SECRET="dev-secret-key"
export DB_HOST=localhost DB_USER=cms_user DB_PASSWORD=cms_password
mvn spring-boot:run
```

**Frontend:**
```bash
cd frontend
npm install
npm run dev
```

## Running Tests

```bash
cd backend
mvn test
```

Tests include:
- Service layer lifecycle and business rule validation
- Observer pattern event publishing and subscription
- Controller access control (role-based)
- Integration tests with Testcontainers MySQL

## Project Structure

```
complaint-management-system/
├── backend/                          # Spring Boot application
│   ├── src/main/java/com/cms/complaints/
│   │   ├── config/                   # Security, CORS, OpenAPI config
│   │   ├── controller/               # REST controllers
│   │   ├── dto/                      # Data Transfer Objects
│   │   ├── entity/                   # JPA entities
│   │   ├── enums/                    # Status, ActionType, Role
│   │   ├── exception/                # Global exception handling
│   │   ├── observer/                 # Observer pattern classes
│   │   ├── repository/               # Spring Data JPA repositories
│   │   ├── security/                 # JWT, BCrypt, filters
│   │   └── service/                  # Business logic
│   └── src/main/resources/
│       └── db/migration/             # Flyway SQL migrations
│
├── frontend/                         # React application
│   ├── src/
│   │   ├── api/                      # Axios client & API calls
│   │   ├── components/               # Shared UI components
│   │   ├── context/                  # Auth context provider
│   │   ├── pages/                    # Page components (auth, complaints, admin)
│   │   ├── styles/                   # CSS modules & design tokens
│   │   └── utils/                    # Helper functions
│   ├── vite.config.js
│   └── nginx.conf                    # Production nginx config
│
├── docker-compose.yml                # Local dev environment
├── .github/workflows/ci.yml          # GitHub Actions CI
├── .env.example                      # Environment variables template
└── README.md
```

## Database Schema

All tables created via Flyway migrations:

- **users** – Registration, roles (COMPLAINANT, STAFF, ADMIN), authentication
- **categories** – Complaint classification (Billing, Service Quality, etc.)
- **priorities** – Priority levels with SLA hours
- **complaints** – Core complaint record with status, assignment, resolution
- **complaint_history** – Immutable audit trail of every action
- **attachments** – File uploads (PNG, JPG, PDF; max 5MB each, 3 per complaint)
- **notifications** – Event-driven notifications (read/unread tracking)

Indexes optimized for common queries: ticket ID lookup, status filtering, assignment, overdue detection.

## REST API Endpoints

### Authentication
- `POST /auth/register` – Create account
- `POST /auth/login` – JWT login
- `POST /auth/logout` – Logout (stateless; client discards token)
- `GET /auth/me` – Current user info

### Complaints (Complainant)
- `POST /complaints` – File a complaint (multipart with attachments)
- `GET /complaints/my` – My complaints (paged, filtered by status)
- `GET /complaints/ticket/{ticketId}` – Track by ticket number
- `GET /complaints/{id}` – View details
- `POST /complaints/{id}/remarks` – Add remarks
- `POST /complaints/{id}/feedback` – Rate after resolution (1–5 stars)
- `PATCH /complaints/{id}/reopen` – Reopen with reason

### Complaints (Staff/Admin)
- `GET /complaints` – All complaints (full filters)
- `PATCH /complaints/{id}/assign` – Assign to staff (admin only)
- `PATCH /complaints/{id}/status` – Update status
- `PATCH /complaints/{id}/resolve` – Mark resolved (requires summary)
- `PATCH /complaints/{id}/close` – Close complaint

### Notifications
- `GET /notifications` – User's notifications
- `GET /notifications/unread-count` – Badge count
- `PATCH /notifications/{id}/read` – Mark one as read
- `PATCH /notifications/read-all` – Mark all as read

### Admin
- `GET/POST/PUT /admin/categories` – Manage complaint categories
- `GET/POST/PUT /admin/priorities` – Manage priority levels
- `GET /admin/users` – User list
- `PATCH /admin/users/{id}/role` – Change role
- `PATCH /admin/users/{id}/active` – Activate/deactivate
- `GET /admin/staff` – Staff list (for assignment dropdown)
- `GET /admin/reports/summary` – Complaint counts, avg resolution time, avg rating
- `GET /admin/reports/trend?days=30` – Daily created vs. resolved counts

Full documentation at `/swagger-ui.html` (includes request/response schemas and try-it-out).

## Complaint Lifecycle

```
SUBMITTED ──┐
            ├─→ ASSIGNED (admin assigns)
            │     ├─→ IN_PROGRESS (staff starts work)
            │     │     └─→ RESOLVED (staff records resolution_summary)
            │     │         ├─→ CLOSED (admin or staff closes)
            │     │         └─→ REOPENED (complainant or admin reopens)
            │     │           └─→ ASSIGNED or IN_PROGRESS
            │     │
            └─────→ REOPENED (if not yet assigned)
```

Invalid transitions return `409 Conflict` with a clear error message.

Every state change, assignment, remark, and resolution writes an immutable `complaint_history` row in the same transaction.

## Observer Pattern – Example

Adding a new observer (e.g., email notifications) requires zero changes to the service layer:

```java
@Component
public class EmailNotificationObserver implements ComplaintObserver {
    @Override
    public void update(ComplaintEvent event) {
        if (event.getType() == EventType.CREATED) {
            sendEmailTo(event.getComplaint().getCreatedBy(), 
                       "Your complaint has been filed...");
        }
    }
}
```

Register by adding `@Component` – Spring auto-wires it into `ComplaintEventPublisher`. Services remain clean; notification logic stays decoupled.

## Escalation & Overdue Detection

A scheduled job runs every 15 minutes:
1. Finds complaints where `due_at < now` and status is not RESOLVED or CLOSED
2. If not already escalated, sets `escalated = true` and publishes an ESCALATED event
3. `OverdueEscalationObserver` creates a notification for admins and assigned staff
4. Admin dashboard highlights overdue complaints

## Security Notes

- **Authentication:** JWT with 1-hour expiry, signed with `JWT_SECRET` environment variable
- **Passwords:** Hashed with BCrypt; never returned in API responses
- **Authorization:** Role-based access enforced in controllers and service layer. Complainants see only their complaints; staff see only assigned ones; admins see all.
- **Input Validation:** Jakarta Bean Validation on all request DTOs
- **Error Handling:** Global `@RestControllerAdvice` returns JSON with status, message, and field errors—never exposes stack traces
- **CORS:** Configured from `CORS_ORIGINS` environment variable
- **HTTPS:** Enabled in production; local dev uses HTTP for convenience

## Environment Variables

See `.env.example`. Key ones:
- `DB_HOST`, `DB_USER`, `DB_PASSWORD` – MySQL connection
- `JWT_SECRET` – Signing key for tokens (change for production)
- `CORS_ORIGINS` – Frontend URL(s) allowed
- `UPLOAD_DIR` – Where files are stored on disk
- `SPRING_PROFILES_ACTIVE` – Set to `prod` in production

## Frontend Design

**Aesthetic:** Editorial "case file" style—calm, functional, human-written.

**Palette:**
- Background: warm off-white (#f6f2ea)
- Text: near-black (#1d1b18)
- Accent: terracotta (#b5441f)
- Success: forest green (#2f5d46)
- Warning: mustard (#c28a1b)

**Typography:**
- Headings: Fraunces (serif display)
- Body: DM Sans or Instrument Sans (clean sans)
- Monospace: JetBrains Mono (for ticket IDs)

**Components:**
- Status labels styled as rubber stamps (bordered, slightly rotated, uppercase)
- Timeline view of complaint history
- Toast notifications for feedback
- Skeleton loaders on data screens
- Responsive at 360px (sidebar collapses to drawer)

No Tailwind, Bootstrap, Material UI, or Ant Design. Plain CSS with variables.

## Deployment

### Local with Docker Compose
```bash
docker compose up --build
```

### Cloud Deployment

**Option 1: Multi-service (recommended)**
1. **Database:** Aiven free MySQL or Railway MySQL
2. **Backend:** Render or Railway (from `backend/Dockerfile`)
   - Set `SPRING_PROFILES_ACTIVE=prod`
   - Add `/actuator/health` as health check
3. **Frontend:** Vercel or Netlify (from `frontend/`)
   - Set `VITE_API_BASE_URL` to backend URL
   - Enable SPA fallback for React Router

**Option 2: Single VPS**
```bash
docker compose -f docker-compose.yml up -d
# Behind Caddy or nginx with Let's Encrypt for HTTPS
```

Production checklist:
- [ ] `JWT_SECRET` is a strong random string
- [ ] Admin password changed from default
- [ ] `ddl-auto=validate` (never relax this in prod)
- [ ] Swagger disabled (`springdoc.swagger-ui.enabled=false` in prod profile)
- [ ] Database backups configured (daily `mysqldump` to S3/backup service)
- [ ] HTTPS enforced (HSTS headers)
- [ ] Log aggregation set up (Datadog, Splunk, etc.)

## Assumptions

- Users have modern browsers and stable internet (no IE11)
- Attachments are the only file-storage requirement; no email/SMS integrations in baseline
- SLA timers are advisory; escalation is automated but manual override is admin responsibility
- Feedback ratings are collected after resolution; reopening does not reset them
- Ticket IDs follow format `CMP-YYYY-NNNNNN` and are immutable

## Notes for Developers

- **No comments in code.** Use clear naming; the code should read like well-written prose.
- **One commit per logical chunk.** Aim for 5–15 line commit messages explaining the "why."
- **Test before pushing.** `mvn test` for backend, `npm run build` for frontend.
- **Check Swagger.** Every endpoint must be documented with summaries, parameter descriptions, and response codes.
- **Verify responsive design.** Test at 360px, 768px, and 1920px widths.

## Contributing

1. Create a feature branch: `git checkout -b feature/my-feature`
2. Commit with clear messages: `git commit -m "short description"`
3. Push and open a pull request
4. Ensure CI passes and tests are green

## License

This project is part of a VIT-AP University software engineering course.
