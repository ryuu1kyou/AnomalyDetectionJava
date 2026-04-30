# AnomalyDetectionJava Improvement Advice

Last updated: 2026-04-29

This note captures the current design quality of the repository and the next improvements that would most
increase maintainability and domain fidelity.

## What Is Already Good

- The repository is already organized as a modular monolith with a clear Maven module split:
  `domain-shared`, `domain`, `application-contracts`, `application`, `infrastructure`, `web`, `auth-server`,
  `db-migrator`, and `host`.
- Spring Modulith structural verification exists in
  [`backend/host/src/test/java/com/anomalydetection/host/architecture/ModularityTest.java`](backend/host/src/test/java/com/anomalydetection/host/architecture/ModularityTest.java).
- ArchUnit rules already enforce key boundaries in
  [`backend/host/src/test/java/com/anomalydetection/host/architecture/ArchitectureTest.java`](backend/host/src/test/java/com/anomalydetection/host/architecture/ArchitectureTest.java).
- The backend has a reasonable DDD base in
  [`backend/domain/src/main/java/com/anomalydetection/domain/base/AggregateRoot.java`](backend/domain/src/main/java/com/anomalydetection/domain/base/AggregateRoot.java)
  and
  [`backend/domain/src/main/java/com/anomalydetection/domain/base/FullAuditedEntity.java`](backend/domain/src/main/java/com/anomalydetection/domain/base/FullAuditedEntity.java).
- Permission checks are already present in the application layer via `@PreAuthorize`.
- The frontend has a clean top-level routing entry and a single authentication guard.

## Main Gaps

### 1. Safety traceability is still too text-heavy

The Safety model currently stores several important trace fields as free-form text columns in
[`backend/domain/src/main/java/com/anomalydetection/domain/safety/SafetyTraceRecord.java`](backend/domain/src/main/java/com/anomalydetection/domain/safety/SafetyTraceRecord.java).
That is acceptable for a prototype, but it weakens traceability, validation, and future reporting.

Recommended direction:

- Make key safety artifacts first-class concepts instead of plain strings where it matters.
- Prefer structured entities or value objects for:
  - safety goals
  - hazard analysis references
  - verification evidence
  - validation evidence
  - change requests
- Keep free-form notes only for commentary fields.

### 2. State transitions need stronger guards

State-changing methods exist, but some of them are still permissive.
Examples include transitions in:

- [`backend/domain/src/main/java/com/anomalydetection/domain/safety/SafetyTraceRecord.java`](backend/domain/src/main/java/com/anomalydetection/domain/safety/SafetyTraceRecord.java)
- [`backend/domain/src/main/java/com/anomalydetection/domain/oemtraceability/OemApproval.java`](backend/domain/src/main/java/com/anomalydetection/domain/oemtraceability/OemApproval.java)

Recommended direction:

- Validate legal transitions inside the aggregate.
- Reject invalid operations explicitly instead of relying on the application layer.
- Add tests for forbidden transitions and duplicate approvals.

### 3. Frontend authorization is authentication-only

The frontend currently redirects unauthenticated users, but it does not yet enforce page-level permission
visibility.

Relevant files:

- [`frontend/src/app/components/AuthGuard.tsx`](frontend/src/app/components/AuthGuard.tsx)
- [`frontend/src/app/routes/router.tsx`](frontend/src/app/routes/router.tsx)

Recommended direction:

- Add permission-aware route guards or menu filtering.
- Hide admin pages from users who do not have the required authorities.
- Keep the UI aligned with backend permission names.

### 4. Safety and OEM traceability should be connected explicitly

Safety and OEM traceability are both present, but the relationship between them is still implicit.

Recommended direction:

- Define how a safety record links to an OEM approval or OEM customization.
- Make the trace graph navigable in both directions.
- Ensure audit/export views can show the full chain:
  requirement -> hazard -> safety goal -> implementation -> verification -> approval.

### 5. DDD boundaries are good, but could be made stricter

The current boundary tests are useful, but the model can still be tightened.

Recommended direction:

- Keep DTOs only in `application-contracts`.
- Keep aggregates and entities in `domain`.
- Keep persistence logic in `infrastructure`.
- Avoid letting application services become an unstructured CRUD layer.
- Prefer records for DTOs and value objects, but not for JPA aggregates.

## Priority Improvements

### P0

1. Add structured traceability for Safety artifacts.
2. Enforce stricter aggregate state transitions.
3. Introduce frontend permission-based visibility.

### P1

1. Add tests for invalid approval/rejection/cancel flows.
2. Add explicit linkage between Safety and OEM traceability.
3. Strengthen the domain model for ASIL-related concepts.

### P2

1. Add UI affordances for traceability navigation.
2. Add exportable evidence views for safety reviews.
3. Add documentation for how requirements map to database tables and DTOs.

## Suggested Next Implementation Steps

If the next work item is to improve the repository in a practical way, the best sequence is:

1. Harden the Safety aggregate and introduce stricter transition rules.
2. Add traceability entities or value objects for evidence and references.
3. Add permission-aware frontend guards.
4. Add focused tests for the new rules.

## Notes

- This repository already has a solid modular foundation, so the next gains come from modeling quality rather than
  more layering.
- The most valuable improvement is making safety traceability auditable and navigable instead of storing it mainly
  as long text fields.
