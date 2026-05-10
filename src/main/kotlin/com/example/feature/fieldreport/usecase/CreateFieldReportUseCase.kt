package com.example.feature.fieldreport.usecase

import com.example.core.RequestContext
import com.example.feature.fieldreport.repository.FieldReportRepository
import com.example.feature.fieldreport.dto.CreateFieldReportResponse
import com.example.models.api.FieldReports
import com.example.core.DateUtils
import java.util.UUID

class CreateFieldReportUseCase (private val repository: FieldReportRepository){
    fun execute(ctx: RequestContext, request: CreateFieldReportResponse) : FieldReports {
        val storageDate = DateUtils.nowStorage()
        val fieldReports = FieldReports(
            FieldReportID = UUID.randomUUID().toString(),
            RemoteID = null,
            ReportNumber = request.ReportNumber,
            Description = request.Description,
            StartDate = DateUtils.uiToStorage(request.StartDate!!),
            EndDate = DateUtils.uiToStorage(request.EndDate!! ),
            Title = request.Title,
            Department = request.Department,
            ClientName = request.ClientName,
            ReportStatus = request.ReportStatus,
            ClientSignature = null,
            Value = request.Value,
            LastModified = storageDate,
            DateCreated = storageDate,
            Version = "1",
            CustomerID = request.CustomerID,
            ContractID = request.ContractID,
            UserID = request.UserID,
            CaseID = request.CaseID
        )
        return repository.save(ctx, fieldReports)
    }
}
