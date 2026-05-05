package com.example.feature.equipments

import com.example.core.RequestContext
import com.example.feature.equipments.dto.EditEquipmentResponse
import com.example.feature.equipments.dto.EquipmentsResponse

import com.example.models.api.Equipments

interface EquipmentRepository {
    fun save(ctx: RequestContext,data: Equipments): Equipments
    fun findAll(ctx: RequestContext): List<EquipmentsResponse>
    fun edit(ctx: RequestContext,data: Equipments): EditEquipmentResponse
}