package com.example.feature.maintenance.dto

import kotlinx.serialization.Serializable

@Serializable
data class Maintenances(
    var MaintenanceID: String,
    var RemoteID: Int?=null,
    var Name: String?=null,
    var Description: String?=null,
    var LastModified: String?=null,
    var DateCreated: String?=null,
    var Version: String?=null
)