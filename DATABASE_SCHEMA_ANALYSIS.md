# Database Schema Analysis & Optimization Guide

## Overview
This document provides a comprehensive evaluation of your FSM (Field Service Management) backend database schema, including identified issues, optimization suggestions, and recommended additions.

---

## Current Database Tables (21 tables)

### Core Entity Tables
- **users** - User/Technician information
- **customer** - Customer/Client information
- **department** - Customer departments
- **contracts** - Service contracts
- **tasks** - Work tasks (linked to tickets)
- **ticket** - Support/service tickets
- **ticket_history** - Historical audit of ticket changes
- **equipment** - Customer equipment/assets
- **maintenances** - Maintenance records
- **tools** - Tools inventory
- **inventory_table** - Parts/materials inventory
- **check_forms** - Inspection/check forms template
- **manufacturer** - Equipment manufacturers
- **category_asset** - Asset categories
- **model_asset** - Asset models
- **settings** - Application settings

### Junction/Relationship Tables
- **Contract_equipments** - Links contracts to equipment
- **fieldreport_equipment** - Links field reports to equipment with maintenance
- **fieldreport_tools** - Links field reports to tools used
- **fieldreport_inventory** - Links field reports to inventory used
- **fieldreport_checkform** - Links field reports to inspection results

---

## 🔴 Critical Issues & Recommendations

### 1. **Date/Time Storage Format ✅ APPROPRIATE FOR YOUR USE CASE**
**Current Implementation:** ✅ RECOMMENDED - Keep as is
```kotlin
// ✅ WELL-DESIGNED for cross-platform sync
val lastModified = varchar("LastModified", 50).nullable()  // Format: "202405051430" (yyyyMMddHHmm)
val dateCreated = varchar("DateCreated", 50).nullable()    // Consistent format
```

**Your Format Strategy (Excellent for data consistency):**
- **Storage Format:** `yyyyMMddHHmm` (database/sync)
- **UI Display:** `dd/MM/YYYY` (front-end conversion)
- **Advantages:** 
  - ✅ Lexicographically sortable (database queries work correctly)
  - ✅ No timezone ambiguity
  - ✅ Compact storage (12 chars)
  - ✅ Works seamlessly with Android SQLite+Room
  - ✅ Easy to parse on both platforms
  - ✅ Sync-safe (no format mismatch issues)

**No Changes Needed** - Your current approach is solid for multi-platform consistency.

### 2. **Missing Foreign Key References** ⚠️ HIGH PRIORITY
**Issue:** Some relationships are not properly defined as foreign keys
```kotlin
// ❌ In fieldReportToolsTable (line shows toolsId as NOT referenced)
val toolsId = varchar("ToolsId", 36).nullable()

// ❌ In fieldReportInventoryTable (fieldReportID not a FK)
val fieldReportID = varchar("FieldReportId", 36).nullable()

// ❌ In checkFormsTable
val maintenancesId = varchar("MaintenancesId", 255).nullable()
```
**Action:** Add proper foreign key references:
```kotlin
// In fieldReportToolsTable
val toolsId = varchar("ToolsId", 36).references(toolsTable.toolsId).nullable()

// In fieldReportInventoryTable  
val fieldReportID = varchar("FieldReportId", 36).references(fieldReportsTable.fieldReportId).nullable()

// In checkFormsTable
val maintenancesId = varchar("MaintenancesId", 36).references(maintenancesTable.maintenanceId).nullable()
```

### 3. **Missing Reference Options (CASCADE/SET NULL)** ⚠️ MEDIUM PRIORITY
**Issue:** No cascading delete rules defined
```kotlin
// ❌ CURRENT
val equipmentId = varchar("EquipmentId",36).references(equipmentTable.equipmentId).nullable()

// ✅ RECOMMENDED
val equipmentId = varchar("EquipmentId",36).references(equipmentTable.equipmentId, ReferenceOption.SET_NULL).nullable()
```
**Impact:** Orphaned records remain when parent entities are deleted
**Action:** Add appropriate ReferenceOptions to all foreign keys

### 4. **Missing Database Indexes** ⚠️ HIGH PRIORITY
**Missing indexes on:**
- Foreign key columns (automatic in some DBs but should be explicit)
- Status/state fields (`active`, `status`, `urgency`, `equipmentStatus`, etc.)
- Date range queries (`dateCreated`, `dateStart`, `dateEnd`)
- Email and phone fields (if used for searches)
- `remoteId` (appears to be for sync operations)

**Suggested Indexes:**
```kotlin
// Add to tables
val indexUserId = index("idx_userid", userId)  // In tickets, tasks
val indexCustomerId = index("idx_customerid", customerId)  // Multiple tables
val indexDateCreated = index("idx_datecreated", dateCreated)  // All audit tables
val indexStatus = index("idx_status", active)  // tickets
val indexEquipmentId = index("idx_equipmentid", equipmentId)  // Multiple tables

// Composite indexes for common queries
val dateRangeIndex = index("idx_date_range", dateStart, dateEnd)  // fieldreports
val contractEquipmentIndex = index("idx_contract_equip", contractId, equipmentId)  // Contract_equipments
val ticketStatusIndex = index("idx_ticket_status", active, customerId)  // tickets
```

### 5. **Inconsistent Naming Conventions** ⚠️ MEDIUM PRIORITY
**Issue:** Mixed camelCase, snake_case, and inconsistent prefixes
```
❌ INCONSISTENT:
- fieldReportId vs FieldReportId (column names vary case)
- inventory_table vs fieldreports (table names inconsistent)
- fieldReportID vs fieldReportId (ID inconsistency)
- maintenancesTable vs ticketTable (class naming)

✅ RECOMMENDED:
- Table names: lowercase with underscores (field_reports, field_report_equipment)
- Columns: snake_case in database (field_report_id)
- Kotlin properties: camelCase (fieldReportId)
```

### 6. **Missing Unique Constraints** ⚠️ MEDIUM PRIORITY
```kotlin
// Add to prevent duplicates
val emailUnique = uniqueIndex("unique_user_email", email)  // In userTable
val emailUnique = uniqueIndex("unique_customer_email", email)  // In customerTable
val serialUnique = uniqueIndex("unique_serial", serialNumber)  // In equipmentTable, toolsTable
val ticketNumberUnique = uniqueIndex("unique_ticket_number", ticketNumber)  // In ticketTable
val reportNumberUnique = uniqueIndex("unique_report_number", reportNumber)  // In fieldReportsTable
val settingsKeyUnique = uniqueIndex("unique_settings_key", settingsKey)  // In settingsTable
```

### 7. **Inconsistent Primary Key Names** ⚠️ LOW PRIORITY
Column names for PK don't match pattern; consider standardization:
```
userTable -> UserId (OK)
customerTable -> CustomerId (OK)
But inconsistent: FieldReportId vs TicketId (camelCase varies)
```

---

## 📋 Missing Tables/Features

### 1. **User Roles & Permissions Table** ⚠️ HIGH PRIORITY
```kotlin
object userRolesTable : Table("user_roles") {
    val userRoleId = varchar("UserRoleId", 36).default(UUID.randomUUID().toString())
    val userId = varchar("UserId", 36).references(userTable.userId, ReferenceOption.CASCADE)
    val roleId = varchar("RoleId", 36).references(rolesTable.roleId)
    val assignedDate = datetime("AssignedDate").default(CurrentDateTime)
    
    override val primaryKey = PrimaryKey(userRoleId)
}

object rolesTable : Table("roles") {
    val roleId = varchar("RoleId", 36).default(UUID.randomUUID().toString())
    val roleName = varchar("RoleName", 100).uniqueIndex()
    val description = text("Description").nullable()
    val permissions = text("Permissions").nullable()
    val isActive = bool("IsActive").default(true)
    
    override val primaryKey = PrimaryKey(roleId)
}
```

### 2. **Audit Log Table** ⚠️ HIGH PRIORITY
```kotlin
object auditLogsTable : Table("audit_logs") {
    val auditId = varchar("AuditId", 36).default(UUID.randomUUID().toString())
    val userId = varchar("UserId", 36).references(userTable.userId, ReferenceOption.SET_NULL).nullable()
    val entityName = varchar("EntityName", 100)
    val entityId = varchar("EntityId", 36)
    val action = varchar("Action", 50)  // CREATE, UPDATE, DELETE
    val oldValue = text("OldValue").nullable()
    val newValue = text("NewValue").nullable()
    val timestamp = datetime("Timestamp").default(CurrentDateTime)
    val ipAddress = varchar("IpAddress", 45).nullable()
    
    override val primaryKey = PrimaryKey(auditId)
    val indexTimestamp = index("idx_audit_timestamp", timestamp)
    val indexEntity = index("idx_audit_entity", entityName, entityId)
}
```

### 3. **Attachment/Document Storage Table** ⚠️ MEDIUM PRIORITY
```kotlin
object documentsTable : Table("documents") {
    val documentId = varchar("DocumentId", 36).default(UUID.randomUUID().toString())
    val fieldReportId = varchar("FieldReportId", 36).references(fieldReportsTable.fieldReportId, ReferenceOption.CASCADE)
    val fileName = varchar("FileName", 255)
    val fileType = varchar("FileType", 50)
    val fileSize = long("FileSize")
    val filePath = varchar("FilePath", 500)
    val uploadDate = datetime("UploadDate").default(CurrentDateTime)
    val uploadedBy = varchar("UploadedBy", 36).references(userTable.userId).nullable()
    val description = text("Description").nullable()
    
    override val primaryKey = PrimaryKey(documentId)
}
```

### 4. **Equipment-Maintenance Relationship** ⚠️ MEDIUM PRIORITY
```kotlin
object equipmentMaintenanceHistoryTable : Table("equipment_maintenance_history") {
    val historyId = varchar("HistoryId", 36).default(UUID.randomUUID().toString())
    val equipmentId = varchar("EquipmentId", 36).references(equipmentTable.equipmentId, ReferenceOption.CASCADE)
    val maintenanceId = varchar("MaintenanceId", 36).references(maintenancesTable.maintenanceId)
    val fieldReportId = varchar("FieldReportId", 36).references(fieldReportsTable.fieldReportId).nullable()
    val completedDate = datetime("CompletedDate").nullable()
    val nextMaintenanceDate = datetime("NextMaintenanceDate").nullable()
    val notes = text("Notes").nullable()
    
    override val primaryKey = PrimaryKey(historyId)
    val indexEquipment = index("idx_equipment_maintenance", equipmentId)
    val indexMaintenanceType = index("idx_maintenance_type", maintenanceId)
}
```

### 5. **User-Department Assignment** ⚠️ MEDIUM PRIORITY
```kotlin
object userDepartmentAssignmentTable : Table("user_department_assignment") {
    val assignmentId = varchar("AssignmentId", 36).default(UUID.randomUUID().toString())
    val userId = varchar("UserId", 36).references(userTable.userId, ReferenceOption.CASCADE)
    val departmentId = varchar("DepartmentId", 36).references(departmentTable.departmentId, ReferenceOption.CASCADE)
    val assignedDate = datetime("AssignedDate").default(CurrentDateTime)
    val isPrimary = bool("IsPrimary").default(false)
    
    override val primaryKey = PrimaryKey(assignmentId)
    val uniqueUserDept = uniqueIndex("unique_user_department", userId, departmentId)
}
```

### 6. **Notification/Alert System** ⚠️ LOW PRIORITY
```kotlin
object notificationsTable : Table("notifications") {
    val notificationId = varchar("NotificationId", 36).default(UUID.randomUUID().toString())
    val userId = varchar("UserId", 36).references(userTable.userId, ReferenceOption.CASCADE)
    val title = varchar("Title", 255)
    val message = text("Message")
    val notificationType = varchar("NotificationType", 50)  // TASK, TICKET, MAINTENANCE, etc.
    val relatedEntityId = varchar("RelatedEntityId", 36).nullable()
    val isRead = bool("IsRead").default(false)
    val createdDate = datetime("CreatedDate").default(CurrentDateTime)
    val readDate = datetime("ReadDate").nullable()
    
    override val primaryKey = PrimaryKey(notificationId)
    val indexUser = index("idx_notification_user", userId)
    val indexUnread = index("idx_unread", isRead, userId)
}
```

### 7. **Department Hierarchy Support** ⚠️ LOW PRIORITY
```kotlin
// Update departmentTable to support parent departments:
object departmentTable : Table("department") {
    // ... existing fields ...
    val parentDepartmentId = varchar("ParentDepartmentId", 36)
        .references(departmentTable.departmentId, ReferenceOption.SET_NULL)
        .nullable()
}
```

---

## � Multi-Platform Sync Considerations (Backend ↔ Android)

Since your backend syncs with an Android SQLite+Room app, here are critical compatibility points:

### **Date Format Consistency**
```kotlin
// BACKEND (Exposed)
val dateCreated = varchar("DateCreated", 50).default(getCurrentDateTime())  // "2024-05-05T14:30:00Z"

// ANDROID (Room)
@Entity
data class TicketEntity(
    @ColumnInfo val dateCreated: String?  // Store same format
)

// TypeConverter for Android:
class Converters {
    @TypeConverter
    fun toDate(dateString: String?): LocalDateTime? =
        dateString?.let { LocalDateTime.parse(it, DateTimeFormatter.ISO_DATE_TIME) }
    
    @TypeConverter
    fun fromDate(date: LocalDateTime?): String? =
        date?.format(DateTimeFormatter.ISO_DATE_TIME)
}
```

### **UUID String Handling**
Your UUID storage as VARCHAR(36) strings works perfectly for sync:
```kotlin
// Both platforms use String IDs - ensures consistency
// Backend: userData.customerId = "550e8400-e29b-41d4-a716-446655440000"
// Android: TicketEntity.customerId = "550e8400-e29b-41d4-a716-446655440000"
```

### **Required Sync Fields in All Tables**
Make sure each table has these fields for sync operations:
```kotlin
val remoteId = integer("RemoteId").nullable()  // ✅ Tracks server-side ID
val version = varchar("Version", 20).nullable()  // ✅ Version control for conflicts
val lastModified = varchar("LastModified", 50).nullable()  // ✅ For sync timestamp
```

### **Recommended: Add Sync Status Field**
```kotlin
// Add to ALL tables that sync:
val syncStatus = varchar("SyncStatus", 20).default("SYNCED")  // SYNCED, PENDING, FAILED, CONFLICT
val lastSyncDate = varchar("LastSyncDate", 50).nullable()

// This helps with conflict resolution and retry logic
```

---

## �🔧 Schema Optimization Recommendations

### 1. **Standardize Audit Fields**
All tables should have consistent audit fields:
```kotlin
val lastModifiedBy = varchar("LastModifiedBy", 36).references(userTable.userId).nullable()
val createdBy = varchar("CreatedBy", 36).references(userTable.userId).nullable()
val lastModified = datetime("LastModified").nullable()
val dateCreated = datetime("DateCreated").default(CurrentDateTime)
```

### 2. **Add UUID Index**
For frequently searched UUIDs (many queries by ID):
```kotlin
val userIdIndex = index("idx_user_id", userId)
```

### 3. **Equipment Model-Manufacturer Relationship**
The equipment table has manufacturer as VARCHAR, should reference manufacturerTable:
```kotlin
object equipmentTable : Table("equipment") {
    // ...
    val manufacturerId = varchar("ManufacturerId", 36)
        .references(manufacturerTable.manufacturerId, ReferenceOption.RESTRICT)
        .nullable()
    val categoryId = varchar("CategoryAssetId", 36)
        .references(categoryAssetTable.categoryAssetId, ReferenceOption.RESTRICT)
        .nullable()
    val modelId = varchar("ModelId", 36)
        .references(modelAssetTable.modelId, ReferenceOption.RESTRICT)
        .nullable()
}
```

### 4. **CheckForms Enhancement**
```kotlin
object checkFormsTable : Table("check_forms") {
    // Add reference to maintenance type
    val maintenanceId = varchar("MaintenanceId", 36)
        .references(maintenancesTable.maintenanceId, ReferenceOption.CASCADE)  // Change from maintenancesId string
    val displayOrder = integer("DisplayOrder").default(0)
    val isRequired = bool("IsRequired").default(true)
}
```

### 5. **Task Assignment to Users/Departments**
```kotlin
// Tasks should link to both user and department, not just ticket
object tasksTable : Table("tasks") {
    // Add missing fields
    val assignedToUserId = varchar("AssignedToUserId", 36)
        .references(userTable.userId, ReferenceOption.SET_NULL)
        .nullable()
    val departmentId = varchar("DepartmentId", 36)
        .references(departmentTable.departmentId, ReferenceOption.SET_NULL)
        .nullable()
}
```

### 6. **Add Soft Deletes** ⚠️ RECOMMENDED
Consider adding soft delete support:
```kotlin
// Add to appropriate tables (not all need it):
val isDeleted = bool("IsDeleted").default(false)
val deletedDate = datetime("DeletedDate").nullable()
val deletedBy = varchar("DeletedBy", 36).nullable()
```

---

## 📊 Query Performance Recommendations

### Critical Indexes to Add:
```sql
-- User lookups
CREATE INDEX idx_user_email ON users(Email);
CREATE INDEX idx_user_phone ON users(Phone);

-- Customer lookups
CREATE INDEX idx_customer_email ON customer(Email);
CREATE INDEX idx_customer_phone ON customer(Phone);

-- Ticket filtering
CREATE INDEX idx_ticket_status ON ticket(Active, CustomerId);
CREATE INDEX idx_ticket_user ON ticket(UserId);
CREATE INDEX idx_ticket_daterange ON ticket(DateStart, DateEnd);

-- Equipment queries
CREATE INDEX idx_equipment_customer ON equipment(CustomerId);
CREATE INDEX idx_equipment_serial ON equipment(SerialNumber);

-- Field reports
CREATE INDEX idx_fieldreport_customer ON fieldreports(CustomerId);
CREATE INDEX idx_fieldreport_status ON fieldreports(ReportStatus);
CREATE INDEX idx_fieldreport_daterange ON fieldreports(StartDate, EndDate);

-- Relationships
CREATE INDEX idx_contract_equipment_contract ON Contract_equipments(ContractId);
CREATE INDEX idx_contract_equipment_equipment ON Contract_equipments(EquipmentId);
CREATE INDEX idx_fieldreport_equipment_report ON fieldreport_equipment(FieldReportId);
```

---

## ✅ Implementation Checklist

- [ ] **Date Format:** Keep current `yyyyMMddHHmm` storage format (verified for consistency)
- [ ] **SYNC CRITICAL**: Add `syncStatus` field to all synced tables
- [ ] Add missing foreign key references
- [ ] Add ReferenceOptions (CASCADE, SET_NULL) to all FKs
- [ ] Create audit log table
- [ ] Create user roles & permissions table
- [ ] Add unique constraints on email, phone, serialNumber fields
- [ ] Create database indexes (see section above)
- [ ] Standardize audit fields across tables
- [ ] Fix equipment model/manufacturer relationships
- [ ] Create user-department assignment table
- [ ] Add equipment-maintenance history table
- [ ] Standardize naming conventions (backend only - don't break Android sync)
- [ ] Add soft delete support (optional but recommended for sync scenarios)
- [ ] Create attachment/document storage table
- [ ] Add notification system table
- [ ] Update department table to support hierarchy

---

## 🔐 Security Recommendations

1. **Add column-level encryption** for sensitive data (signatures, contact info)
2. **Implement row-level security (RLS)** for multi-tenant scenarios
3. **Add data masking** for email/phone in logs
4. **Track user modifications** with `lastModifiedBy` and `createdBy`
5. **Implement soft deletes** for sensitive entities instead of hard deletes

---

## 📝 Notes

- **Multi-Platform Sync:** Your `remoteId` fields indicate SQLite ↔ PostgreSQL/MySQL sync pattern
  - Date format: `yyyyMMddHHmm` for storage (database/sync) ✅
  - Display format: `dd/MM/YYYY` for UI (front-end conversion) ✅
  - This ensures data consistency across backend and Android app
  - String UUIDs work well for cross-platform sync
- Consider partitioning large tables like `audit_logs` and `ticket_history` by date
- Review query patterns for additional composite indexes
- This FSM system appears to support field service reports with equipment tracking
- **Sync Conflict Resolution:** Implement version-based conflict detection using the `version` field and proposed `syncStatus` field

---

**Last Updated:** May 5, 2026  
**Database Type:** Exposed ORM (Kotlin)  
**Status:** Needs optimization
