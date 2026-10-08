# FieldReport routes: simple vs bulk

Αυτό το feature έχει δύο route patterns:

1. `fieldReportRoute` -> απλό CRUD για ένα field report
2. `fieldReportBulkRoute` -> bulk insert με όλα τα παιδιά

## 1) Simple route

File:
- `src/main/kotlin/com/example/feature/fieldreport/FieldReportRoute.kt`

Το route αυτό καλείται για:
- `GET /fieldreports`
- `POST /fieldreports`
- `PUT /fieldreports/{id}`
- `DELETE /fieldreports/{id}`

Σκοπός:
- δημιουργεί ή διαχειρίζεται ένα μόνο `fieldreports` row
- χρησιμοποιείται από το `CreateFieldReportUseCase`
- δεν αναλαμβάνει τα παιδικά tables (`fieldreport_tools`, `fieldreport_inventory`, `fieldreport_equipment`, `fieldreport_checkform`)

Παράδειγμα payload:
```json
{
  "ReportNumber": "FR-001",
  "Description": "Simple report",
  "StartDate": "2026-10-06",
  "EndDate": "2026-10-06",
  "Title": "Inspection",
  "Department": "Maintenance",
  "ClientName": "Client A",
  "ReportStatus": "Draft",
  "Value": 1500,
  "CustomerID": "customer-1",
  "ContractID": "contract-1",
  "UserID": "user-1",
  "CaseID": "case-1"
}
```

Σημασία:
- αυτό το route είναι για απλό insert/update/delete ενός report
- αν θες να περάσεις tools/inventory/equipment/checkforms, τότε δεν είναι το σωστό route

## 2) Bulk route

File:
- `src/main/kotlin/com/example/feature/fieldreport/FieldReportBulkRoute.kt`

Το route αυτό καλείται για:
- `POST /fieldreports/bulk`

Σκοπός:
- κάνει ένα “transactional” insert σε όλα τα tables μαζί
- πρώτα δημιουργεί το `fieldreports` row
- μετά προσθέτει τα `fieldreport_tools`
- μετά τα `fieldreport_inventory`
- μετά τα `fieldreport_equipment`
- και τέλος τα `fieldreport_checkform` για κάθε equipment row

Η λογική είναι:

```text
fieldreports
  -> fieldreport_tools
  -> fieldreport_inventory
  -> fieldreport_equipment
       -> fieldreport_checkform
```

Γιατί χρειάζεται:
- γιατί το `fieldreport` συνδέεται με πολλά παιδιά
- το front-end συνήθως στέλνει μαζί όλα τα στοιχεία της περιοδείας/επιθεώρησης

Παράδειγμα payload:
```json
{
  "ReportNumber": "FR-001",
  "Description": "Inspection",
  "StartDate": "2026-10-06",
  "EndDate": "2026-10-06",
  "Title": "Generator inspection",
  "Department": "Maintenance",
  "ClientName": "Client A",
  "ReportStatus": "Draft",
  "Value": 1500,
  "CustomerID": "cust-1",
  "ContractID": "contract-1",
  "UserID": "user-1",
  "CaseID": "case-1",
  "FieldReportTools": [
    { "ToolsID": "tool-1" }
  ],
  "FieldReportInventory": [
    { "InventoryID": "inv-1" }
  ],
  "FieldReportEquipments": [
    {
      "EquipmentID": "eq-1",
      "MaintenanceID": "maint-1",
      "CompletedStatus": true,
      "FieldReportCheckforms": [
        {
          "Description": "Voltage",
          "ValueExpected": "220V",
          "ValueMeasured": "218V",
          "Result": "OK"
        }
      ]
    }
  ]
}
```

## Σημαντική διαφορά

### `POST /fieldreports`
- κάνει μόνο το βασικό report
- δεν χειρίζεται relation tables

### `POST /fieldreports/bulk`
- φτιάχνει και το parent και όλα τα related records
- είναι το σωστό endpoint όταν στέλνεις ολόκληρη αναφορά με εξοπλισμό/εργαλεία/inventory/checklists

## Repository διαφορά

- `FieldReportRepository` -> το standard repository για save/find/edit/delete του single report
- `FieldReportBulkRepository` -> custom bulk insert για parent + children

## Τι ακολουθεί στο repo

Το bulk route είναι σχεδιασμένο να λειτουργεί με το ίδιο fieldreport feature, χωρίς να μπερδεύει τα απλά CRUD routes με το mass insert.

Αν το frontend στέλνει σχετικό payload, το `bulk` route θα φτιάξει τα παιδιά αυτόματα. Αν στέλνει μόνο βασικές πληροφορίες για ένα report, τότε το απλό route είναι πιο κατάλληλο.
