# Custom settings API

Each setting keeps the tenant-defined display value in `SettingsValue`. Status settings also require `SystemCategory`, whose allowed values are `OPEN`, `CLOSED`, and `CANCELED`. This lets users rename statuses without changing backend filtering, completion, or dashboard semantics.

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

Existing endpoints remain available for `TechnicalCasePriority`, `WorkOrderType`, and `ContractType`. `SystemCategory` is ignored for non-status categories. Deletion continues through `DELETE /settings/delete/{id}` and is a soft delete (`IsDeleted=true`). Deleted settings are hidden from all settings selection endpoints, but remain available internally so historical records retain their status category and dashboard metrics.

For task completion, the backend uses the first deterministic `TaskStatus` value categorized as `CLOSED`. If none exists, it stores `CLOSED` as a safe fallback.
