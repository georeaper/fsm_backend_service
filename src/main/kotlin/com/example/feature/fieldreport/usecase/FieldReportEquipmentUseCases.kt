package com.example.feature.fieldreport.usecase

import com.example.core.RequestContext
import com.example.feature.fieldreport.dto.FieldReportEquipment
import com.example.feature.fieldreport.repository.FieldReportEquipmentRepository

class CreateFieldReportEquipmentUseCase(private val repository: FieldReportEquipmentRepository) {
    fun execute(ctx: RequestContext, fieldReportId: String, input: FieldReportEquipment) =
        repository.save(ctx, fieldReportId, input)
}

class UpdateFieldReportEquipmentUseCase(private val repository: FieldReportEquipmentRepository) {
    fun execute(ctx: RequestContext, fieldReportId: String, id: String, input: FieldReportEquipment) =
        repository.update(ctx, fieldReportId, id, input)
}

class DeleteFieldReportEquipmentUseCase(private val repository: FieldReportEquipmentRepository) {
    fun execute(ctx: RequestContext, fieldReportId: String, id: String) =
        repository.delete(ctx, fieldReportId, id)
}
