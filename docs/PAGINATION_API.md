# List API Pagination

Entity list endpoints accept optional `page` and `pageSize` query parameters:

```http
GET /fieldreports?page=1&pageSize=50
GET /tickets?page=2&pageSize=25
GET /customers?page=1&pageSize=100
```

- `page` defaults to `1` and must be at least `1`.
- `pageSize` defaults to `50` and must be between `1` and `200`.
- Responses remain JSON arrays for backward compatibility.
- Pagination metadata is returned in response headers:

```text
X-Total-Count
X-Page
X-Page-Size
X-Total-Pages
```

Pagination applies to:

- `/fieldreports`
- `/tickets`
- `/tasks`
- `/customers`
- `/contracts`
- `/equipments`
- `/inventory`
- `/maintenances`
- `/manufacturer`
- `/model`
- `/tools`
- `/users`
- `/scheduling/workorders`

Settings/category endpoints remain unpaged because they are complete lookup collections used
by selectors and status/type resolution.

The Scheduling endpoint additionally rejects date ranges longer than 366 days. Its existing
`from`, `to`, `technicianId`, and `status` filters are applied before pagination.
