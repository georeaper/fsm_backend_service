# FSM — Contract Maintenance Planning

**Status:** Architecture proposal / agreed functional direction  
**Date:** 2026-10-28  
**Scope:** Contracts, ContractEquipments, maintenance obligations, Work Orders (FieldReports), Scheduling

## 1. Goal

A contract can cover multiple equipment assets, each requiring a different number of maintenance visits during the contract period (e.g. Pump A: 2, Compressor B: 3, Generator C: 4). The application must show future maintenance obligations even before a Work Order is created. Users can select multiple nearby-dated obligations with checkboxes and create **one shared Work Order** for the selected equipment.

**Agreed design: Option B.** Generate maintenance obligations first, not automatic Work Orders. Generate a Work Order only when a user groups/selects obligations and schedules an actual visit.

## 2. Key domain distinction

- **Contract:** Commercial agreement and its validity period.
- **ContractEquipment:** Equipment covered by a contract, with its own visit count and scheduling rules.
- **ContractMaintenanceVisit:** A maintenance obligation for one contract equipment item. It can be pending without an assigned technician or Work Order.
- **FieldReport / Work Order:** One actual or scheduled on-site visit, possibly covering multiple equipment items and obligations. `FieldReports` is the backend table; Work Order is its UI name.
- **Scheduling:** Management UI/API that aggregates planned Work Orders and unscheduled maintenance obligations; it is **not** a separate database table in the Alpha design.

## 3. Entity relationships

```text
Contracts (1) ─── (N) ContractEquipments (1) ─── (N) ContractMaintenanceVisits
     │                                                    │
     │                                                    │ (N:M)
     │                                          FieldReportMaintenanceVisits
     │                                                    │
     └── existing ContractID on FieldReports ─── FieldReports / Work Orders
                                                        │
                                                        └── FieldReportTechnicians (N:M users)
```

One Work Order may cover several maintenance obligations; an obligation may have multiple Work Order attempts, with explicit execution status/history. For Alpha, only obligations belonging to the **same customer AND same contract** can be grouped into one Work Order. This preserves the existing single `ContractID` on `FieldReports`. Multi-contract grouping is deferred.

## 4. Database changes

### 4.1 Existing `Contracts`

Reuse the contract ID, customer relationship, start/end dates, and existing soft-delete behavior. No new total-visits field is necessary; totals are derived from equipment/obligations.

### 4.2 Existing `ContractEquipments` — proposed additions

| Field | Type | Purpose |
|---|---|---|
| `VisitsRequired` | Integer | Required maintenance occurrences for this equipment over the contract term |
| `DistributionMode` | Enum/string | `AUTO`, `MANUAL`, or `HYBRID` |

Retain existing `ContractID`, `EquipmentID`, PK, and soft-delete fields. **Verify the current Exposed schema and naming before making a migration**; these are conceptual names.

### 4.3 New `ContractMaintenanceVisits`

| Field | Type | Purpose |
|---|---|---|
| `VisitID` | UUID PK | Obligation ID |
| `ContractEquipmentID` | UUID FK | Covered asset in contract |
| `VisitNumber` | Integer | Sequence number within this contract equipment |
| `PlannedDate` | Date | Proposed/adjusted target date |
| `DueDate` | Date | Contractual deadline, if applicable |
| `Status` | Enum/string | `PENDING`, `SCHEDULED`, `COMPLETED`, `CANCELLED` (or derived) |
| `DateCreated` | Timestamp/string | Audit |
| `LastModified` | Timestamp/string | Audit |
| `IsDeleted` | Boolean | Soft delete |

`PlannedDate` is a suggested target, not an assigned appointment time. Consider storing obligation status as a derived projection from links, or maintain it transactionally to avoid contradictions. Enforce uniqueness for active `(ContractEquipmentID, VisitNumber)` records.

### 4.4 New `FieldReportMaintenanceVisits`

| Field | Type | Purpose |
|---|---|---|
| `ID` | UUID PK | Link ID |
| `FieldReportID` | UUID FK | Actual/scheduled Work Order |
| `VisitID` | UUID FK | Maintenance obligation |
| `ExecutionStatus` | Enum/string | `PLANNED`, `COMPLETED`, `NOT_COMPLETED` |
| `CompletedAt` | Timestamp nullable | Actual fulfillment time |
| `IsDeleted` | Boolean | Soft delete |

Enforce unique active `(FieldReportID, VisitID)`. A single obligation can link to several WOs over time if the first attempt does not fulfill it; prevent simultaneous conflicting active bookings unless explicitly allowed.

### 4.5 Existing `FieldReports`

Use the existing `ContractID` and `CustomerID`. The broader Work Order/Scheduling feature adds `ScheduleStart` and `ScheduleEnd` (including time) and a separate `FieldReportTechnicians` relation. Keep actual execution `StartDate`/`EndDate` distinct from planned appointment dates.

## 5. Maintenance plan generation

Supported modes:

- **AUTO:** User specifies visits required; system proposes evenly distributed target dates within the contract term.
- **MANUAL:** User enters each visit date.
- **HYBRID (selected):** System proposes dates, then user may edit individual dates before saving.

Rules:

1. Validate positive visit count and valid contract date range.
2. Generate a **preview** before persisting.
3. Keep dates inside the contract term unless a documented exception is permitted.
4. Validate ordering and duplicates; handle short contract terms and high visit counts.
5. Use calendar-date semantics for obligations, with an explicit business timezone when converting to timestamps.
6. Do not create Work Orders as a side effect of plan generation.
7. On contract changes, preserve completed obligations and links to WOs. Show a diff for new, removed, or rescheduled pending visits; do not silently delete history.

**Example:** Contract 01/01/2027–31/12/2027. Pump A requires 2 obligations; Compressor B 3; Generator C 4. The system creates **9 maintenance obligations**, not necessarily 9 Work Orders.

## 6. Frontend workflow

### 6.1 Contract editor

Within the Contract detail, show a `Contract Equipments / Maintenance Plan` section:

- Equipment selector and `VisitsRequired` per equipment.
- Distribution mode AUTO/MANUAL/HYBRID.
- Preview/edit visit dates and save plan.
- Per-equipment progress: required, completed, remaining, next due.

### 6.2 Management → Scheduling → Upcoming Maintenance

Show maintenance obligations (including those with **no WO**) with filters such as next 1/3/6 months, customer, contract, equipment, due date, and status. Display customer, contract, equipment, target/due date, and assignment state.

User selects obligations with **checkboxes** and clicks **Create Work Order**.

Selection rules for Alpha:

- Same `CustomerID` and same `ContractID` for every selected obligation.
- Dates should be close enough for a practical shared visit; initially show a warning or configurable tolerance, not an undocumented hard-coded constraint.
- Reject cancelled/completed obligations and incompatible already-scheduled obligations.
- Allow selecting multiple equipment items from the same contract.

### 6.3 Create Work Order dialog

Prepopulate customer, contract, selected equipment, and maintenance-obligation links. User chooses planned start/end and assigned technician(s). After confirmation, one `FieldReports` record is created, plus its maintenance links and technician assignments. It appears in the Scheduling Calendar and Work Order list.

### 6.4 Scheduling Calendar

Display:

- **Scheduled Work Orders:** actual technician appointments, using `ScheduleStart`/`ScheduleEnd`.
- **Unscheduled maintenance obligations:** separate visual category, based on `PlannedDate`/`DueDate`, not represented as assigned WOs.

Include date-range and technician filters. The Management planner can see unassigned obligations; visibility for individual technicians must follow role/permission rules.

## 7. Backend API proposal (Ktor)

These are proposed contracts, **not existing endpoints**.

| Method | Endpoint | Purpose |
|---|---|---|
| GET | `/contracts/{id}/maintenance-plan` | Equipment and obligations for contract |
| POST | `/contracts/{id}/maintenance-plan/preview` | Preview generated visits without saving |
| PUT | `/contracts/{id}/maintenance-plan` | Save/reconcile per-equipment maintenance plan |
| GET | `/maintenance-visits` | Filter obligations by date/customer/contract/status |
| POST | `/scheduling/workorders/from-maintenance` | Create one WO from selected obligations |
| GET | `/scheduling/workorders` | Scheduled WOs for calendar range |
| POST | `/fieldreports/{id}/complete` | Complete WO and explicitly fulfilled obligations |

### Example: create a shared Work Order

```json
{
  "maintenanceVisitIds": [
    "uuid-mv-001",
    "uuid-mv-002",
    "uuid-mv-003"
  ],
  "scheduleStart": "20270315090000",
  "scheduleEnd": "20270315120000",
  "technicianIds": [
    "uuid-technician-1",
    "uuid-technician-2"
  ]
}
```

The date-time string above follows the app's current `yyyyMMddHHmmss` convention. Define a clear timezone policy for scheduled appointments; date-only obligations need not use the same storage representation.

Backend validations: authenticated tenant context; same tenant, customer, and contract; selected obligations exist and are eligible; technician IDs belong to the tenant; schedule start precedes end; no invalid duplicates. Create `FieldReports`, `FieldReportMaintenanceVisits`, technician links, and equipment links **in one transaction**. Handle concurrent requests to avoid two active WOs being created from the same obligation unintentionally.

## 8. Execution and completion

A Work Order can be completed while some selected maintenance obligations remain incomplete. At WO completion, the user explicitly marks which obligations were fulfilled. Record the result on `FieldReportMaintenanceVisits` and update/derive the obligation status. Preserve prior failed/incomplete visits for audit. This is separate from Technical Case resolution and should not automatically close unrelated tickets.

## 9. Notifications and workload

- **Notifications v2:** Alert on approaching/overdue maintenance deadlines and upcoming assigned Work Orders, without duplicating reminders.
- **Technician Workload:** Calculate booked technician hours from scheduled Work Orders only, **not** from unassigned maintenance obligations.
- **Planning backlog:** Count due/overdue obligations without an assigned Work Order separately from technician workload.

## 10. Implementation order

1. Inspect current `Contracts`, `ContractEquipments`, `FieldReports`, and equipment FK schemas.
2. Add visit count/distribution mode to `ContractEquipments` where absent.
3. Create `ContractMaintenanceVisits` and `FieldReportMaintenanceVisits` with indexes, FKs, and soft delete.
4. Implement preview/generation/reconciliation logic in Ktor.
5. Implement Contract Maintenance Plan editor in React.
6. Implement Upcoming Maintenance list with checkbox multi-select and same-customer/same-contract validation.
7. Implement atomic `Create Work Order from Maintenance` flow.
8. Integrate Calendar, completion handling, Notifications v2, and workload.
9. Test contract edits, repeat visits, partial completion, concurrent creation, and tenant isolation.

## 11. Decisions and deferred scope

**Agreed:** Option B (obligations first), HYBRID date generation, checkbox selection, one shared WO for compatible equipment obligations, Scheduling as UI/API rather than its own DB table, and one WO per actual visit.

**Alpha recommendation:** Shared WO only within one customer **and one contract**, because `FieldReports` currently has a single `ContractID`.

**Later:** Cross-contract grouping for one customer; automatic grouping of nearby obligations; drag-and-drop scheduling; sophisticated technician capacity optimization; recurrence policy beyond a single contract term.

**To verify before implementation:** Exact existing Exposed columns; tenant user/technician relationship; status/category mapping; whether `FieldReportEquipments` should be populated automatically for all selected maintenance assets; date/timezone strategy; how the contract renewal workflow creates the next plan.
