package com.example.core

import com.example.feature.customers.usecase.CreateCustomerUseCase
import com.example.feature.customers.repository.CustomerRepositoryImpl
import com.example.feature.customers.usecase.GetCustomersUseCase

import com.example.feature.equipments.usecase.CreateEquipmentUseCase
import com.example.feature.equipments.repository.EquipmentRepository
import com.example.feature.equipments.repository.EquipmentRepositoryImpl
import com.example.feature.equipments.usecase.GetEquipmentUseCase

import com.example.feature.contracts.usecase.CreateContractUseCase
import com.example.feature.contracts.repository.ContractRepository
import com.example.feature.contracts.repository.ContractRepositoryImpl

import com.example.feature.maintenance.usecase.CreateMaintenanceUseCase
import com.example.feature.maintenance.repository.MaintenanceRepository
import com.example.feature.maintenance.repository.MaintenanceRepositoryImpl

import com.example.feature.tools.usecase.CreateToolUseCase
import com.example.feature.tools.repository.ToolRepository
import com.example.feature.tools.repository.ToolRepositoryImpl

import com.example.feature.user.repository.UserRepositoryImpl
import com.example.feature.user.repository.UserRepository
import com.example.feature.user.usecase.CreateUserUseCase
import com.example.feature.user.usecase.GetUserUseCase 

import com.example.feature.fieldreport.repository.FieldReportRepositoryImpl
import com.example.feature.fieldreport.repository.FieldReportRepository
import com.example.feature.fieldreport.usecase.CreateFieldReportUseCase 
import com.example.feature.fieldreport.usecase.GetFieldReportUseCase

import com.example.feature.ticket.repository.TicketRepositoryImpl
import com.example.feature.ticket.repository.TicketRepository
import com.example.feature.ticket.usecase.CreateTicketUseCase
import com.example.feature.ticket.usecase.GetTicketUseCase

import com.example.feature.settings.repository.SettingsRepositoryImpl 
import com.example.feature.settings.repository.SettingsRepository
import com.example.feature.settings.usecase.CreateSettingsUseCase 
import com.example.feature.settings.usecase.GetSettingsUseCase

import com.example.feature.tasks.repository.TaskRepositoryImpl 
import com.example.feature.tasks.repository.TaskRepository 
import com.example.feature.tasks.usecase.CreateTaskUseCase
import com.example.feature.tasks.usecase.GetTaskUseCase



class AppContainer {
    private val dbProvider by lazy { DatabaseProvider() }

    // repositories
    private val customerRepository by lazy {
        CustomerRepositoryImpl(dbProvider)
    }
    private val equipmentRepository by lazy{
        EquipmentRepositoryImpl(dbProvider)
    }
    private val contractRepository by lazy{
        ContractRepositoryImpl(dbProvider) 
    }
    private val maintenanceRepository by lazy{
        MaintenanceRepositoryImpl(dbProvider) 
    }
    private val toolRepository by lazy{
        ToolRepositoryImpl(dbProvider)  
    }
    private val userRepository by lazy{
        UserRepositoryImpl(dbProvider) 
    }
    private val fieldReportRepository by lazy{
        FieldReportRepositoryImpl(dbProvider) 
    }   
    private val ticketRepository by lazy{
        TicketRepositoryImpl(dbProvider) 
    }
    private val settingRepository by lazy{
        SettingsRepositoryImpl(dbProvider) 
    }
    private val taskRepository by lazy{
        TaskRepositoryImpl(dbProvider) 
    }
    


    // use cases
    val getCustomersUseCase by lazy {
        GetCustomersUseCase(customerRepository)
    }

    val createCustomerUseCase by lazy {
        CreateCustomerUseCase(customerRepository)
    }
    val createEquipmentUseCase by lazy{
        CreateEquipmentUseCase(equipmentRepository)
    }
    val getEquipmentUseCase by lazy{
        GetEquipmentUseCase(equipmentRepository)
    }
}