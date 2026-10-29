# Custom settings API

Each setting keeps the tenant-defined display value in `SettingsValue`. Status settings require `SystemCategory` with `OPEN`, `CLOSED`, or `CANCELED`. Priority settings require `SystemCategory` with `LOW`, `MEDIUM`, or `HIGH`. This lets users rename values without changing backend filtering or dashboard semantics.

Example:

```json
{
  "SettingsValue": "Σε εξέλιξη",
  "SettingsStyle": "blue",
  "SettingsDescription": "Εργασία που βρίσκεται σε εξέλιξη",
  "SystemCategory": "OPEN"
}
```

The following authenticated GET/POST pairs are available:

- `/settings/technical-case-statuses`
- `/settings/technical-case-types`
- `/settings/work-order-statuses`
- `/settings/work-order-priorities`
- `/settings/contract-statuses`
- `/settings/task-statuses`
- `/settings/task-priorities`
- `/settings/task-types`

Existing endpoints remain available for `TechnicalCasePriority`, `WorkOrderType`, and `ContractType`. `SystemCategory` is required for status and priority categories and ignored for all other categories. Deletion continues through `DELETE /settings/delete/{id}` and is a soft delete (`IsDeleted=true`). Deleted settings are hidden from all settings selection endpoints, but remain available internally so historical records retain their category and dashboard semantics.

Priority creation example:

```json
{
  "SettingsValue": "Urgent",
  "SettingsStyle": "#EF4444",
  "SettingsDescription": "Requires immediate attention",
  "SystemCategory": "HIGH"
}
```

To change only the category of an existing status or priority setting:

```http
PATCH /settings/{settingId}/system-category
```

```json
{ "systemCategory": "HIGH" }
```

## Update only a setting color

`PATCH /settings/{settingId}/style` changes only `SettingsStyle` and `LastModified`:

```json
{ "style": "#2563EB" }
```

Successful response:

```json
{ "settingId": "setting-uuid", "style": "#2563EB" }
```

Accepted formats are `#RGB`, `#RRGGBB`, and `#RRGGBBAA`. Invalid input returns
`400 INVALID_STYLE`; a missing or soft-deleted setting returns `404 SETTING_NOT_FOUND`.

For task completion, the backend uses the first deterministic `TaskStatus` value categorized as `CLOSED`. If none exists, it stores `CLOSED` as a safe fallback.
