# FSM Backend Service - Database Structure

## Overview
This document describes the complete database structure of the FSM Backend Service application. The database is built using Exposed ORM framework and SQL.

---

## Core Tables

### 1. **users** (userTable)
User account management table.

| Field | Type | Nullable | Default | Notes |
|-------|------|----------|---------|-------|
| UserId | VARCHAR(36) | No | UUID | Primary Key |
| RemoteId | INTEGER | Yes | - | Remote system identifier |
| Name | VARCHAR(255) | Yes | - | User full name |
| Description | TEXT | Yes | - | User description |
| Email | VARCHAR(255) | Yes | - | Email address |
| Phone | VARCHAR(20) | Yes | - | Phone number |
| Signature | BINARY | Yes | - | Digital signature |
| PrefixWO | VARCHAR(50) | Yes | - | Work Order prefix |
| PrefixTC | VARCHAR(50) | Yes | - | Technical Case prefix |
| LastReportNumber | INTEGER | Yes | - | Last report number counter |
| LastTCNumber | INTEGER | Yes | - | Last technical case number counter |
| LastModified | VARCHAR(50) | Yes | - | Last modification timestamp |
| DateCreated | VARCHAR(50) | Yes | - | Creation timestamp |
| Version | VARCHAR(20) | Yes | - | Version number |
| IsDeleted | BOOLEAN | No | false | Soft delete flag |

**Relationships:**
- Referenced by: ticketTable, tasksTable, notificationsTable, fieldReportsTable, ticketHistoryTable

---

### 2. **customer** (customerTable)
Customer management table.

| Field | Type | Nullable | Default | Notes |
|-------|------|----------|---------|-------|
| CustomerId | VARCHAR(36) | No | UUID | Primary Key |
| RemoteId | INTEGER | Yes | - | Remote system identifier |
| Name | VARCHAR(255) | Yes | - | Customer name |
| Phone | VARCHAR(20) | Yes | - | Contact phone |
| Email | VARCHAR(255) | Yes | - | Contact email |
| Address | VARCHAR(255) | Yes | - | Physical address |
| ZipCode | VARCHAR(20) | Yes | - | Postal code |
| City | VARCHAR(100) | Yes | - | City name |
| Notes | TEXT | Yes | - | Additional notes |
| Description | TEXT | Yes | - | Customer description |
| CustomerStatus | BOOLEAN | Yes | - | Active/Inactive status |
| LastModified | VARCHAR(50) | Yes | - | Last modification timestamp |
| DateCreated | VARCHAR(50) | Yes | - | Creation timestamp |
| Version | VARCHAR(20) | Yes | - | Version number |
| IsDeleted | BOOLEAN | No | false | Soft delete flag |

**Relationships:**
- Referenced by: departmentTable, equipmentTable, contractsTable, fieldReportsTable, ticketTable, ticketHistoryTable

---

### 3. **department** (departmentTable)
Department/Branch management table.

| Field | Type | Nullable | Default | Notes |
|-------|------|----------|---------|-------|
| DepartmentId | VARCHAR(36) | No | UUID | Primary Key |
| RemoteId | INTEGER | Yes | - | Remote system identifier |
| Name | VARCHAR(255) | Yes | - | Department name |
| Phone | VARCHAR(100) | Yes | - | Department phone |
| Email | VARCHAR(100) | Yes | - | Department email |
| ContactPerson | VARCHAR(255) | Yes | - | Contact person name |
| Notes | TEXT | Yes | - | Additional notes |
| Description | TEXT | Yes | - | Department description |
| DepartmentStatus | BOOLEAN | Yes | - | Active/Inactive status |
| LastModified | VARCHAR(50) | Yes | - | Last modification timestamp |
| DateCreated | VARCHAR(50) | Yes | - | Creation timestamp |
| Version | VARCHAR(20) | Yes | - | Version number |
| IsDeleted | BOOLEAN | No | false | Soft delete flag |
| CustomerId | VARCHAR(36) | Yes | - | Foreign Key → customer |

**Relationships:**
- References: customerTable (CustomerId)

---

### 4. **equipment** (equipmentTable)
Equipment/Asset management table.

| Field | Type | Nullable | Default | Notes |
|-------|------|----------|---------|-------|
| EquipmentId | VARCHAR(36) | No | UUID | Primary Key |
| RemoteId | INTEGER | Yes | - | Remote system identifier |
| Name | VARCHAR(255) | Yes | - | Equipment name |
| SerialNumber | VARCHAR(100) | Yes | - | Serial number |
| Model | VARCHAR(100) | Yes | - | Equipment model |
| Manufacturer | VARCHAR(100) | Yes | - | Manufacturer name |
| Notes | TEXT | Yes | - | Additional notes |
| Description | TEXT | Yes | - | Equipment description |
| EquipmentVersion | VARCHAR(50) | Yes | - | Equipment version |
| EquipmentCategory | VARCHAR(100) | Yes | - | Equipment category |
| Warranty | VARCHAR(100) | Yes | - | Warranty information |
| EquipmentStatus | BOOLEAN | Yes | - | Active/Inactive status |
| InstallationDate | VARCHAR(50) | Yes | - | Installation date |
| LastModified | VARCHAR(50) | Yes | - | Last modification timestamp |
| DateCreated | VARCHAR(50) | Yes | - | Creation timestamp |
| Version | VARCHAR(20) | Yes | - | Version number |
| IsDeleted | BOOLEAN | No | false | Soft delete flag |
| CustomerId | VARCHAR(36) | Yes | - | Foreign Key → customer |

**Relationships:**
- References: customerTable (CustomerId)
- Referenced by: ticketTable, fieldReportEquipmentTable, contractEquipmentsTable, ticketHistoryTable

---

### 5. **tools** (toolsTable)
Tools/Instruments inventory table.

| Field | Type | Nullable | Default | Notes |
|-------|------|----------|---------|-------|
| ToolsId | VARCHAR(36) | No | UUID | Primary Key |
| RemoteId | INTEGER | Yes | - | Remote system identifier |
| Title | VARCHAR(255) | Yes | - | Tool name |
| Description | TEXT | Yes | - | Tool description |
| Model | VARCHAR(100) | Yes | - | Tool model |
| Manufacturer | VARCHAR(100) | Yes | - | Manufacturer name |
| SerialNumber | VARCHAR(100) | Yes | - | Serial number |
| CalibrationDate | VARCHAR(50) | Yes | - | Last calibration date |
| LastModified | VARCHAR(50) | Yes | - | Last modification timestamp |
| DateCreated | VARCHAR(50) | Yes | - | Creation timestamp |
| Version | VARCHAR(20) | Yes | - | Version number |
| IsDeleted | BOOLEAN | No | false | Soft delete flag |

**Relationships:**
- Referenced by: fieldReportToolsTable

---

## Ticket & Work Order Management

### 6. **ticket** (ticketTable)
Service tickets/Work orders management.

| Field | Type | Nullable | Default | Notes |
|-------|------|----------|---------|-------|
| TicketId | VARCHAR(36) | No | UUID | Primary Key |
| RemoteId | INTEGER | Yes | - | Remote system identifier |
| Title | VARCHAR(255) | Yes | - | Ticket title |
| TicketNumber | VARCHAR(50) | Yes | - | Ticket reference number |
| Description | TEXT | Yes | - | Detailed description |
| Notes | TEXT | Yes | - | Internal notes |
| Urgency | VARCHAR(50) | Yes | - | Priority level |
| Active | BOOLEAN | Yes | true | Active/Closed status |
| DateStart | VARCHAR(50) | Yes | - | Start date |
| DateEnd | VARCHAR(50) | Yes | - | End/Completion date |
| LastModified | VARCHAR(50) | Yes | - | Last modification timestamp |
| DateCreated | VARCHAR(50) | Yes | - | Creation timestamp |
| Version | VARCHAR(20) | Yes | - | Version number |
| IsDeleted | BOOLEAN | No | false | Soft delete flag |
| UserId | VARCHAR(36) | Yes | - | Foreign Key → users |
| CustomerId | VARCHAR(36) | Yes | - | Foreign Key → customer |
| EquipmentId | VARCHAR(36) | Yes | - | Foreign Key → equipment |

**Relationships:**
- References: userTable, customerTable, equipmentTable
- Referenced by: tasksTable, fieldReportsTable, ticketHistoryTable

---

### 7. **ticket_history** (ticketHistoryTable)
Audit/History log for tickets.

| Field | Type | Nullable | Default | Notes |
|-------|------|----------|---------|-------|
| TicketHistoryId | VARCHAR(36) | No | UUID | Primary Key |
| RemoteId | INTEGER | Yes | - | Remote system identifier |
| Title | VARCHAR(255) | Yes | - | Ticket title (snapshot) |
| TicketNumber | VARCHAR(255) | Yes | - | Ticket reference number |
| Description | TEXT | Yes | - | Detailed description |
| Notes | VARCHAR(50) | Yes | - | Internal notes |
| Urgency | VARCHAR(50) | Yes | - | Priority level |
| Active | BOOLEAN | Yes | - | Active/Closed status |
| DateStart | VARCHAR(50) | Yes | - | Start date |
| DateEnd | VARCHAR(50) | Yes | - | End/Completion date |
| LastModified | VARCHAR(50) | Yes | - | Last modification timestamp |
| DateCreated | VARCHAR(50) | Yes | - | Creation timestamp |
| Version | VARCHAR(50) | Yes | - | Version number |
| IsDeleted | BOOLEAN | No | false | Soft delete flag |
| UserId | VARCHAR(36) | Yes | - | Foreign Key → users |
| CustomerId | VARCHAR(36) | Yes | - | Foreign Key → customer |
| EquipmentId | VARCHAR(36) | Yes | - | Foreign Key → equipment |

**Relationships:**
- References: userTable, customerTable, equipmentTable

---

### 8. **tasks** (tasksTable)
Task management linked to tickets.

| Field | Type | Nullable | Default | Notes |
|-------|------|----------|---------|-------|
| TaskId | VARCHAR(36) | No | UUID | Primary Key |
| Title | VARCHAR(255) | Yes | - | Task title |
| Description | TEXT | Yes | - | Task description |
| Status | VARCHAR(50) | Yes | - | Task status (pending, in-progress, complete, etc.) |
| Priority | VARCHAR(50) | Yes | - | Priority level |
| DateStart | VARCHAR(50) | Yes | - | Start date |
| DateDue | VARCHAR(50) | Yes | - | Due date |
| DateCompleted | VARCHAR(50) | Yes | - | Completion date |
| LastModified | VARCHAR(50) | Yes | - | Last modification timestamp |
| DateCreated | VARCHAR(50) | Yes | - | Creation timestamp |
| IsDeleted | BOOLEAN | No | false | Soft delete flag |
| TicketId | VARCHAR(36) | Yes | - | Foreign Key → ticket |
| UserId | VARCHAR(36) | Yes | - | Foreign Key → users |

**Relationships:**
- References: ticketTable, userTable

---

## Contracts Management

### 9. **contracts** (contractsTable)
Service contracts management.

| Field | Type | Nullable | Default | Notes |
|-------|------|----------|---------|-------|
| ContractId | VARCHAR(36) | No | UUID | Primary Key |
| RemoteId | INTEGER | Yes | - | Remote system identifier |
| Title | VARCHAR(255) | Yes | - | Contract title |
| DateStart | VARCHAR(50) | Yes | - | Contract start date |
| DateEnd | VARCHAR(50) | Yes | - | Contract end date |
| Value | DOUBLE | Yes | - | Contract value |
| Notes | TEXT | Yes | - | Additional notes |
| Description | TEXT | Yes | - | Contract description |
| ContractType | VARCHAR(100) | Yes | - | Type of contract |
| ContractStatus | BOOLEAN | Yes | - | Active/Inactive status |
| ContactName | VARCHAR(255) | Yes | - | Contact person name |
| LastModified | VARCHAR(50) | Yes | - | Last modification timestamp |
| DateCreated | VARCHAR(50) | Yes | - | Creation timestamp |
| Version | VARCHAR(20) | Yes | - | Version number |
| IsDeleted | BOOLEAN | No | false | Soft delete flag |
| CustomerId | VARCHAR(36) | Yes | - | Foreign Key → customer |

**Relationships:**
- References: customerTable
- Referenced by: contractEquipmentsTable, fieldReportsTable

---

### 10. **Contract_equipments** (contractEquipmentsTable)
Mapping table for equipment covered by contracts.

| Field | Type | Nullable | Default | Notes |
|-------|------|----------|---------|-------|
| ContractEquipmentId | VARCHAR(36) | No | UUID | Primary Key |
| RemoteId | INTEGER | Yes | - | Remote system identifier |
| Value | DOUBLE | Yes | - | Equipment value under contract |
| Visits | DOUBLE | Yes | - | Number of visits allowed |
| ContractId | VARCHAR(36) | Yes | - | Foreign Key → contracts |
| EquipmentId | VARCHAR(36) | Yes | - | Foreign Key → equipment |
| LastModified | VARCHAR(50) | Yes | - | Last modification timestamp |
| DateCreated | VARCHAR(50) | Yes | - | Creation timestamp |
| Version | VARCHAR(20) | Yes | - | Version number |
| IsDeleted | BOOLEAN | No | false | Soft delete flag |

**Relationships:**
- References: contractsTable, equipmentTable

---

## Field Reports & Work Records

### 11. **fieldreports** (fieldReportsTable)
Field service reports/Work reports.

| Field | Type | Nullable | Default | Notes |
|-------|------|----------|---------|-------|
| FieldReportId | VARCHAR(36) | No | UUID | Primary Key |
| RemoteId | INTEGER | Yes | - | Remote system identifier |
| ReportNumber | VARCHAR(255) | Yes | - | Report reference number |
| Description | TEXT | Yes | - | Report description |
| StartDate | VARCHAR(255) | Yes | - | Service start date |
| EndDate | VARCHAR(255) | Yes | - | Service end date |
| Title | VARCHAR(255) | Yes | - | Report title |
| Department | VARCHAR(255) | Yes | - | Department name |
| ClientName | VARCHAR(255) | Yes | - | Client name |
| ReportStatus | VARCHAR(255) | Yes | - | Report status |
| ClientSignature | BINARY | Yes | - | Client signature |
| Value | DOUBLE | Yes | - | Service value |
| LastModified | VARCHAR(255) | Yes | - | Last modification timestamp |
| DateCreated | VARCHAR(255) | Yes | - | Creation timestamp |
| Version | VARCHAR(255) | Yes | - | Version number |
| IsDeleted | BOOLEAN | No | false | Soft delete flag |
| CustomerId | VARCHAR(36) | Yes | - | Foreign Key → customer |
| ContractId | VARCHAR(36) | Yes | - | Foreign Key → contracts |
| UserId | VARCHAR(36) | Yes | - | Foreign Key → users |
| CaseId | VARCHAR(36) | Yes | - | Foreign Key → ticket |

**Relationships:**
- References: customerTable, contractsTable, userTable, ticketTable
- Referenced by: fieldReportEquipmentTable, fieldReportToolsTable, fieldReportInventoryTable

---

### 12. **fieldreport_equipment** (fieldReportEquipmentTable)
Equipment details in field reports.

| Field | Type | Nullable | Default | Notes |
|-------|------|----------|---------|-------|
| FieldReportEquipmentId | VARCHAR(36) | No | UUID | Primary Key |
| RemoteId | INTEGER | Yes | - | Remote system identifier |
| CompletedStatus | BOOLEAN | Yes | - | Completion status |
| LastModified | VARCHAR(255) | Yes | - | Last modification timestamp |
| DateCreated | VARCHAR(255) | Yes | - | Creation timestamp |
| Version | VARCHAR(255) | Yes | - | Version number |
| IsDeleted | BOOLEAN | No | false | Soft delete flag |
| FieldReportId | VARCHAR(36) | Yes | - | Foreign Key → fieldreports |
| EquipmentId | VARCHAR(36) | Yes | - | Foreign Key → equipment |
| MaintenanceId | VARCHAR(36) | Yes | - | Foreign Key → maintenances |

**Relationships:**
- References: fieldReportsTable, equipmentTable, maintenancesTable
- Referenced by: fieldReportCheckformTable

---

### 13. **fieldreport_tools** (fieldReportToolsTable)
Tools used in field reports.

| Field | Type | Nullable | Default | Notes |
|-------|------|----------|---------|-------|
| FieldReportToolsId | VARCHAR(36) | No | UUID | Primary Key |
| RemoteId | INTEGER | Yes | - | Remote system identifier |
| FieldReportId | VARCHAR(36) | Yes | - | Foreign Key → fieldreports |
| ToolsId | VARCHAR(36) | Yes | - | Tool identifier (not FK) |
| LastModified | VARCHAR(255) | Yes | - | Last modification timestamp |
| DateCreated | VARCHAR(255) | Yes | - | Creation timestamp |
| Version | VARCHAR(255) | Yes | - | Version number |
| IsDeleted | BOOLEAN | No | false | Soft delete flag |

**Relationships:**
- References: fieldReportsTable

---

### 14. **fieldreport_inventory** (fieldReportInventoryTable)
Inventory items used in field reports.

| Field | Type | Nullable | Default | Notes |
|-------|------|----------|---------|-------|
| FieldReportInventoryId | VARCHAR(36) | No | UUID | Primary Key |
| RemoteId | INTEGER | Yes | - | Remote system identifier |
| LastModified | VARCHAR(255) | Yes | - | Last modification timestamp |
| DateCreated | VARCHAR(255) | Yes | - | Creation timestamp |
| Version | VARCHAR(255) | Yes | - | Version number |
| IsDeleted | BOOLEAN | No | false | Soft delete flag |
| FieldReportId | VARCHAR(36) | Yes | - | Field report identifier (not FK) |
| InventoryId | VARCHAR(36) | Yes | - | Foreign Key → inventory_table |

**Relationships:**
- References: inventoryTable

---

### 15. **fieldreport_checkform** (fieldReportCheckformTable)
Checklist/Inspection items in field reports.

| Field | Type | Nullable | Default | Notes |
|-------|------|----------|---------|-------|
| FieldReportCheckFormId | VARCHAR(36) | No | UUID | Primary Key |
| RemoteId | INTEGER | Yes | - | Remote system identifier |
| FieldReportEquipmentId | VARCHAR(36) | Yes | - | Foreign Key → fieldreport_equipment |
| Description | TEXT | Yes | - | Check item description |
| ValueExpected | TEXT | Yes | - | Expected value/result |
| ValueMeasured | TEXT | Yes | - | Measured/Actual value |
| Result | TEXT | Yes | - | Check result |
| LastModified | VARCHAR(50) | Yes | - | Last modification timestamp |
| DateCreated | VARCHAR(50) | Yes | - | Creation timestamp |
| Version | VARCHAR(20) | Yes | - | Version number |
| IsDeleted | BOOLEAN | No | false | Soft delete flag |

**Relationships:**
- References: fieldReportEquipmentTable

---

## Maintenance & Inspection

### 16. **maintenances** (maintenancesTable)
Maintenance plans and records.

| Field | Type | Nullable | Default | Notes |
|-------|------|----------|---------|-------|
| MaintenanceId | VARCHAR(36) | No | UUID | Primary Key |
| RemoteId | INTEGER | Yes | - | Remote system identifier |
| Name | VARCHAR(255) | Yes | - | Maintenance plan name |
| Description | TEXT | Yes | - | Description |
| LastModified | VARCHAR(255) | Yes | - | Last modification timestamp |
| DateCreated | VARCHAR(255) | Yes | - | Creation timestamp |
| Version | VARCHAR(255) | Yes | - | Version number |
| IsDeleted | BOOLEAN | No | false | Soft delete flag |

**Relationships:**
- Referenced by: fieldReportEquipmentTable

---

### 17. **check_forms** (checkFormsTable)
Inspection/Maintenance checklist templates.

| Field | Type | Nullable | Default | Notes |
|-------|------|----------|---------|-------|
| CheckFormId | VARCHAR(36) | No | UUID | Primary Key |
| RemoteId | INTEGER | Yes | - | Remote system identifier |
| MaintenancesId | VARCHAR(255) | Yes | - | Maintenance plan identifier |
| Description | TEXT | Yes | - | Check item description |
| ValueExpected | TEXT | Yes | - | Expected value |
| ValueType | VARCHAR(50) | Yes | - | Input type (checkbox, text, number, etc.) |
| LastModified | VARCHAR(50) | Yes | - | Last modification timestamp |
| DateCreated | VARCHAR(50) | Yes | - | Creation timestamp |
| Version | VARCHAR(20) | Yes | - | Version number |
| IsDeleted | BOOLEAN | No | false | Soft delete flag |

**Relationships:**
- No foreign key references (MaintenancesId is not enforced as FK)

---

## Inventory & Assets

### 18. **inventory_table** (inventoryTable)
Spare parts and inventory items.

| Field | Type | Nullable | Default | Notes |
|-------|------|----------|---------|-------|
| InventoryId | VARCHAR(36) | No | UUID | Primary Key |
| RemoteId | INTEGER | Yes | - | Remote system identifier |
| Title | VARCHAR(255) | Yes | - | Item name |
| Description | TEXT | Yes | - | Item description |
| Quantity | LONG | Yes | - | Quantity in stock |
| Value | DOUBLE | Yes | - | Unit value |
| Type | VARCHAR(255) | Yes | - | Item type/category |
| LastModified | VARCHAR(255) | Yes | - | Last modification timestamp |
| DateCreated | VARCHAR(255) | Yes | - | Creation timestamp |
| Version | VARCHAR(255) | Yes | - | Version number |
| IsDeleted | BOOLEAN | No | false | Soft delete flag |

**Relationships:**
- Referenced by: fieldReportInventoryTable

---

## Taxonomies & Classifications

### 19. **category_asset** (categoryAssetTable)
Asset/Equipment categories.

| Field | Type | Nullable | Default | Notes |
|-------|------|----------|---------|-------|
| CategoryAssetId | VARCHAR(36) | No | UUID | Primary Key |
| RemoteId | INTEGER | Yes | - | Remote system identifier |
| Name | VARCHAR(255) | Yes | - | Category name |
| Style | VARCHAR(255) | Yes | - | UI style/color coding |
| LastModified | VARCHAR(50) | Yes | - | Last modification timestamp |
| DateCreated | VARCHAR(50) | Yes | - | Creation timestamp |
| Version | VARCHAR(20) | Yes | - | Version number |
| IsDeleted | BOOLEAN | No | false | Soft delete flag |

**Note:** Duplicate table exists as CategoriesTable (see below)

---

### 20. **category_asset** (CategoriesTable)
Asset categories (duplicate table definition).

| Field | Type | Nullable | Default | Notes |
|-------|------|----------|---------|-------|
| CategoryAssetId | VARCHAR(36) | No | UUID | Primary Key |
| RemoteId | INTEGER | Yes | - | Remote system identifier |
| Name | VARCHAR(255) | Yes | - | Category name |
| Style | VARCHAR(255) | Yes | - | UI style/color coding |
| LastModified | VARCHAR(50) | Yes | - | Last modification timestamp |
| DateCreated | VARCHAR(50) | Yes | - | Creation timestamp |
| Version | VARCHAR(20) | Yes | - | Version number |
| IsDeleted | BOOLEAN | No | false | Soft delete flag |

---

### 21. **manufacturer** (manufacturerTable)
Equipment manufacturers catalog.

| Field | Type | Nullable | Default | Notes |
|-------|------|----------|---------|-------|
| ManufacturerId | VARCHAR(36) | No | UUID | Primary Key |
| RemoteId | INTEGER | Yes | - | Remote system identifier |
| Name | VARCHAR(255) | Yes | - | Manufacturer name |
| Style | VARCHAR(255) | Yes | - | UI style/color coding |
| LastModified | VARCHAR(255) | Yes | - | Last modification timestamp |
| DateCreated | VARCHAR(255) | Yes | - | Creation timestamp |
| Version | VARCHAR(255) | Yes | - | Version number |
| IsDeleted | BOOLEAN | No | false | Soft delete flag |

---

### 22. **model_asset** (modelAssetTable)
Equipment models catalog.

| Field | Type | Nullable | Default | Notes |
|-------|------|----------|---------|-------|
| ModelId | VARCHAR(36) | No | UUID | Primary Key |
| RemoteId | INTEGER | Yes | - | Remote system identifier |
| Name | VARCHAR(255) | Yes | - | Model name |
| Style | VARCHAR(255) | Yes | - | UI style/color coding |
| LastModified | VARCHAR(255) | Yes | - | Last modification timestamp |
| DateCreated | VARCHAR(255) | Yes | - | Creation timestamp |
| Version | VARCHAR(255) | Yes | - | Version number |
| IsDeleted | BOOLEAN | No | false | Soft delete flag |

---

## System Settings & Notifications

### 23. **settings** (settingsTable)
Application settings and configuration.

| Field | Type | Nullable | Default | Notes |
|-------|------|----------|---------|-------|
| SettingsId | VARCHAR(36) | No | UUID | Primary Key |
| RemoteId | INTEGER | Yes | - | Remote system identifier |
| SettingsKey | VARCHAR(255) | Yes | - | Setting key/name |
| SettingsValue | VARCHAR(255) | Yes | - | Setting value |
| SettingsStyle | VARCHAR(255) | Yes | - | UI style/presentation |
| SettingsDescription | TEXT | Yes | - | Setting description |
| LastModified | VARCHAR(255) | Yes | - | Last modification timestamp |
| DateCreated | VARCHAR(255) | Yes | - | Creation timestamp |
| Version | VARCHAR(255) | Yes | - | Version number |
| IsDeleted | BOOLEAN | No | false | Soft delete flag |

---

### 24. **notifications** (notificationsTable)
User notifications and alerts.

| Field | Type | Nullable | Default | Notes |
|-------|------|----------|---------|-------|
| NotificationId | VARCHAR(36) | No | UUID | Primary Key |
| UserId | VARCHAR(36) | No | - | Foreign Key → users |
| Title | VARCHAR(255) | Yes | - | Notification title |
| Message | TEXT | Yes | - | Notification message |
| Type | VARCHAR(50) | Yes | - | Notification type |
| EntityType | VARCHAR(50) | Yes | - | Related entity type |
| EntityId | VARCHAR(36) | Yes | - | Related entity identifier |
| IsRead | BOOLEAN | No | false | Read status |
| ReadAt | VARCHAR(50) | Yes | - | Timestamp when read |
| DateCreated | VARCHAR(50) | Yes | - | Creation timestamp |
| IsDeleted | BOOLEAN | No | false | Soft delete flag |

**Relationships:**
- References: userTable (UserId) - REQUIRED

---

## Database Relationships Diagram

```
users
├── References
│   └── None
├── Referenced By
│   ├── ticketTable
│   ├── tasksTable
│   ├── notificationsTable
│   ├── fieldReportsTable
│   └── ticketHistoryTable
└── 1:N Relationship with: tickets, tasks, notifications, fieldreports

customer
├── References
│   └── None
├── Referenced By
│   ├── departmentTable
│   ├── equipmentTable
│   ├── contractsTable
│   ├── ticketTable
│   ├── fieldReportsTable
│   └── ticketHistoryTable
└── 1:N Relationship with: departments, equipment, contracts, tickets, fieldreports

equipment
├── References
│   └── customer (CustomerId)
├── Referenced By
│   ├── ticketTable
│   ├── fieldReportEquipmentTable
│   ├── contractEquipmentsTable
│   └── ticketHistoryTable
└── 1:N Relationship with: tickets, fieldreport_equipment, contract_equipments

ticketTable
├── References
│   ├── users (UserId)
│   ├── customer (CustomerId)
│   └── equipment (EquipmentId)
├── Referenced By
│   ├── tasksTable
│   ├── fieldReportsTable
│   └── ticketHistoryTable
└── 1:N Relationship with: tasks, fieldreports

contractsTable
├── References
│   └── customer (CustomerId)
├── Referenced By
│   ├── contractEquipmentsTable
│   └── fieldReportsTable
└── 1:N Relationship with: contract_equipments, fieldreports

fieldReportsTable
├── References
│   ├── customer (CustomerId)
│   ├── contracts (ContractId)
│   ├── users (UserId)
│   └── ticketTable (CaseId)
├── Referenced By
│   ├── fieldReportEquipmentTable
│   ├── fieldReportToolsTable
│   └── fieldReportInventoryTable
└── 1:N Relationship with: fieldreport_equipment, fieldreport_tools, fieldreport_inventory

fieldReportEquipmentTable
├── References
│   ├── fieldReportsTable
│   ├── equipmentTable
│   └── maintenancesTable
├── Referenced By
│   └── fieldReportCheckformTable
└── 1:N Relationship with: fieldreport_checkform

inventoryTable
├── References
│   └── None
├── Referenced By
│   └── fieldReportInventoryTable
└── 1:N Relationship with: fieldreport_inventory

maintenancesTable
├── References
│   └── None
├── Referenced By
│   └── fieldReportEquipmentTable
└── 1:N Relationship with: fieldreport_equipment
```

---

## Key Design Patterns

1. **Soft Delete:** All tables include `IsDeleted` (BOOLEAN) flag for logical deletion
2. **Audit Trail:** Most tables include `LastModified`, `DateCreated`, and `Version` fields
3. **Remote Synchronization:** `RemoteId` field in most tables for syncing with remote systems
4. **UUID Primary Keys:** All primary keys use UUID (36-char VARCHAR)
5. **Nullable Fields:** Most fields are nullable to support progressive data entry
6. **String Timestamps:** Date/time stored as VARCHAR(50) instead of TIMESTAMP type

---

## Notable Issues/Observations

1. **Duplicate Tables:** `categoryAssetTable` and `CategoriesTable` define the same table "category_asset"
   - **Recommendation:** Remove one duplicate

2. **Missing Foreign Keys:** 
   - `fieldReportInventoryTable.FieldReportId` - not a foreign key, should reference fieldReportsTable
   - `checkFormsTable.MaintenancesId` - VARCHAR(255) but should reference maintenancesTable

3. **Type Inconsistencies:**
   - `fieldReportCheckformTable` in fieldReportCheckformTable.kt has `fieldReportEquipmentId` as VARCHAR(36) with reference
   - Some references use different ID field names (EquipmentID vs EquipmentId)

4. **Data Type Recommendations:**
   - Consider using TIMESTAMP or DATETIME instead of VARCHAR(50) for date fields
   - Consider using proper BIGINT for integer fields instead of INTEGER

---

## Related Files
- Database Models Location: `src/main/kotlin/com/example/models/databaseModels/`
- ORM Framework: Jetbrains Exposed
- Database Type: SQL (SQLite, PostgreSQL, MySQL compatible)

