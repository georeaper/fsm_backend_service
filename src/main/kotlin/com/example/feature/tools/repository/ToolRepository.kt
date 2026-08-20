package com.example.feature.tools.repository

import com.example.core.RequestContext
import com.example.feature.tools.dto.EditToolResponse
import com.example.feature.tools.dto.ToolsResponse
import com.example.models.api.Tools

interface ToolRepository {
    fun save(ctx: RequestContext, data: Tools): Tools
    fun findAll(ctx: RequestContext): List<ToolsResponse>
    fun edit(ctx: RequestContext, data: EditToolResponse): EditToolResponse
    fun delete(ctx: RequestContext, id: String): Boolean
}
