package com.example.models.api

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import java.util.*

@Serializable
sealed interface SynCable

@Serializable
@SerialName("Customer")
data class Customers(var CustomerID : String,var RemoteID : Int? =null,var Name :String? =null,var Phone :String? =null,var Email :String? =null,var Address :String? =null, var ZipCode :String? =null,var City :String? =null,var Notes :String? =null,var Description :String? =null,var CustomerStatus :Boolean? =null,var LastModified :String? =null ,var DateCreated :String? =null,var Version :String?=null): SynCable

@Serializable
@SerialName("User")
data class Users(var UserID :String,
                 var RemoteID :Int?=null,
                 var Name :String?=null,
                 var Description :String?=null,
                 var Email :String?=null,
                 var Phone :String?=null,
                 val Signature: ByteArray? = null,
                 var ReportPrefix :String?=null,
                 var TechnicalCasePrefix :String?=null,
                 var LastReportNumber :Int?=null,
                 var LastTCNumber :Int?=null,
                 var LastModified :String?=null,
                 var DateCreated :String?=null,
                 var Version :String?=null):SynCable

@Serializable
@SerialName("Equipment")
data class Equipments(var EquipmentID :String ,var RemoteID :Int? =null,var Name :String? =null,var SerialNumber :String? =null,var Model :String?=null ,var Manufacturer :String? =null,var Notes :String?=null ,var Description :String?=null ,var EquipmentVersion :String?=null ,var EquipmentCategory :String? =null,var Warranty :String? =null,var EquipmentStatus :Boolean?=null,var InstallationDate :String? =null,var LastModified :String? =null,var DateCreated :String? =null,var Version :String? =null,var CustomerID :String? =null):SynCable

@Serializable
@SerialName("CategoryAsset")
data class CategoryAsset  (var CategoryAssetID: String ,
var RemoteID: Int? =null,
var Name: String?=null,
var Style: String?=null,
var LastModified: String?=null, var DateCreated: String?=null,
var Version: String?=null ):SynCable

@Serializable
@SerialName("CheckForms")
data class CheckForms(
    var CheckFormID: String ,
    var RemoteID: Int?=null,
    var MaintenancesID: String?=null,
    var Description: String?=null,
    var ValueExpected: String?=null,
    var ValueType: String?=null, //checkbox, Textview, Edittext, etc
    var LastModified: String?=null,
    var DateCreated: String?=null,
    var Version: String? =null ):SynCable

@Serializable
@SerialName("ContractEquipments")
data class ContractEquipments(
    var ContractEquipmentID: String ,
    var RemoteID: Int?=null,
    var Value: Double?=null,
    var Visits: Double?=null,
    var ContractID: String?=null,
    var EquipmentID: String?=null,
    var LastModified: String?=null,
    var DateCreated: String?=null,
    var Version: String?=null ):SynCable

@Serializable
@SerialName("Contracts")
data class Contracts(
    var ContractID: String ,
    var RemoteID: Int?=null,
    var Title: String?=null,
    var DateStart: String?=null,
    var DateEnd: String?=null,
    var Value: Double?=null,
    var Notes: String?=null,
    var Description: String?=null,
    var ContractType: String?=null,
    var ContractStatus: Boolean?=null,
    var ContactName: String?=null,
    var LastModified: String?=null,
    var DateCreated: String?=null,
    var Version: String?=null,
    var CustomerID: String?=null ):SynCable

@Serializable
@SerialName("Departments")
data class Departments(
    var DepartmentID: String ,
    var RemoteID: Int?=null,
    var Name: String?=null,
    var Phone: String?=null,
    var Email: String?=null,
    var ContactPerson: String?=null,
    var Notes: String?=null,
    var Description: String?=null,
    var DepartmentStatus: Boolean?=null,
    var LastModified: String?=null,
    var DateCreated: String?=null,
    var Version: String?=null,
    var CustomerID: String?=null
):SynCable

@Serializable
@SerialName("FieldReportCheckForm")
data class FieldReportCheckForm(
    var FieldReportCheckFormID: String ,
    var RemoteID: Int?=null,
    var FieldReportEquipmentID: String?=null,
    var Description: String?=null,
    var ValueExpected: String?=null,
    var ValueMeasured: String?=null,
    var Result: String?=null,
    var LastModified: String?=null,
    var DateCreated: String?=null,
    var Version: String?=null
):SynCable
@Serializable
@SerialName("FieldReportEquipment")
data class FieldReportEquipment(
    var FieldReportEquipmentID: String ,
    var RemoteID: Int?=null,
    var CompletedStatus: Boolean?=null,
    var LastModified: String?=null,
    var DateCreated: String?=null,
    var Version: String?=null,
    var FieldReportID: String?=null,
    var EquipmentID: String?=null,
    var MaintenanceID: String?=null
):SynCable
@Serializable
@SerialName("FieldReportInventory")
data class FieldReportInventory(
    var FieldReportInventoryID: String,
    var RemoteID: Int?=null,
    var LastModified: String?=null,
    var DateCreated: String?=null,
    var Version: String?=null,
    var FieldReportID: String?=null,
    var InventoryID: String?=null
):SynCable
@Serializable
@SerialName("FieldReports")
data class FieldReports(
    var FieldReportID: String,
    var RemoteID: Int?=null,
    var ReportNumber: String?=null,
    var Description: String?=null,
    var StartDate: String?=null,
    var EndDate: String?=null,
    var Title: String?=null,
    var Department: String?=null,
    var ClientName: String?=null,
    var ReportStatus: String?=null,
    var ClientSignature: ByteArray?=null,
    var Value: Double?=null,
    var LastModified: String?=null,
    var DateCreated: String?=null,
    var Version: String?=null,
    var CustomerID: String?=null,
    var ContractID: String?=null,
    var UserID: String?=null,
    var CaseID: String? = null
):SynCable
@Serializable
@SerialName("FieldReportTools")
data class FieldReportTools(
    var FieldReportToolsID: String ,
    var RemoteID: Int?=null,
    var FieldReportID:String?=null,
    var ToolsID: String?=null,
    var LastModified: String?=null,
    var DateCreated: String?=null,
    var Version: String?=null
):SynCable
@Serializable
@SerialName("Inventory")
data class Inventory(
    var InventoryID: String ,
    var RemoteID: Int?=null,
    var Title: String?=null,
    var Description: String?=null,
    var Quantity: Long?=null,
    var Value: Double?=null,
    var Type: String?=null,
    var LastModified: String?=null,
    var DateCreated: String?=null,
    var Version: String?=null
):SynCable
@Serializable
@SerialName("Maintenances")
data class Maintenances(
    var MaintenanceID: String,
    var RemoteID: Int?=null,
    var Name: String?=null,
    var Description: String?=null,
    var LastModified: String?=null,
    var DateCreated: String?=null,
   var Version: String?=null
):SynCable
@Serializable
@SerialName("Manufacturer")
data class Manufacturer(
    var ManufacturerID: String ,
    var RemoteID: Int?=null,
    var Name: String?=null,
    var Style: String?=null,
    var LastModified: String?=null,
    var DateCreated: String?=null,
    var Version: String?=null
):SynCable
@Serializable
@SerialName("ModelAsset")
data class ModelAsset(
    var ModelID: String ,
    var RemoteID: Int?=null,
    var Name: String?=null,
    var Style: String?=null,
    var LastModified: String?=null,
    var DateCreated: String?=null,
    var Version: String?=null
):SynCable
@Serializable
@SerialName("Settings")
data class Settings(
    var SettingsID: String ,
    var RemoteID: Int?=null,
    var SettingsKey: String?=null,
    var SettingsValue: String?=null,
    var SettingsStyle: String?=null,
    var SettingsDescription : String ?=null ,
    var LastModified: String?=null,
    var DateCreated: String?=null,
    var Version: String?=null
):SynCable
@Serializable
@SerialName("Tasks")
data class Tasks(
    var TaskID: String ,
    var Title: String?=null,
    var Description: String?=null,
    var Status: String?=null, // E.g., "Pending", "In Progress", "Completed"
    var Priority: String?=null, // E.g., "Low", "Medium", "High"
    var DateStart: String?=null,
    var DateDue: String?=null,
    var DateCompleted: String?=null,
    var LastModified: String?=null,
    var DateCreated: String?=null,
    var TicketID: String?=null, // Links task to a ticket
    var UserID: String? =null// Assign task to a user
):SynCable
@Serializable
@SerialName("TicketHistory")
data class TicketHistory(
    var TicketHistoryID: String ,
    var RemoteID: Int?=null,
    var Title: String?=null,
    var TicketNumber: String?=null,
    var Description: String?=null,
    var Notes: String?=null,
    var Urgency: String?=null,
    var Active: Boolean?=null,
    var DateStart: String?=null,
    var DateEnd: String?=null,
    var LastModified: String?=null,
    var DateCreated: String?=null,
    var Version: String?=null,
    var UserID: String?=null,
    var CustomerID: String?=null,
    var EquipmentID: String?=null
):SynCable
@Serializable
@SerialName("Tickets")
data class Tickets(
    var TicketID: String ,
    var RemoteID: Int?=null,
    var Title: String?=null,
    var TicketNumber: String?=null,
    var Description: String?=null,
    var Notes: String?=null,
    var Urgency: String?=null,
    var Active: Boolean?=null,
    var DateStart: String?=null,
    var DateEnd: String?=null,
    var LastModified: String?=null,
    var DateCreated: String?=null,
    var Version: String?=null,
    var UserID: String?=null,
    var CustomerID: String?=null,
    var EquipmentID: String?=null
):SynCable
@Serializable
@SerialName("Tools")
data class Tools(
    var ToolsID: String ,
    var RemoteID: Int?=null,
    var Title: String?=null,
    var Description: String?=null,
    var Model: String?=null,
    var Manufacturer: String?=null,
    var SerialNumber: String?=null,
    var CalibrationDate: String?=null,
    var LastModified: String?=null,
    var DateCreated: String?=null,
    var Version: String?=null
):SynCable
