# Equipment Knowledge Base API

All endpoints require `auth-jwt-v2` and use the tenant database resolved from the
JWT. The existing CRUD `/equipments` contract is unchanged.

## Paginated list

```http
GET /equipment-knowledge-base/equipment?page=1&pageSize=10
    &search=pump
    &manufacturer=Grundfos
    &customerId=customer-1&customerId=customer-2
    &status=ACTIVE
    &warrantyStatus=ACTIVE
    &openTechnicalCases=WITH_OPEN
```

Filters are applied to the complete tenant result before pagination. Consequently
`X-Total-Count` and `X-Total-Pages` describe the filtered result, not all
equipment. The frontend must reset `page` to 1 whenever a filter changes.

- `page` defaults to 1; `pageSize` defaults to 50 and accepts 1–200.
- `search` matches code/id, name, manufacturer, model, or customer name.
- `manufacturer` is case-insensitive.
- Repeat `customerId` (or send comma-separated IDs) for multi-select filtering.
- `status`: `ACTIVE` or `INACTIVE`.
- `warrantyStatus`: `ACTIVE`, `EXPIRED`, or `UNKNOWN`.
- `openTechnicalCases`: `ANY`, `WITH_OPEN`, or `WITHOUT_OPEN`.

The response remains an array. Pagination metadata is returned in
`X-Total-Count`, `X-Page`, `X-Page-Size`, and `X-Total-Pages` headers.

Each item contains `equipmentId`, `code`, `serialNumber`, `name`, `manufacturer`, `model`,
customer fields, `lastServiceDate`, `openTechnicalCases`, raw `warranty`, derived
`warrantyStatus`, and equipment `status`.

## Filter options

```http
GET /equipment-knowledge-base/filters
```

Returns distinct manufacturers and `{customerId, customerName}` options, together
with the accepted status, warranty, and open-Technical-Case values.

## Drawer detail

```http
GET /equipment-knowledge-base/equipment/{equipmentId}
```

Call this endpoint only after a row is selected. The response contains:

- `overview`: equipment/customer information, last service, next scheduled
  service, warranty state, status, and open Technical Case count.
- `recentActivity`: at most 20 combined service and Technical Case events,
  newest first.
- `serviceHistory`: linked field reports/work orders, newest first.
- `technicalCases`: Technical Cases linked through `ticket.EquipmentId`.
- `maintenanceSchedule`: linked field-report maintenance rows with a scheduled
  start date, ordered chronologically.

Unknown equipment returns `404 EQUIPMENT_NOT_FOUND`.

## Current schema limitations

The database has one free-text `equipment.Warranty` field, not separate warranty
start/end dates. The API preserves that raw value and derives a status only when
the value is a recognized state or parseable date; otherwise it returns
`UNKNOWN`. Equipment code/tag and image URL also do not exist as separate schema
fields, so `code` uses the existing `SerialNumber` value (and is null when no
serial number is stored). No fabricated image/tag is returned.
