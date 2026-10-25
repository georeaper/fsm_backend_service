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

`GET /dashboard/work-orders?period=year&year=2026` uses the same shape under `statusDistribution`, together with `createdVsCompleted`. `open` is the sum of every custom status categorized as `OPEN`. `overdue` is the subset of those records whose non-null `EndDate` is earlier than the current UTC date, so it must not be added to the status counts. Active settings are returned even when their count is zero. Soft-deleted settings are returned only when historical work orders still use their value. Unknown legacy values are returned with null `settingId`, `systemCategory`, and `style` and are excluded from `open` and `overdue`.

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
