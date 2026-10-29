# FSM — Work Orders, Technical Cases & Scheduling

**Status:** Proposed design for Alpha  
**Date:** 2026-10-28  
**Stack:** Kotlin Ktor + Exposed + PostgreSQL / React + TypeScript

## 1. Current architecture

- A **Work Order (WO)** in the UI corresponds to the **FieldReports** entity/table in the backend; there is no separate WorkOrder table.
- Currently one WO can reference **one Technical Case (Ticket)** via `CaseID`, **one Contract** via `ContractID`, and a Customer via `CustomerID`.
- Completing a WO currently **does not automatically update** its related Technical Case.
- FieldReports already has nested equipment/checkforms, inventory and tools data, as well as report details and client signature.
- A WO represents a **visit/work execution**, whereas a Technical Case represents an **incident/problem**. Multiple visits may be needed to resolve a case.

## 2. Target architecture

The WO is the **single scheduling unit**. No independent Scheduling table is needed for Alpha.

| Relationship | Cardinality | Implementation |
|---|---|---|
| WO ↔ Technical Cases | N:M | New `FieldReportTechnicalCases` junction table |
| WO ↔ Technicians | N:M | New `FieldReportTechnicians` junction table |
| WO → Contract | N:1 | Retain existing `ContractID` |
| WO → Customer | N:1 | Retain existing `CustomerID` |

A WO may have **zero Technical Cases** (e.g. preventive maintenance or installation). A Technical Case may span multiple WOs (diagnosis, return visit, repair).

## 3. Database changes

### 3.1 New table: `FieldReportTechnicalCases`

| Field | Type | Notes |
|---|---|---|
| `ID` | UUID PK | Junction row ID |
| `FieldReportID` | UUID FK | References FieldReports |
| `TechnicalCaseID` | UUID FK | References Tickets; align with actual PK name |
| `ResolutionStatus` | VARCHAR nullable | Outcome of **this visit**, not necessarily overall case status |
| `ResolutionNotes` | TEXT nullable | Work performed / remaining issue |
| `DateCreated` | VARCHAR(14) | Existing `yyyyMMddHHmmss` convention, if retained |
| `IsDeleted` | BOOLEAN | Soft delete |

Ensure only one **active** link per `(FieldReportID, TechnicalCaseID)`; use a partial unique index (`WHERE IsDeleted = false`) if soft-deleted links can be recreated.

### 3.2 New table: `FieldReportTechnicians`

| Field | Type | Notes |
|---|---|---|
| `ID` | UUID PK | Assignment ID |
| `FieldReportID` | UUID FK | References FieldReports |
| `UserID` | UUID FK | References the tenant's user record, once confirmed |
| `Role` | VARCHAR | `Lead` / `Assistant` (or equivalent) |
| `AssignedAt` | VARCHAR(14) | Assignment timestamp |
| `IsDeleted` | BOOLEAN | Soft delete |

Ensure only one active assignment per `(FieldReportID, UserID)`. Optionally enforce at most one active Lead per WO. Reuse existing tenant users rather than inventing a separate Technician entity unless the current user model requires it.

### 3.3 Update `FieldReports`

Add:

- `ScheduleStart`: planned start **date and time**.
- `ScheduleEnd`: planned end **date and time**.

Keep separate from existing `StartDate` / `EndDate` (actual work/report times). Validate `ScheduleEnd > ScheduleStart`. Define a consistent timezone strategy (prefer instants/UTC in the API and conversion to the company's timezone in UI; coordinate with existing `yyyyMMddHHmmss` storage convention).

### 3.4 Existing `CaseID` migration

1. Confirm current PostgreSQL/Exposed schema and foreign keys.
2. Create junction table and backfill links from existing `FieldReports.CaseID`.
3. Update reads/writes and API DTOs to use the junction table.
4. Verify no links were lost and keep compatibility during rollout.
5. Deprecate/remove the single `CaseID` only **after** successful migration and frontend/API transition.

`ContractID` and `CustomerID` remain unchanged.

## 4. Backend (Ktor) functionality

Proposed API contracts (names to finalize against existing routes):

| Method | Endpoint | Purpose |
|---|---|---|
| GET | `/fieldreports/{id}/technicians` | List assigned technicians |
| PUT | `/fieldreports/{id}/technicians` | Replace assignments as one atomic operation |
| GET | `/fieldreports/{id}/technical-cases` | List linked Technical Cases |
| PUT | `/fieldreports/{id}/technical-cases` | Replace links as one atomic operation |
| POST | `/fieldreports/{id}/complete` | Complete WO and resolve **selected** Technical Cases |
| GET | `/scheduling/workorders?from=...&to=...` | Calendar data with optional `technicianId`, `status` filters |

**Rules:**

- WO completion must **not automatically close all linked cases**.
- User explicitly selects which linked cases were resolved; unresolved cases remain open.
- Resolve selected cases using the existing custom-status/system-category mapping, not a hardcoded display string such as `Completed`.
- Write Technical Case status changes and TicketHistory records in the **same transaction** as the completion operation.
- Validate tenant ownership, permissions, case/WO associations, and idempotency/repeated requests.
- A cancelled WO does not automatically close related cases.
- Calendar API should return event ID, report number, title, planned start/end, status, customer, and assigned technician IDs/names in a compact response.
- Scheduling overlap warnings can be introduced progressively; prevent invalid date ranges from Alpha.

## 5. React frontend changes

### 5.1 Work Order (FieldReport) form

Add sections:

- **Scheduling:** ScheduleStart, ScheduleEnd.
- **Assigned Technicians:** multi-select, optional Lead designation.
- **Related Technical Cases:** multi-select/list with status and per-visit resolution notes.
- **Completion flow:** on completing a WO, show a dialog listing linked cases with checkboxes to resolve individually.

Keep existing equipment, checkforms, inventory, tools, customer, contract and signature functionality intact.

### 5.2 New Management → Scheduling module

**Primary goal:** Show upcoming visits and technician assignments in a calendar. Data comes from Work Orders; Scheduling does not create a separate work entity.

**Alpha UI:**

- Week and Month views.
- Filters: technician, status, optionally customer.
- Event card: WO number, time, title/customer, status and technicians.
- Click event → open Work Order detail/edit view.
- Same WO may appear under multiple technicians in technician-specific views.
- Distinguish planned from completed/cancelled work.

**Later:** Day/time-grid view, drag-and-drop rescheduling, conflict detection, capacity management.

## 6. Technician Workload

Calculate scheduled workload from **assigned WOs**, not from Technical Cases.

- Planned workload per technician = sum of scheduled WO durations for WOs assigned to that technician in the selected interval.
- If two technicians attend the same four-hour WO, each is allocated four scheduled hours (unless a future assignment-level schedule overrides it).
- Utilization = scheduled hours / available working hours, if availability data exists.
- For Alpha, show hours and upcoming WOs; advanced capacity, leave and working calendars can come later.

## 7. Notifications integration

Reuse Notification System v2 to support per-WO reminders, such as **2 hours before `ScheduleStart`**. Notifications should target assigned users, respect tenant boundaries, and avoid duplicate delivery. FCM/browser push is a separate delivery concern.

## 8. Suggested implementation order

1. **Schema and migration:** two junction tables, ScheduleStart/ScheduleEnd, backfill CaseID.
2. **Backend:** repositories, use cases, DTOs, routes, tenant validation and tests.
3. **WO frontend:** schedule fields, technicians, related cases.
4. **Completion logic:** explicit selection of resolved cases and TicketHistory.
5. **Management → Scheduling:** week/month calendar, filters, event detail navigation.
6. **Workload and notifications:** scheduled-hour summaries, pre-visit reminders.

## 9. Decisions to confirm before coding

- Exact PostgreSQL column/table names and current `CaseID`/`ContractID` definitions.
- Whether technicians are represented by tenant users and what permissions/roles they have.
- Whether the Lead Technician is mandatory or optional.
- How custom Technical Case statuses map to system-level resolved/open categories.
- Canonical timezone and date serialization rules for scheduled timestamps.
- Whether scheduling conflicts should warn or block in Alpha.

---

**Core design principle:** One Work Order = one visit. Scheduling and workload are views over planned Work Orders and technician assignments. Technical Cases track issues and may be addressed across multiple visits.
