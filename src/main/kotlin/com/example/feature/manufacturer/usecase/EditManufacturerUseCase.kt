package com.example.feature.manufacturer.usecase

import com.example.core.RequestContext
import com.example.feature.manufacturer.repository.ManufacturerRepository
import com.example.feature.manufacturer.dto.EditManufacturerResponse

class EditManufacturerUseCase(private val repository: ManufacturerRepository) {
    fun execute(ctx: RequestContext, input: EditManufacturerResponse): EditManufacturerResponse {
        val manufacturer = EditManufacturerResponse(
            ManufacturerID = input.ManufacturerID,
            RemoteID = input.RemoteID,
            Name = input.Name,
            Style = input.Style,
            LastModified = input.LastModified,
            DateCreated = input.DateCreated,
            Version = input.Version
        )
        return repository.edit(ctx, manufacturer)
    }
}
