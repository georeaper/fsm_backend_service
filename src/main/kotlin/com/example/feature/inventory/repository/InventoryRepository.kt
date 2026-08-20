package com.example.feature.inventory.repository

import com.example.core.RequestContext
import com.example.feature.inventory.dto.EditInventoryResponse
import com.example.feature.inventory.dto.InventoryResponse
import com.example.models.api.Inventory

interface InventoryRepository {
    fun save(ctx: RequestContext, data: Inventory): Inventory
    fun findAll(ctx: RequestContext): List<InventoryResponse>
    fun edit(ctx: RequestContext, data: EditInventoryResponse): EditInventoryResponse
    fun delete(ctx: RequestContext, id: String): Boolean
}
