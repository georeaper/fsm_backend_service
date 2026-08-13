package com.example.feature.manufacturer.repository

import com.example.core.RequestContext
import com.example.feature.manufacturer.dto.EditManufacturerResponse
import com.example.feature.manufacturer.dto.ManufacturerResponse
import com.example.models.api.Manufacturer

interface ManufacturerRepository {
    fun save(ctx: RequestContext, data: Manufacturer): Manufacturer
    fun findAll(ctx: RequestContext): List<ManufacturerResponse>
    fun edit(ctx: RequestContext, data: EditManufacturerResponse): EditManufacturerResponse
}
