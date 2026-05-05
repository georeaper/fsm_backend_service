package com.example.feature.equipments.dto

import kotlinx.serialization.Serializable

@Serializable
data class EditEquipmentResponse (
    val EquipmentID: String,
    val RemoteID: Int?,
    val Name: String?,
    val SerialNumber: String?,
    val Model: String?,
    val Manufacturer: String?,
    val Notes: String?,
    val Description: String?,
    val EquipmentVersion: String?,
    val EquipmentCategory: String?,
    val Warranty: String?,
    val EquipmentStatus: Boolean?,
    val InstallationDate: String?,
    val LastModified: String?,
    val DateCreated: String?,
    val Version: String?,
    val CustomerID: String?,
    val CustomerName : String?
)