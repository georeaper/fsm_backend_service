package com.example.feature.contracts.dto

import kotlinx.serialization.Serializable

@Serializable
data class ContractsResponse(
    val ContractID: String,
    val RemoteID: Int?,
    val Title: String?,
    val DateStart: String?,
    val DateEnd: String?,
    val Value: Double?,
    val Notes: String?,
    val Description: String?,
    val ContractType: String?,
    val ContractStatus: Boolean?,
    val ContactName: String?,
    val LastModified: String?,
    val DateCreated: String?,
    val Version: String?,
    val CustomerID: String?
)
