package com.example.feature.maintenance.dto


import kotlinx.serialization.Serializable

@Serializable
data class Checkforms(
    var CheckFormID: String ,
    var RemoteID: Int?=null,
    var MaintenancesID: String?=null,
    var Description: String?=null,
    var ValueExpected: String?=null,
    var ValueType: String?=null, //checkbox, Textview, Edittext, etc
    var LastModified: String?=null,
    var DateCreated: String?=null,
    var Version: String? =null )