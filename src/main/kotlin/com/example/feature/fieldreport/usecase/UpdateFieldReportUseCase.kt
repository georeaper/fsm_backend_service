package com.example.feature.fieldreport.usecase

import com.example.core.RequestContext
import com.example.feature.fieldreport.dto.EditFieldReportResponse
import com.example.feature.fieldreport.repository.FieldReportRepository

class UpdateFieldReportUseCase(private val repository: FieldReportRepository) {
    fun execute(ctx: RequestContext, input: EditFieldReportResponse): EditFieldReportResponse {
        return repository.edit(ctx, input)
    }
}
