# User API (alpha)

All endpoints require `Authorization: Bearer <access-token>` and use camelCase JSON.
The tenant/company is always resolved from the token; clients must not send `companyId`,
`userId`, `authUserId` or `remoteId` as writable fields.

## Admin endpoints

`GET /users` requires `owner` or `admin` and returns the combined central account and tenant profile.

`POST /users` requires `owner` or `admin`:

```json
{
  "username": "giorgos",
  "email": "giorgos@example.com",
  "password": "secure-password",
  "role": "user",
  "name": "Γιώργος Παπαδόπουλος",
  "description": "Τεχνικός",
  "phone": "6900000000",
  "reportPrefix": "GP",
  "technicalCasePrefix": "TC-GP"
}
```

It creates the authentication account in the central database and the linked profile in the tenant database.
Subscription capacity is checked atomically before the central account is created.

`PUT /users/{tenantUserId}` requires `owner` or `admin` and replaces all editable account/profile fields.
The `password` property is optional; when omitted, the existing password remains unchanged.

`PATCH /users/{tenantUserId}/activate` and `PATCH /users/{tenantUserId}/deactivate` require `owner` or `admin`.
Activation consumes a subscription seat. Deactivation releases it and revokes the user's sessions.
Permanent deletion is deliberately not exposed in the alpha API.

## Self-service endpoints

`PUT /users/me` updates profile-only fields:

```json
{
  "name": "Γιώργος Παπαδόπουλος",
  "description": "Τεχνικός",
  "phone": "6900000000",
  "reportPrefix": "GP",
  "technicalCasePrefix": "TC-GP"
}
```

`PUT /users/me/password` changes the password after verifying the current password:

```json
{
  "currentPassword": "old-password",
  "newPassword": "new-secure-password"
}
```

A successful password change revokes existing refresh sessions.

## Seat-limit response

```http
409 Conflict
```

```json
{
  "code": "USER_LIMIT_REACHED",
  "message": "Your subscription user limit has been reached",
  "activeUsers": 3,
  "allowedUsers": 3
}
```
