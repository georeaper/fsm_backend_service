# Field report datetime: temporary contract and follow-up work

Decision recorded on 2026-10-26.

## Temporary implementation

`StartDate` and `EndDate` are sent by the frontend as ISO 8601 datetime strings and
continue to be stored in the existing `VARCHAR` columns without conversion.

Canonical payload format:

```json
{
  "StartDate": "2026-10-26T08:30:00.000Z",
  "EndDate": "2026-10-26T10:45:00.000Z"
}
```

The frontend should always send UTC values with the `Z` suffix (for example, the
result of JavaScript `Date.prototype.toISOString()`). It must not mix date-only,
localized, offset-free, or differently formatted values in these fields.

For now, the API DTOs remain `String?` and the database columns remain `VARCHAR`.
The current `/fieldreports` create/update repository already persists these values
as received.

## Required follow-up corrections

- Change the API DTO fields from `String?` to a typed time representation such as
  `Instant?` (or explicitly parse ISO 8601 at the API boundary).
- Return HTTP 400 with a field-specific message for malformed values.
- Validate that `EndDate` is not earlier than `StartDate`.
- Decide and document whether null, equal start/end values, and future timestamps
  are allowed.
- Migrate `StartDate` and `EndDate` from `VARCHAR` to the database's timestamp type.
- Before migration, audit and normalize legacy values such as `dd/MM/yyyy`,
  `yyyy-MM-dd`, and `yyyyMMddHHmmss`.
- Keep all stored instants in UTC and convert to `Europe/Athens` only for display.
- Update dashboard queries, sorting, notification parsing, PDF formatting, API
  examples, tests, and indexes to use the typed columns.
- Remove or refactor the legacy `DateUtils.uiToStorage` flow, which accepts
  `dd/MM/yyyy`, forces midnight, and produces `yyyyMMddHHmmss`.
- Apply the same decision separately to `ScheduledStart` and `ScheduledEnd`; do not
  assume they automatically share the semantics of actual start/end times.

## Temporary limitation

Because the database values are strings, chronological sorting and comparisons are
reliable only while every newly stored value uses the same UTC ISO 8601 format.
Legacy values must be handled explicitly until the database migration is complete.
