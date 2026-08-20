package com.example.feature.fieldreport.usecase

import com.example.core.RequestContext
import com.example.feature.fieldreport.repository.FieldReportRepository

class DeleteFieldReportUseCase(private val repository: FieldReportRepository) {
    fun execute(ctx: RequestContext, id: String): Boolean {
        return repository.delete(ctx, id)
    }
}
