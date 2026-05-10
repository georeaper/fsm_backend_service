package com.example.feature.fieldreport.usecase

import com.example.core.DateUtils
import com.example.core.RequestContext
import com.example.feature.fieldreport.repository.FieldReportRepository
import com.example.feature.fieldreport.dto.FieldReportsResponse

class GetFieldReportUseCase (private val repository: FieldReportRepository){
    fun execute(ctx: RequestContext) :List<FieldReportsResponse>{
        val data =repository.findAll(ctx)
        return data.map{
            FieldReportsResponse(
                FieldReportID = it.FieldReportID,
                RemoteID = it.RemoteID,
                ReportNumber = it.ReportNumber,
                Description = it.Description,
                StartDate = DateUtils.storageToUi(it.StartDate),
                EndDate = DateUtils.storageToUi(it.EndDate),
                Title = it.Title,
                Department = it.Department,
                ClientName = it.ClientName,
                ReportStatus = it.ReportStatus,
                Value = it.Value,
                LastModified = DateUtils.storageToUi(it.LastModified),
                DateCreated = DateUtils.storageToUi(it.DateCreated),
                Version = it.Version,
                CustomerID = it.CustomerID,
                ContractID = it.ContractID,
                UserID = it.UserID,
                CaseID = it.CaseID
            )
        }
    }
}
