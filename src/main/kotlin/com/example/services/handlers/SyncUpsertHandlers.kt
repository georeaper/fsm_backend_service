package com.example.services.handlers


import com.example.models.api.*
import com.example.models.databaseModels.*

import org.jetbrains.exposed.sql.*



fun upsertCustomerData(clientData: List<Customers>) {
        // This function runs inside the same transaction block
        clientData.forEach { customer ->
            val existing = customerTable
                .select { customerTable.customerId eq customer.CustomerID }
                .singleOrNull()

            if (existing == null) {
                customerTable.insert {
                    it[customerId] = customer.CustomerID
                    it[remoteId]=customer.RemoteID
                    it[name] = customer.Name
                    it[phone]=customer.Phone
                    it[email]=customer.Email
                    it[address]=customer.Address
                    it[zipCode]=customer.ZipCode
                    it[city]=customer.City
                    it[notes]=customer.Notes
                    it[description]=customer.Description
                    it[customerStatus]=customer.CustomerStatus
                    it[dateCreated]=customer.DateCreated
                    it[version]=customer.Version
                    it[lastModified] = customer.LastModified
                }
            } else {
                customerTable.update({ customerTable.customerId eq customer.CustomerID }) {
                    it[customerId] = customer.CustomerID
                    it[remoteId]=customer.RemoteID
                    it[name] = customer.Name
                    it[phone]=customer.Phone
                    it[email]=customer.Email
                    it[address]=customer.Address
                    it[zipCode]=customer.ZipCode
                    it[city]=customer.City
                    it[notes]=customer.Notes
                    it[description]=customer.Description
                    it[customerStatus]=customer.CustomerStatus
                    it[dateCreated]=customer.DateCreated
                    it[version]=customer.Version
                    it[lastModified] = customer.LastModified
                }
            }
        }
    }
fun upsertUsersData(clientData: List<Users>) {
    val table = userTable
    val id = userTable.userId

    clientData.forEach { data ->
        val existing = table
            .select { id eq data.UserID }
            .singleOrNull()

        if (existing == null) {
            table.insert {
                it[userId] = data.UserID
                it[remoteId] = data.RemoteID
                it[name] = data.Name
                it[description] = data.Description
                it[email] = data.Email
                it[phone] = data.Phone
                it[signature] = data.Signature
                it[reportPrefix] = data.ReportPrefix
                it[technicalCasePrefix] = data.TechnicalCasePrefix
                it[lastReportNumber] = data.LastReportNumber
                it[lastTCNumber] = data.LastTCNumber
                it[lastModified] = data.LastModified
                it[dateCreated] = data.DateCreated
                it[version] = data.Version
            }
        }
        else{
            table.update ({ table.userId eq data.UserID }  ){
                it[userId] = data.UserID
                it[remoteId] = data.RemoteID
                it[name] = data.Name
                it[description] = data.Description
                it[email] = data.Email
                it[phone] = data.Phone
                it[signature] = data.Signature
                it[reportPrefix] = data.ReportPrefix
                it[technicalCasePrefix] = data.TechnicalCasePrefix
                it[lastReportNumber] = data.LastReportNumber
                it[lastTCNumber] = data.LastTCNumber
                it[lastModified] = data.LastModified
                it[dateCreated] = data.DateCreated
                it[version] = data.Version
            }

        }
    }

}

fun upsertUEquipmentData(clientData: List<Equipments>) {
    val table = equipmentTable
    val id = table.equipmentId

    clientData.forEach { data ->
        val dataID = data.EquipmentID
        val existing = table
            .select { id eq dataID }
            .singleOrNull()

        if (existing == null) {
            table.insert {
                it[id] = dataID
                it[remoteId] = data.RemoteID
                it[name] = data.Name
                it[serialNumber] = data.SerialNumber
                it[model] = data.Model
                it[manufacturer] = data.Manufacturer
                it[notes] = data.Notes
                it[description] = data.Description
                it[equipmentVersion] = data.EquipmentVersion
                it[equipmentCategory] = data.EquipmentCategory
                it[warranty] = data.Warranty
                it[equipmentStatus] = data.EquipmentStatus
                it[installationDate] = data.InstallationDate

                it[customerId] = data.CustomerID

                it[lastModified] = data.LastModified
                it[dateCreated] = data.DateCreated
                it[version] = data.Version
            }
        }
        else{
            table.update ({ id eq dataID }  ){
                it[id] = dataID
                it[remoteId] = data.RemoteID
                it[name] = data.Name
                it[serialNumber] = data.SerialNumber
                it[model] = data.Model
                it[manufacturer] = data.Manufacturer
                it[notes] = data.Notes
                it[description] = data.Description
                it[equipmentVersion] = data.EquipmentVersion
                it[equipmentCategory] = data.EquipmentCategory
                it[warranty] = data.Warranty
                it[equipmentStatus] = data.EquipmentStatus
                it[installationDate] = data.InstallationDate

                it[customerId] = data.CustomerID

                it[lastModified] = data.LastModified
                it[dateCreated] = data.DateCreated
                it[version] = data.Version
            }

        }
    }

}

fun upsertContractsData(clientData: List<Contracts>) {
    val table = contractsTable
    val id = table.contractId

    clientData.forEach { data ->
        val dataID = data.ContractID
        val existing = table
            .select { id eq dataID }
            .singleOrNull()

        if (existing == null) {
            table.insert {
                it[id] = dataID
                it[remoteId] = data.RemoteID
                it[title] = data.Title
                it[dateStart] = data.DateStart
                it[dateEnd] = data.DateEnd
                it[value] = data.Value
                it[description] = data.Description
                it[notes] = data.Notes
                it[contractType] = data.ContractType
                it[contractStatus] = data.ContractStatus
                it[contactName] = data.ContactName


                it[customerId] = data.CustomerID

                it[lastModified] = data.LastModified
                it[dateCreated] = data.DateCreated
                it[version] = data.Version
            }
        }
        else{
            table.update ({ id eq dataID }  ){
                it[id] = dataID
                it[remoteId] = data.RemoteID
                it[title] = data.Title
                it[dateStart] = data.DateStart
                it[dateEnd] = data.DateEnd
                it[value] = data.Value
                it[description] = data.Description
                it[notes] = data.Notes
                it[contractType] = data.ContractType
                it[contractStatus] = data.ContractStatus
                it[contactName] = data.ContactName


                it[customerId] = data.CustomerID

                it[lastModified] = data.LastModified
                it[dateCreated] = data.DateCreated
                it[version] = data.Version
            }

        }
    }
}

fun upsertCategoryAssetData(clientData: List<CategoryAsset>) {
    val table = categoryAssetTable
    val id = table.categoryAssetId

    clientData.forEach { data ->
        val dataID = data.CategoryAssetID
        val existing = table
            .select { id eq dataID }
            .singleOrNull()

        if (existing == null) {
            table.insert {
                it[id] = dataID
                it[remoteId] = data.RemoteID
                it[name] = data.Name
                it[style] = data.Style


                it[lastModified] = data.LastModified
                it[dateCreated] = data.DateCreated
                it[version] = data.Version
            }
        }
        else{
            table.update ({ id eq dataID }  ){
                it[id] = dataID
                it[remoteId] = data.RemoteID
                it[name] = data.Name
                it[style] = data.Style


                it[lastModified] = data.LastModified
                it[dateCreated] = data.DateCreated
                it[version] = data.Version
            }

        }
    }
}

fun upsertCheckFormsData(clientData: List<CheckForms>) {
    val table = checkFormsTable
    val id = table.checkFormId

    clientData.forEach { data ->
        val dataID = data.CheckFormID
        val existing = table
            .select { id eq dataID }
            .singleOrNull()

        if (existing == null) {
            table.insert {
                it[id] = dataID
                it[remoteId] = data.RemoteID
                it[maintenancesId] = data.MaintenancesID
                it[description] = data.Description
                it[valueExpected] = data.ValueExpected
                it[valueType] = data.ValueType



                it[lastModified] = data.LastModified
                it[dateCreated] = data.DateCreated
                it[version] = data.Version
            }
        }
        else{
            table.update ({ id eq dataID }  ){
                it[id] = dataID
                it[remoteId] = data.RemoteID
                it[maintenancesId] = data.MaintenancesID
                it[description] = data.Description
                it[valueExpected] = data.ValueExpected
                it[valueType] = data.ValueType



                it[lastModified] = data.LastModified
                it[dateCreated] = data.DateCreated
                it[version] = data.Version

            }

        }
    }
}

fun upsertContractEquipmentsData(clientData: List<ContractEquipments>) {
    val table = contractEquipmentsTable
    val id = table.contractEquipmentId

    clientData.forEach { data ->
        val dataID = data.ContractEquipmentID
        val existing = table
            .select { id eq dataID }
            .singleOrNull()

        if (existing == null) {
            table.insert {
                it[id] = dataID
                it[remoteId] = data.RemoteID
                it[value] = data.Value
                it[visits] = data.Visits
                it[contractId] = data.ContractID
                it[equipmentId] = data.EquipmentID



                it[lastModified] = data.LastModified
                it[dateCreated] = data.DateCreated
                it[version] = data.Version
            }
        }
        else{
            table.update ({ id eq dataID }  ){
                it[id] = dataID
                it[remoteId] = data.RemoteID
                it[value] = data.Value
                it[visits] = data.Visits
                it[contractId] = data.ContractID
                it[equipmentId] = data.EquipmentID



                it[lastModified] = data.LastModified
                it[dateCreated] = data.DateCreated
                it[version] = data.Version

            }

        }
    }
}

fun upsertDepartmentsData(clientData: List<Departments>) {
    val table = departmentTable
    val id = table.departmentId

    clientData.forEach { data ->
        val dataID = data.DepartmentID
        val existing = table
            .select { id eq dataID }
            .singleOrNull()

        if (existing == null) {
            table.insert {
                it[id] = dataID
                it[remoteId] = data.RemoteID
                it[name] = data.Name
                it[phone] = data.Phone
                it[email] = data.Email
                it[contactPerson] = data.ContactPerson
                it[notes] = data.Notes
                it[description] = data.Description
                it[departmentStatus] = data.DepartmentStatus
                it[customerId] = data.CustomerID

                it[lastModified] = data.LastModified
                it[dateCreated] = data.DateCreated
                it[version] = data.Version
            }
        }
        else{
            table.update ({ id eq dataID }  ){
                it[id] = dataID
                it[remoteId] = data.RemoteID
                it[name] = data.Name
                it[phone] = data.Phone
                it[email] = data.Email
                it[contactPerson] = data.ContactPerson
                it[notes] = data.Notes
                it[description] = data.Description
                it[departmentStatus] = data.DepartmentStatus
                it[customerId] = data.CustomerID

                it[lastModified] = data.LastModified
                it[dateCreated] = data.DateCreated
                it[version] = data.Version
            }

        }
    }
}

fun upsertFieldReportEquipmentData(clientData: List<FieldReportEquipment>) {
    val table = fieldReportEquipmentTable
    val id = table.fieldReportEquipmentID

    clientData.forEach { data ->
        val dataID = data.FieldReportEquipmentID
        val existing = table
            .select { id eq dataID }
            .singleOrNull()

        if (existing == null) {
            table.insert {
                it[id] = dataID
                it[remoteID] = data.RemoteID
                it[completedStatus] = data.CompletedStatus
                it[equipmentID] = data.EquipmentID
                it[fieldReportID] = data.FieldReportID
                it[maintenanceID] = data.MaintenanceID

                it[lastModified] = data.LastModified
                it[dateCreated] = data.DateCreated
                it[version] = data.Version
            }
        }
        else{
            table.update ({ id eq dataID }  ){
                it[id] = dataID
                it[remoteID] = data.RemoteID
                it[completedStatus] = data.CompletedStatus
                it[equipmentID] = data.EquipmentID
                it[fieldReportID] = data.FieldReportID
                it[maintenanceID] = data.MaintenanceID

                it[lastModified] = data.LastModified
                it[dateCreated] = data.DateCreated
                it[version] = data.Version
            }

        }
    }
}

fun upsertFieldReportInventoryData(clientData: List<FieldReportInventory>) {
    val table = fieldReportInventoryTable
    val id = table.fieldReportInventoryId

    clientData.forEach { data ->
        val dataID = data.FieldReportInventoryID
        val existing = table
            .select { id eq dataID }
            .singleOrNull()

        if (existing == null) {
            table.insert {
                it[id] = dataID
                it[remoteID] = data.RemoteID
                it[fieldReportID] = data.FieldReportID
                it[inventoryID] = data.InventoryID

                it[lastModified] = data.LastModified
                it[dateCreated] = data.DateCreated
                it[version] = data.Version
            }
        }
        else{
            table.update ({ id eq dataID }  ){
                it[id] = dataID
                it[remoteID] = data.RemoteID
                it[fieldReportID] = data.FieldReportID
                it[inventoryID] = data.InventoryID

                it[lastModified] = data.LastModified
                it[dateCreated] = data.DateCreated
                it[version] = data.Version
            }

        }
    }
}

fun upsertFieldReportsData(clientData: List<FieldReports>) {
    val table = fieldReportsTable
    val id = table.fieldReportId

    clientData.forEach { data ->
        val dataID = data.FieldReportID
        val existing = table
            .select { id eq dataID }
            .singleOrNull()

        if (existing == null) {
            table.insert {
                it[id] = dataID
                it[remoteID] = data.RemoteID
                it[reportNumber] = data.ReportNumber
                it[description] = data.Description
                it[startDate] = data.StartDate
                it[endDate] = data.EndDate
                it[title] = data.Title
                it[department] = data.Department
                it[clientName] = data.ClientName
                it[reportStatus] = data.ReportStatus
                it[clientSignature] = data.ClientSignature
                it[value] = data.Value

                it[customerID] = data.CustomerID
                it[contractID] = data.ContractID
                it[userID] = data.UserID
                it[caseID] = data.CaseID

                it[lastModified] = data.LastModified
                it[dateCreated] = data.DateCreated
                it[version] = data.Version
            }
        }
        else{
            table.update ({ id eq dataID }  ){
                it[id] = dataID
                it[remoteID] = data.RemoteID
                it[reportNumber] = data.ReportNumber
                it[description] = data.Description
                it[startDate] = data.StartDate
                it[endDate] = data.EndDate
                it[title] = data.Title
                it[department] = data.Department
                it[clientName] = data.ClientName
                it[reportStatus] = data.ReportStatus
                it[clientSignature] = data.ClientSignature
                it[value] = data.Value

                it[customerID] = data.CustomerID
                it[contractID] = data.ContractID
                it[userID] = data.UserID
                it[caseID] = data.CaseID

                it[lastModified] = data.LastModified
                it[dateCreated] = data.DateCreated
                it[version] = data.Version
            }

        }
    }

}

fun upsertFieldReportToolsData(clientData: List<FieldReportTools>) {
    val table = fieldReportToolsTable
    val id = table.fieldReportToolsId

    clientData.forEach { data ->
        val dataID = data.FieldReportToolsID
        val existing = table
            .select { id eq dataID }
            .singleOrNull()

        if (existing == null) {
            table.insert {
                it[id] = dataID
                it[remoteId] = data.RemoteID
                it[fieldReportId] = data.FieldReportID
                it[toolsId] = data.ToolsID


                it[lastModified] = data.LastModified
                it[dateCreated] = data.DateCreated
                it[version] = data.Version
            }
        }
        else{
            table.update ({ id eq dataID }  ){
                it[id] = dataID
                it[remoteId] = data.RemoteID
                it[fieldReportId] = data.FieldReportID
                it[toolsId] = data.ToolsID


                it[lastModified] = data.LastModified
                it[dateCreated] = data.DateCreated
                it[version] = data.Version
            }

        }
    }
}

fun upsertInventoryData(clientData: List<Inventory>) {
    val table = inventoryTable
    val id = table.inventoryId

    clientData.forEach { data ->
        val dataID = data.InventoryID
        val existing = table
            .select { id eq dataID }
            .singleOrNull()

        if (existing == null) {
            table.insert {
                it[id] = dataID
                it[remoteID] = data.RemoteID
                it[title] = data.Title
                it[description] = data.Description
                it[quantity] = data.Quantity
                it[value] = data.Value
                it[type] = data.Type

                it[lastModified] = data.LastModified
                it[dateCreated] = data.DateCreated
                it[version] = data.Version
            }
        }
        else{
            table.update ({ id eq dataID }  ){
                it[id] = dataID
                it[remoteID] = data.RemoteID
                it[title] = data.Title
                it[description] = data.Description
                it[quantity] = data.Quantity
                it[value] = data.Value
                it[type] = data.Type

                it[lastModified] = data.LastModified
                it[dateCreated] = data.DateCreated
                it[version] = data.Version
            }

        }
    }
}

fun upsertMaintenancesData(clientData: List<Maintenances>) {
    val table = maintenancesTable
    val id = table.maintenanceId

    clientData.forEach { data ->
        val dataID = data.MaintenanceID
        val existing = table
            .select { id eq dataID }
            .singleOrNull()

        if (existing == null) {
            table.insert {
                it[id] = dataID
                it[remoteID] = data.RemoteID
                it[name] = data.Name
                it[description] = data.Description

                it[lastModified] = data.LastModified
                it[dateCreated] = data.DateCreated
                it[version] = data.Version
            }
        }
        else{
            table.update ({ id eq dataID }  ){
                it[id] = dataID
                it[remoteID] = data.RemoteID
                it[name] = data.Name
                it[description] = data.Description

                it[lastModified] = data.LastModified
                it[dateCreated] = data.DateCreated
                it[version] = data.Version
            }

        }
    }
}

fun upsertManufacturerData(clientData: List<Manufacturer>) {
    val table = manufacturerTable
    val id = table.manufacturerId

    clientData.forEach { data ->
        val dataID = data.ManufacturerID
        val existing = table
            .select { id eq dataID }
            .singleOrNull()

        if (existing == null) {
            table.insert {
                it[id] = dataID
                it[remoteId] = data.RemoteID
                it[name] = data.Name
                it[style] = data.Style

                it[lastModified] = data.LastModified
                it[dateCreated] = data.DateCreated
                it[version] = data.Version
            }
        }
        else{
            table.update ({ id eq dataID }  ){
                it[id] = dataID
                it[remoteId] = data.RemoteID
                it[name] = data.Name
                it[style] = data.Style

                it[lastModified] = data.LastModified
                it[dateCreated] = data.DateCreated
                it[version] = data.Version
            }

        }
    }
}

fun upsertModelAssetData(clientData: List<ModelAsset>) {
    val table = modelAssetTable
    val id = table.modelId

    clientData.forEach { data ->
        val dataID = data.ModelID
        val existing = table
            .select { id eq dataID }
            .singleOrNull()

        if (existing == null) {
            table.insert {
                it[id] = dataID
                it[remoteId] = data.RemoteID
                it[name] = data.Name
                it[style] = data.Style

                it[lastModified] = data.LastModified
                it[dateCreated] = data.DateCreated
                it[version] = data.Version
            }
        }
        else{
            table.update ({ id eq dataID }  ){
                it[id] = dataID
                it[remoteId] = data.RemoteID
                it[name] = data.Name
                it[style] = data.Style

                it[lastModified] = data.LastModified
                it[dateCreated] = data.DateCreated
                it[version] = data.Version
            }

        }
    }
}

fun upsertSettingsData(clientData: List<Settings>) {
    val table = settingsTable
    val id = table.settingsId

    clientData.forEach { data ->
        val dataID = data.SettingsID
        val existing = table
            .select { id eq dataID }
            .singleOrNull()

        if (existing == null) {
            table.insert {
                it[id] = dataID
                it[remoteId] = data.RemoteID
                it[settingsKey] = data.SettingsKey
                it[settingsValue] = data.SettingsValue
                it[settingsStyle] = data.SettingsStyle
                it[settingsDescription] = data.SettingsDescription

                it[lastModified] = data.LastModified
                it[dateCreated] = data.DateCreated
                it[version] = data.Version
            }
        }
        else{
            table.update ({ id eq dataID }  ){
                it[id] = dataID
                it[remoteId] = data.RemoteID
                it[settingsKey] = data.SettingsKey
                it[settingsValue] = data.SettingsValue
                it[settingsStyle] = data.SettingsStyle
                it[settingsDescription] = data.SettingsDescription

                it[lastModified] = data.LastModified
                it[dateCreated] = data.DateCreated
                it[version] = data.Version
            }

        }
    }
}

fun upsertTasksData(clientData: List<Tasks>) {
    val table = tasksTable
    val id = table.taskId

    clientData.forEach { data ->
        val dataID = data.TaskID
        val existing = table
            .select { id eq dataID }
            .singleOrNull()

        if (existing == null) {
            table.insert {
                it[id] = dataID
                //it[remoteID] = data.RemoteID
                it[title] = data.Title
                it[description] = data.Description
                it[status] = data.Status
                it[priority] = data.Priority
                it[dateStart] = data.DateStart
                it[dateDue] = data.DateDue
                it[dateCompleted] = data.DateCompleted

                it[ticketId] = data.TicketID
                it[userId] = data.UserID

                it[lastModified] = data.LastModified
                it[dateCreated] = data.DateCreated
                //it[version] = data.Version
            }
        }
        else{
            table.update ({ id eq dataID }  ){
                it[id] = dataID
                //it[remoteID] = data.RemoteID
                it[title] = data.Title
                it[description] = data.Description
                it[status] = data.Status
                it[priority] = data.Priority
                it[dateStart] = data.DateStart
                it[dateDue] = data.DateDue
                it[dateCompleted] = data.DateCompleted

                it[ticketId] = data.TicketID
                it[userId] = data.UserID

                it[lastModified] = data.LastModified
                it[dateCreated] = data.DateCreated
                //it[version] = data.Version
            }

        }
    }
}

fun upsertTicketHistoryData(clientData: List<TicketHistory>) {
    val table = ticketHistoryTable
    val id = table.ticketHistoryId

    clientData.forEach { data ->
        val dataID = data.TicketHistoryID
        val existing = table
            .select { id eq dataID }
            .singleOrNull()

        if (existing == null) {
            table.insert {
                it[id] = dataID
                it[remoteId] = data.RemoteID
                it[title] = data.Title
                it[ticketNumber] = data.TicketNumber
                it[description] = data.Description
                it[notes] = data.Notes
                it[urgency] = data.Urgency
                it[dateStart] = data.DateStart
                it[dateEnd] = data.DateEnd
                it[active] = data.Active

                it[customerId] = data.CustomerID
                it[userId] = data.UserID
                it[equipmentId] = data.EquipmentID

                it[lastModified] = data.LastModified
                it[dateCreated] = data.DateCreated
                it[version] = data.Version
            }
        }
        else{
            table.update ({ id eq dataID }  ){
                it[id] = dataID
                it[remoteId] = data.RemoteID
                it[title] = data.Title
                it[ticketNumber] = data.TicketNumber
                it[description] = data.Description
                it[notes] = data.Notes
                it[urgency] = data.Urgency
                it[dateStart] = data.DateStart
                it[dateEnd] = data.DateEnd
                it[active] = data.Active

                it[customerId] = data.CustomerID
                it[userId] = data.UserID
                it[equipmentId] = data.EquipmentID

                it[lastModified] = data.LastModified
                it[dateCreated] = data.DateCreated
                it[version] = data.Version
            }

        }
    }
}

fun upsertTicketsData(clientData: List<Tickets>) {
    val table = ticketTable
    val id = table.ticketId

    clientData.forEach { data ->
        val dataID = data.TicketID
        val existing = table
            .select { id eq dataID }
            .singleOrNull()

        if (existing == null) {
            table.insert {
                it[id] = dataID
                it[remoteId] = data.RemoteID
                it[title] = data.Title
                it[ticketNumber] = data.TicketNumber
                it[description] = data.Description
                it[notes] = data.Notes
                it[urgency] = data.Urgency
                it[dateStart] = data.DateStart
                it[dateEnd] = data.DateEnd
                it[active] = data.Active

                it[customerId] = data.CustomerID
                it[userId] = data.UserID
                it[equipmentId] = data.EquipmentID

                it[lastModified] = data.LastModified
                it[dateCreated] = data.DateCreated
                it[version] = data.Version
            }
        }
        else{
            table.update ({ id eq dataID }  ){
                it[id] = dataID
                it[remoteId] = data.RemoteID
                it[title] = data.Title
                it[ticketNumber] = data.TicketNumber
                it[description] = data.Description
                it[notes] = data.Notes
                it[urgency] = data.Urgency
                it[dateStart] = data.DateStart
                it[dateEnd] = data.DateEnd
                it[active] = data.Active

                it[customerId] = data.CustomerID
                it[userId] = data.UserID
                it[equipmentId] = data.EquipmentID

                it[lastModified] = data.LastModified
                it[dateCreated] = data.DateCreated
                it[version] = data.Version
            }

        }
    }
}

fun upsertToolsData(clientData: List<Tools>) {
    val table = toolsTable
    val id = table.toolsId

    clientData.forEach { data ->
        val dataID = data.ToolsID
        val existing = table
            .select { id eq dataID }
            .singleOrNull()

        if (existing == null) {
            table.insert {
                it[id] = dataID
                it[remoteId] = data.RemoteID
                it[title] = data.Title
                it[description] = data.Description
                it[model] = data.Model
                it[manufacturer] = data.Manufacturer
                it[serialNumber] = data.SerialNumber
                it[calibrationDate] = data.CalibrationDate

                it[lastModified] = data.LastModified
                it[dateCreated] = data.DateCreated
                it[version] = data.Version
            }
        }
        else{
            table.update ({ id eq dataID }  ){
                it[id] = dataID
                it[remoteId] = data.RemoteID
                it[title] = data.Title
                it[description] = data.Description
                it[model] = data.Model
                it[manufacturer] = data.Manufacturer
                it[serialNumber] = data.SerialNumber
                it[calibrationDate] = data.CalibrationDate

                it[lastModified] = data.LastModified
                it[dateCreated] = data.DateCreated
                it[version] = data.Version
            }

        }
    }
}
fun upsertFieldReportCheckFormData(clientData: List<FieldReportCheckForm>) {
    val table = fieldReportCheckformTable
    val id = table.fieldReportCheckFormId

    clientData.forEach { data ->
        val dataID = data.FieldReportCheckFormID
        val existing = table
            .select { id eq dataID }
            .singleOrNull()

        if (existing == null) {
            table.insert {
                it[id] = dataID
                it[remoteId] = data.RemoteID
                it[fieldReportEquipmentId] = data.FieldReportEquipmentID
                it[description] = data.Description
                it[valueExpected] = data.ValueExpected
                it[valueMeasured] = data.ValueMeasured
                it[result] = data.Result

                it[lastModified] = data.LastModified
                it[dateCreated] = data.DateCreated
                it[version] = data.Version
            }
        }
        else{
            table.update ({ id eq dataID }  ){
                it[id] = dataID
                it[remoteId] = data.RemoteID
                it[fieldReportEquipmentId] = data.FieldReportEquipmentID
                it[description] = data.Description
                it[valueExpected] = data.ValueExpected
                it[valueMeasured] = data.ValueMeasured
                it[result] = data.Result

                it[lastModified] = data.LastModified
                it[dateCreated] = data.DateCreated
                it[version] = data.Version
            }

        }
    }
}

