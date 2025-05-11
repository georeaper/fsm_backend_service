package com.example

import com.example.models.api.*
import kotlinx.serialization.json.Json
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic
import kotlinx.serialization.modules.subclass

val synCableModule = SerializersModule {
    polymorphic(SynCable::class) {
        subclass(Customers::class)
        subclass(Users::class)
        subclass(Equipments::class)
        subclass(CategoryAsset::class)
        subclass(CheckForms::class)
        subclass(ContractEquipments::class)
        subclass(Contracts::class)
        subclass(Departments::class)
        subclass(FieldReports::class)
        subclass(FieldReportTools::class)
        subclass(FieldReportInventory::class)
        subclass(FieldReportCheckForm::class)
        subclass(FieldReportEquipment::class)
        subclass(Inventory::class)
        subclass(Maintenances::class)
        subclass(Manufacturer::class)
        subclass(ModelAsset::class)
        subclass(Settings::class)
        subclass(TicketHistory::class)
        subclass(Tickets::class)
        subclass(Tools::class)
        // Later you can add other types here too
    }
}

val json = Json {
    serializersModule = synCableModule
    //classDiscriminator = "type" // will include "type": "Customer"/"Users"/...
    prettyPrint = true
    encodeDefaults = true // Optional: includes default values
    ignoreUnknownKeys = true // Optional: safely ignores extra fields during decoding
}