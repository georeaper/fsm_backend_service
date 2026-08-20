package com.example.feature.equipments.dto

import kotlinx.serialization.Serializable

//@Serializable
//data class CreateEquipmentResponse (
//    val EquipmentID: String,
//    val RemoteID: Int?,
//    val Name: String?,
//    val SerialNumber: String?,
//    val Model: String?,
//    val Manufacturer: String?,
//    val Notes: String?,
//    val Description: String?,
//    val EquipmentVersion: String?,
//    val EquipmentCategory: String?,
//    val Warranty: String?,
//    val EquipmentStatus: Boolean?,
//    val InstallationDate: String?,
//    val LastModified: String?,
//    val DateCreated: String?,
//    val Version: String?,
//    val CustomerID: String?
//    )
@Serializable
data class CreateEquipmentResponse(
    val EquipmentID: String? = null,
    val RemoteID: Int? = null,
    val Name: String,
    val SerialNumber: String,
    val Model: String? = null,
    val Manufacturer: String? = null,
    val Notes: String? = null,
    val Description: String? = null,
    val EquipmentVersion: String? = null,
    val EquipmentCategory: String? = null,
    val Warranty: String? = null,
    val EquipmentStatus: Boolean = true,
    val InstallationDate: String? = null,
    val LastModified: String? = null,
    val DateCreated: String? = null,
    val Version: String = "1",
    val CustomerID: String,
    val isDeleted: Boolean? = false
)
