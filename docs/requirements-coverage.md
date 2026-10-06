# Requirements Coverage

This document maps every functional and non-functional requirement from the SRS to the implementation.

## Functional Requirements (FR)

| Req | Title | Implementation | Status |
|-----|-------|----------------|--------|
| FR-01 | New user registration | `AuthController.register()`, `AuthService.register()` | ✓ |
| FR-02 | Validate registration data | `RegisterRequest` DTO with Jakarta validation | ✓ |
| FR-03 | Authenticate users | `AuthController.login()`, JWT token generation | ✓ |
| FR-04 | Role-based access | `@PreAuthorize`, `SecurityConfig`, role enforcement in services | ✓ |
| FR-05 | Logout securely | `AuthController.logout()`, client-side token removal | ✓ |
| FR-06 | Submit complaint | `ComplaintController.createComplaint()`, `ComplaintService` | ✓ |
| FR-07 | Capture complaint details | `CreateComplaintRequest` DTO (title, description, category, priority) | ✓ |
| FR-08 | Assign unique ID | `ComplaintService.generateTicketId()` format CMP-YYYY-NNNNNN | ✓ |
| FR-09 | Record creation metadata | `Complaint` entity (createdBy, createdAt) | ✓ |
| FR-10 | Support attachments | `Attachment` entity, multipart upload support | ✓ |
| FR-11 | Validate complaint fields | `CreateComplaintRequest` validation annotations | ✓ |
| FR-12 | Categorize complaints | `Category` entity, `CategoryRepository`, admin endpoints | ✓ |
| FR-13 | Set priority | `Priority` entity with levels and SLA hours | ✓ |
| FR-14 | Assign to staff | `LifecycleService.assignComplaint()`, admin-only | ✓ |
| FR-15 | Record assignment details | `ComplaintHistory` with ASSIGNED action type | ✓ |
| FR-16 | Support reassignment | `LifecycleService.assignComplaint()` handles REASSIGNED | ✓ |
| FR-17 | View complaint status | `ComplaintController.getComplaintById()` returns status | ✓ |
| FR-18 | Maintain history | `ComplaintHistory` entity, immutable audit trail | ✓ |
| FR-19 | Update status | `LifecycleService.updateStatus()` with validation | ✓ |
| FR-20 | Support status values | `ComplaintStatus` enum (SUBMITTED, ASSIGNED, IN_PROGRESS, RESOLVED, REOPENED, CLOSED) | ✓ |
| FR-21 | Record remarks | `ComplaintHistory.remark` field, REMARK_ADDED action | ✓ |
| FR-22 | Record resolution | `LifecycleService.resolveComplaint()` | ✓ |
| FR-23 | Mark resolved | `ComplaintService.updateStatus()` sets RESOLVED | ✓ |
| FR-24 | Close complaint | `LifecycleService.closeComplaint()` sets CLOSED | ✓ |
| FR-25 | Provide feedback | `LifecycleService.addFeedback()` (rating 1-5, comment) | ✓ |
| FR-26 | Support reopen | `LifecycleService.reopenComplaint()` | ✓ |
| FR-27 | Generate events | `ComplaintEvent` with type enum | ✓ |
| FR-28 | Observer subscription | `ComplaintObserver` interface, `ComplaintEventPublisher` | ✓ |
| FR-29 | Notify on events | `InAppNotificationObserver` on CREATED, ASSIGNED, STATUS_CHANGED, etc. | ✓ |
| FR-30 | Decouple notification | `ComplaintEventPublisher` independent from `ComplaintService` | ✓ |
| FR-31 | Display notifications | `NotificationController` endpoints, notification list/read API | ✓ |
| FR-32 | Locate by ID | `ComplaintController.getComplaintByTicketId()` | ✓ |
| FR-33 | Filter complaints | `ComplaintRepository.findWithFilters()` (status, category, priority, assignee, date, search) | ✓ |
| FR-34 | View summaries | `ReportService.generateSummaryReport()` | ✓ |
| FR-35 | Consistent counts | Database queries with proper aggregation | ✓ |
| FR-36 | Manage user roles | `AdminService.updateUserRole()` | ✓ |
| FR-37 | Manage categories/priorities | `AdminService` create/update methods | ✓ |
| FR-38 | View complaint records | `AdminController` routes, role-based access | ✓ |
| FR-39 | Monitor overdue | `ComplaintRepository.findOverdueNotEscalated()`, scheduled job | ✓ |
| FR-40 | Restrict admin ops | `@PreAuthorize("hasRole('ADMIN')")` on all admin endpoints | ✓ |

## Non-Functional Requirements (NFR)

| Req | Title | Implementation | Status |
|-----|-------|----------------|--------|
| NFR-01 | Response time | API endpoints optimized, Swagger tested | ✓ |
| NFR-02 | Database indexes | Indexes on ticket_id, status, category_id, priority_id, assigned_to, created_by, due_at | ✓ |
| NFR-03 | Concurrent users | Spring Boot handles multiple connections, connection pooling configured | ✓ |
| NFR-04 | Authentication required | `@PreAuthorize` enforced on protected endpoints | ✓ |
| NFR-05 | Password hashing | `PasswordEncoder` with BCrypt | ✓ |
| NFR-06 | Authorization enforced | Role checks in `SecurityConfig`, service layer validation | ✓ |
| NFR-07 | Input validation | Jakarta Bean Validation on all DTOs | ✓ |
| NFR-08 | HTTPS in production | Nginx with Let's Encrypt, HSTS headers, SSL in docker-compose | ✓ |
| NFR-09 | Admin privileges required | `@PreAuthorize("hasRole('ADMIN')")` on admin endpoints | ✓ |
| NFR-10 | Reliable storage | MySQL with transactions, Flyway migrations | ✓ |
| NFR-11 | Transaction management | `@Transactional` on service methods modifying multiple tables | ✓ |
| NFR-12 | Error handling | `GlobalExceptionHandler` returns clean JSON, no stack traces | ✓ |
| NFR-13 | Database backups | `mysqldump` strategy documented in README production checklist | ✓ |

## Business Rules

| Rule | Implementation | Status |
|------|----------------|--------|
| Only authenticated users submit complaints | `@PreAuthorize` on POST /complaints | ✓ |
| Unique complaint identifier | UUID generated as CMP-YYYY-NNNNNN | ✓ |
| Defined lifecycle status | `ComplaintStatus` enum with allowed transitions | ✓ |
| Authorized staff update info | `@PreAuthorize("hasAnyRole('STAFF', 'ADMIN')")` | ✓ |
| Admin-only assign/reassign | `@PreAuthorize("hasRole('ADMIN')")` on assign endpoints | ✓ |
| Status changes recorded in history | `ComplaintHistory` row per state change in same transaction | ✓ |
| Resolution info before marking resolved | `ResolveComplaintRequest` requires `resolutionSummary` | ✓ |
| Closed complaints not modified | `LifecycleService` throws `ConflictException` | ✓ |
| Users view only permitted complaints | Role-based filtering in `ComplaintService` | ✓ |
| Events trigger notifications | `ComplaintEventPublisher.publish()` calls all observers | ✓ |

## Architecture

| Component | Implementation | Status |
|-----------|----------------|--------|
| Layered Architecture | Controller → Service → Repository → Entity | ✓ |
| MVC Pattern | Model (entities/DTOs), View (React), Controller (REST) | ✓ |
| Observer Pattern | `ComplaintObserver` interface, `ComplaintEventPublisher`, 3 concrete observers | ✓ |
| REST API | All endpoints under /api, JSON communication | ✓ |
| JWT Auth | Stateless, 1-hour expiry, BCrypt passwords | ✓ |
| Database | MySQL 8, Flyway migrations, Spring Data JPA | ✓ |
| Docker | Multi-stage builds, non-root users, healthchecks | ✓ |
| CI/CD | GitHub Actions for mvn verify, npm build, Docker build | ✓ |

## Coverage Summary

- **Functional Requirements:** 40/40 (100%)
- **Non-Functional Requirements:** 13/13 (100%)
- **Business Rules:** 10/10 (100%)
- **Total Coverage:** 100%

## Unmapped Items

None. All requirements from the SRS have been implemented and mapped.
