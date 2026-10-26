# Notification V2 Alpha API

All endpoints require `auth-jwt-v2`. Rule and job administration requires the `owner` or `admin` role. User notification and device operations always resolve the recipient from the JWT; callers cannot read another user's notifications.

## Metadata and rules

- `GET /api/v2/notification-metadata`
- `GET /api/v2/notification-rules`
- `POST /api/v2/notification-rules`
- `PUT /api/v2/notification-rules/{id}`
- `PATCH /api/v2/notification-rules/{id}/enabled`
- `DELETE /api/v2/notification-rules/{id}` (disables the rule and cancels pending jobs)

Example date rule:

```json
{
  "name": "Two hours before scheduled start",
  "entityType": "WORK_ORDER",
  "triggerType": "DATE",
  "dateField": "SCHEDULED_START",
  "offsetValue": 2,
  "offsetUnit": "HOURS",
  "offsetDirection": "BEFORE",
  "conditions": [{ "field": "STATUS", "operator": "NEQ", "value": "COMPLETED" }],
  "recipientType": "ASSIGNED_USER",
  "titleTemplate": "Upcoming work order",
  "messageTemplate": "{{entityTitle}} starts soon",
  "isEnabled": true
}
```

## User notifications and devices

- `GET /api/v2/notifications?isRead=false&limit=50`
- `GET /api/v2/notifications/unread-count`
- `PATCH /api/v2/notifications/{id}/read`
- `POST /api/v2/notification-devices`
- `DELETE /api/v2/notification-devices/{id}`

## Administrative jobs

- `GET /api/v2/notification-jobs?status=FAILED&limit=100`
- `POST /api/v2/notification-jobs/{id}/retry`

The Alpha release stores in-app notifications and device registrations. FCM delivery is intentionally deferred; it can consume the device table without coupling Firebase to business use cases.
