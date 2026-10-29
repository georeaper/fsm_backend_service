# Dashboard API contract

All routes use `auth-jwt-v2`. The tenant is resolved from the JWT `tenant_id` through the central authentication database; callers cannot provide a database, company, or tenant identifier. Existing domain rules allow every authenticated tenant role to read field reports, tickets, and tasks, so the dashboard follows the same policy. Task completion follows the existing task write policy and is available to every authenticated tenant role.

## Domain mapping

- Work orders: `fieldreports`. `ReportStatus` is the database source of truth and is also exposed as the backward-compatible API field `Status`. It maps case-insensitively to `pending`, `inProgress`, `completed`, or `onHold`; unknown/blank values map to `pending`. Work-order `Priority` and `Type` are stored separately; `Type` corresponds to the tenant's `WorkOrderType` settings.
- Technical cases: `ticket`. `Status` maps explicitly to `open`, `inProgress`, `resolved`, or `onHold`. For migrated legacy rows, `Active=false` falls back to `resolved` and other values to `open`.
- Priority: `Urgency`/`Priority` values `urgent`, `critical`, and `high` map to `high`; `low` maps to `low`; other values map to `medium`.
- Contracts use string `ContractStatus`. Dashboard-active values are `ACTIVE`, `IN_PROGRESS`, `VALID`, and `OPEN` (case/separator insensitive).
- Dates are read in the repository storage format `yyyyMMddHHmmss` (ISO and legacy `dd/MM/yyyy` are accepted defensively) and returned as ISO-8601 dates. Completion timestamps are UTC instants.
- A work order's `EndDate` is treated as its due/completion date because the current schema has only one end-date field.
- A technical case's `DateEnd` is treated as its due date while it remains active.
- The week is Monday through Sunday, inclusive, in UTC. The application uses an injectable `Clock`.

## Routes

- `GET /dashboard/summary`
- `GET /dashboard/work-orders?period=year&year=2026` (`period` defaults to `year`; year range 2000–2100)
- `GET /dashboard/technical-cases?recentLimit=5` (range 1–50)
- `GET /dashboard/tasks/upcoming?limit=5` (range 1–50)
- `PATCH /tasks/{taskId}/complete` (UUID, no body, idempotent; returns `200`)
- `GET /dashboard/due-dates`
- `GET /dashboard/top-customers?period=month&limit=5` (`period` defaults to `month`; range 1–50)
- `GET /dashboard/knowledge-base?period=last6Months&customerId={uuid}&customerId={uuid}&limit=5`

## Knowledge-base overview

`GET /dashboard/knowledge-base` powers the four cards at the top of the equipment
knowledge-base screen. It derives all incident counts from non-deleted Technical
Cases (`ticket`): equipment uses the related equipment's `Model`, manufacturer
uses its `Manufacturer`, incident type uses Technical Case `Type`, and incident
status uses Technical Case `Status` with `TechnicalCaseStatus` setting metadata.

`period` defaults to `last6Months` and accepts `last30Days`, `last3Months`,
`last6Months`, or `last12Months`. The inclusive range ends on the current UTC
date. The response also contains the immediately preceding equal-length range
used to calculate each leading value's `changePercentage`.

Customer filtering is optional. Repeat `customerId` for multi-select filtering;
comma-separated values are also accepted. No `customerId` means all customers.
`limit` defaults to 5 and accepts 1–20.

The response contains `range`, `previousRange`, echoed `filters`, and the four
metrics `mostFrequentEquipment`, `mostFrequentManufacturer`, `topIncidentTypes`,
and `incidentStatus`. Ranking metrics expose `totalIncidents`, `leadingValue`,
`leadingCount`, `changePercentage`, and `items`. Status additionally exposes
`leadingPercentage`; every status item contains `settingId`, `value`,
`systemCategory`, `style`, `count`, and `percentage`.

`filters.availableCustomers` contains distinct `{customerId, customerName}`
options that have incidents in the selected period before customer filtering, so
the frontend can render the multi-select without calling an owner-only endpoint.

Rows without a model, manufacturer, or type are excluded only from that ranking
list; they remain included in `totalIncidents` and the status breakdown. Invalid
input returns `400 INVALID_PERIOD` or `400 INVALID_LIMIT`.

## Work-order dashboard contract

`GET /dashboard/summary` returns the work-order section using canonical status categories:

```json
{
  "workOrders": {
    "total": 27,
    "open": 20,
    "overdue": 7,
    "statuses": [
      {
        "settingId": "setting-uuid",
        "value": "Pending Approval",
        "systemCategory": "OPEN",
        "style": "#3b82f6",
        "count": 12
      }
    ],
    "changePercentage": 12
  }
}
```

`GET /dashboard/work-orders?period=year&year=2026` returns `reportsByType`, the backward-compatible `createdVsCompleted`, and the same shape under `statusDistribution`.

```json
{
  "reportsByType": {
    "types": [
      { "settingId": "uuid-1", "value": "Maintenance", "style": "#2563EB" },
      { "settingId": "uuid-2", "value": "Installation", "style": "#F97316" }
    ],
    "periods": [
      {
        "period": "2026-01",
        "label": "Jan",
        "counts": { "uuid-1": 4, "uuid-2": 2 }
      }
    ]
  },
  "createdVsCompleted": [],
  "statusDistribution": { "total": 6, "open": 4, "overdue": 1, "statuses": [] }
}
```

Work-order type counts are grouped in PostgreSQL by month and `fieldreports.Type`. The service maps the stored type case-insensitively by either `SettingsId` or `SettingsValue` to support both existing client payload forms, and exposes the stable `SettingsId`. All active types are returned with zero-filled months so the chart legend remains stable. A soft-deleted type is returned only while it still has reports in the selected year. Unknown legacy type values are omitted because they have no stable setting ID.

`open` is the sum of every custom status categorized as `OPEN`. `overdue` is the subset of those records whose non-null `EndDate` is earlier than the current UTC date, so it must not be added to the status counts. Active status settings are returned even when their count is zero. Soft-deleted status settings are returned only when historical work orders still use their value. Unknown legacy values are returned with null `settingId`, `systemCategory`, and `style` and are excluded from `open` and `overdue`.

## Technical-case dashboard contract

The `technicalCases` section of `GET /dashboard/summary` contains `total`, `open`, `overdue`, `statuses`, and `changePercentage`, using the same `DashboardStatusItem` structure as work orders. `GET /dashboard/technical-cases?recentLimit=5` returns:

```json
{
  "statusDistribution": {
    "total": 34,
    "open": 26,
    "overdue": 8,
    "statuses": [
      {
        "settingId": "setting-uuid",
        "value": "Technician Assigned",
        "systemCategory": "OPEN",
        "style": "#f97316",
        "count": 16
      }
    ]
  },
  "recent": [
    {
      "technicalCaseId": "technical-case-uuid",
      "caseNumber": "TC-1024",
      "title": "Air Conditioner Maintenance",
      "customerId": "customer-uuid",
      "customerName": "Acme Ltd",
      "status": "Technician Assigned",
      "systemCategory": "OPEN",
      "statusStyle": "#f97316",
      "type": "Maintenance",
      "dueDate": "2026-10-30",
      "priority": "high"
    }
  ]
}
```

Technical-case `open` and `overdue` use `TechnicalCaseStatus.SystemCategory`. For legacy rows without a matching status setting, `Active=true/null` falls back to `OPEN` and `Active=false` falls back to `CLOSED`. Soft-deleted settings remain available only for historical classification.

Errors introduced by these routes use `{ "code": "...", "message": "..." }`. Missing records return `404`, invalid parameters/transitions return `400`, and missing or stale authentication returns `401`.

`changePercentage` compares records created in the current UTC calendar month with the immediately preceding month. If both counts are zero the result is `0`; if the previous count is zero and the current count is positive the result is `100`; otherwise the ordinary percentage change is rounded to the nearest integer.

No migration or index was added. The feature uses projections over existing tenant tables. For large tenant datasets, expression indexes over the string date/status fields should be considered together with a future typed-date migration.

## Technician workload API

- `GET /dashboard/technician-workload/summary?from=2026-10-19&to=2026-10-25`
- `GET /dashboard/technician-workload/charts?from=2026-10-19&to=2026-10-25`
- `GET /dashboard/technician-workload/technicians?from=2026-10-19&to=2026-10-25`
- `GET /dashboard/technician-workload/upcoming`
- `GET /dashboard/technician-workload/upcoming?type=WORK_ORDER`

Both endpoints require inclusive ISO dates (`yyyy-MM-dd`). `from` must not be
after `to`, and the selected range may contain at most 366 days. UI presets are
resolved by the frontend. The backend calculates the immediately preceding
equal-length range internally for percentage comparisons.

Summary response:

```json
{
  "range": { "from": "2026-10-19", "to": "2026-10-25" },
  "activeAssignments": { "total": 42, "previousTotal": 37, "changePercentage": 14, "technicalCases": 18, "workOrders": 14, "tasks": 10 },
  "overdueAssignments": { "total": 8, "previousTotal": 6, "changePercentage": 33, "technicalCases": 3, "workOrders": 4, "tasks": 1 },
  "highPriorityAssignments": { "total": 12, "previousTotal": 10, "changePercentage": 20, "technicalCases": 5, "workOrders": 5, "tasks": 2 },
  "technicians": { "total": 5, "previousTotal": 4, "changePercentage": 25 }
}
```

Charts response:

```json
{
  "range": { "from": "2026-10-19", "to": "2026-10-25" },
  "workloadByTechnician": [
    { "userId": "user-uuid", "name": "John Smith", "technicalCases": 7, "workOrders": 8, "tasks": 3, "total": 18, "scheduledHours": 24.5 }
  ],
  "assignmentsByType": { "total": 42, "technicalCases": 18, "workOrders": 14, "tasks": 10 }
}
```

An assignment belongs to the range when its start/due interval overlaps the
inclusive range. With only one date, that date must be inside the range. Undated
records are excluded. Active assignments use status `SystemCategory=OPEN`; high
priority uses priority `SystemCategory=HIGH`; overdue means active with a due date
earlier than the current UTC date. The technician metric counts distinct assigned
users among active assignments in the range. Invalid input returns
`400 INVALID_DATE_RANGE`.

Stored assignment dates accept the canonical storage/ISO formats and the legacy
task payload format `dd/MM/yyyy HH:mm:ss` (for example `27/10/2026 06:00:00`).

The technicians endpoint returns the table breakdown for the selected inclusive
range. Each row contains `userId`, `name`, `total`, `technicalCases`, `workOrders`,
`tasks`, `overdue`, and `highPriority`, ordered by total descending.

The upcoming endpoint returns at most the next 50 active assignments, ordered by
their next start time. `type` is optional and accepts `TECHNICAL_CASE`,
`WORK_ORDER`, or `TASK`. Work orders assigned to multiple technicians are returned
once with all assignees in `technicians`; past, closed, cancelled, and undated
assignments are excluded. An assignment that has already started remains upcoming
while its due/end timestamp is in the present or future. There is intentionally no
pagination or View-all mode.
