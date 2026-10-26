# Field Report PDF API

## Endpoint

`GET /fieldreports/{id}/pdf`

Requires the existing `auth-jwt-v2` bearer token. The report is resolved only in the database associated with the authenticated tenant.

Successful responses use `Content-Type: application/pdf` and `Content-Disposition: attachment; filename="<ReportNumber>.pdf"`.

Example Postman request:

```http
GET {{baseUrl}}/fieldreports/11111111-1111-1111-1111-111111111111/pdf
Authorization: Bearer {{accessToken}}
Accept: application/pdf
```

In Postman, choose **Send and Download** to save the response.

Responses: `400` invalid ID/template limits, `401` missing or invalid authentication, `404` report absent from the authenticated tenant, and `500` unexpected rendering failure. The endpoint follows the existing Field Report read authorization model: any active authenticated tenant user can read a report in that tenant. There is currently no finer-grained Field Report permission model from which to produce a meaningful `403`.

## Settings

Optional tenant settings:

- `PDF_FIELDREPORT_MAIN`
- `PDF_FIELDREPORT_EQUIPMENT`
- `PDF_FIELDREPORT_CHECKFORMS`
- `PDF_COMPANY_LOGO` — Base64 PNG/JPEG, optionally as a data URL
- `PDF_COMPANY_PHONE`
- `PDF_COMPANY_ADDRESS`
- `PDF_COMPANY_FOOTER`

Missing, blank, oversized, syntactically invalid, or unsafe custom templates fall back to bundled defaults. Existing settings are never overwritten. Run `migrations/pdf_fieldreport_templates.sql` on each existing tenant database to change `SettingsValue` from `VARCHAR(255)` to `TEXT`.

## Template variables

All objects are restricted maps containing strings, booleans, numbers, or lists. Missing values render as empty strings and dynamic text is HTML-escaped.

### Main Report & Tools

- `report.id`, `report.reportNumber`, `report.title`, `report.description`
- `report.startDate`, `report.endDate`, `report.scheduledStart`, `report.scheduledEnd`
- `report.status`, `report.priority`, `report.type`, `report.department`
- `report.customerName`, `report.clientName`, `report.technicianName`
- `report.contractName`, `report.ticketNumber`, `report.signatureDataUri`
- `tools`: `name`, `manufacturer`, `model`, `serialNumber`, `calibrationDate`

### Equipment & Spare Parts

- `equipments`: `id`, `equipmentId`, `name`, `manufacturer`, `model`, `serialNumber`, `maintenanceName`, `completed`
- `inventories`: `name`, `quantity`

### CheckForms

- `equipments` as above
- `equipment.checkforms`: `description`, `expected`, `measured`, `result`

All templates can also access `company.name`, `company.email`, `company.address`, `company.phone`, `company.footer`, and `company.logoDataUri`.

Supported template constructs include escaped `{{ value }}`, `{% if %}`, and `{% for %}`. Includes, imports, inheritance, embedded templates, parallel execution, method calls, scripts, SVG, forms, and external URLs are rejected.

## Limits and behavior

- Templates: 250,000 characters each.
- Rendered HTML: 5,000,000 characters.
- Images: PNG/JPEG only, maximum 2 MiB and 4096×4096 pixels.
- Aggregate list/checkform limit: 5,000 records.
- Generated PDF: maximum 25 MiB.
- PDFs are generated on demand on `Dispatchers.IO` and are not persisted.
- Dates in the legacy `yyyyMMddHHmmss` format and ISO date prefixes are displayed as `dd/MM/yyyy`.
- The existing signature storage is unchanged. Unsupported signature content, including SVG, produces an empty signature area.

## Testing

```powershell
.\gradlew.bat test --no-daemon
```

Focused PDF tests:

```powershell
.\gradlew.bat test --tests com.example.feature.pdf.FieldReportPdfModuleTest --no-daemon
```

The focused tests generate real PDFs, load them with PDFBox, inspect page counts, and extract Greek/English text.
