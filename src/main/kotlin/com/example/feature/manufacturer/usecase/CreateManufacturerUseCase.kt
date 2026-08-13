package com.example.feature.manufacturer.usecase

import com.example.core.DateUtils
import com.example.core.RequestContext
import com.example.feature.manufacturer.repository.ManufacturerRepository
import com.example.feature.manufacturer.dto.CreateManufacturerResponse
import com.example.models.api.Manufacturer
import java.util.UUID

class CreateManufacturerUseCase (private val repository: ManufacturerRepository
) {
    fun execute(ctx : RequestContext, input: CreateManufacturerResponse): Manufacturer {
        val storageDate = DateUtils.nowStorage()

        val manufacturer = Manufacturer(
            ManufacturerID = UUID.randomUUID().toString(),
            RemoteID = input.RemoteID,
            Name = input.Name,
            Style = input.Style,
            LastModified = storageDate,
            DateCreated = storageDate,
            Version = input.Version
        )
        val saved =repository.save(ctx,manufacturer)
        return saved
    }
}
