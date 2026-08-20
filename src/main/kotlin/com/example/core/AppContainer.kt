package com.example.core

import com.example.feature.customers.usecase.CreateCustomerUseCase
import com.example.feature.customers.repository.CustomerRepositoryImpl
import com.example.feature.customers.usecase.GetCustomersUseCase
import com.example.feature.customers.usecase.UpdateCustomerUseCase
import com.example.feature.customers.usecase.DeleteCustomerUseCase

import com.example.feature.equipments.usecase.CreateEquipmentUseCase
import com.example.feature.equipments.repository.EquipmentRepository
import com.example.feature.equipments.repository.EquipmentRepositoryImpl
import com.example.feature.equipments.usecase.GetEquipmentUseCase
import com.example.feature.equipments.usecase.UpdateEquipmentUseCase
import com.example.feature.equipments.usecase.DeleteEquipmentUseCase

import com.example.feature.inventory.usecase.CreateInventoryUseCase
import com.example.feature.inventory.repository.InventoryRepository
import com.example.feature.inventory.repository.InventoryRepositoryImpl
import com.example.feature.inventory.usecase.GetInventoryUseCase

import com.example.feature.model.usecase.CreateModelUseCase
import com.example.feature.model.repository.ModelRepository
import com.example.feature.model.repository.ModelRepositoryImpl
import com.example.feature.model.usecase.GetModelUseCase

import com.example.feature.manufacturer.usecase.CreateManufacturerUseCase
import com.example.feature.manufacturer.repository.ManufacturerRepository
import com.example.feature.manufacturer.repository.ManufacturerRepositoryImpl
import com.example.feature.manufacturer.usecase.GetManufacturerUseCase

import com.example.feature.contracts.usecase.CreateContractUseCase
import com.example.feature.contracts.repository.ContractRepository
import com.example.feature.contracts.repository.ContractRepositoryImpl
import com.example.feature.contracts.usecase.GetContractUseCase

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
import com.example.feature.maintenance.usecase.GetMaintenanceUseCase

import com.example.feature.ticket.repository.TicketRepositoryImpl
import com.example.feature.ticket.repository.TicketRepository
import com.example.feature.ticket.usecase.CreateTicketUseCase
import com.example.feature.ticket.usecase.GetTicketUseCase

import com.example.feature.settings.repository.SettingsRepositoryImpl 
import com.example.feature.settings.repository.SettingsRepository
import com.example.feature.settings.usecase.CreateContractTypeUseCase
import com.example.feature.settings.usecase.CreateSettingsUseCase
import com.example.feature.settings.usecase.CreateTechnicalCasePriorityUseCase
import com.example.feature.settings.usecase.CreateWorkOrderTypeUseCase
import com.example.feature.settings.usecase.GetContractTypeUseCase
import com.example.feature.settings.usecase.GetSettingsUseCase
import com.example.feature.settings.usecase.GetTechnicalCasePriorityUseCase
import com.example.feature.settings.usecase.GetWorkOrderTypeUseCase

import com.example.feature.categories.repository.CategoryRepositoryImpl
import com.example.feature.categories.repository.CategoryRepository
import com.example.feature.categories.usecase.CreateCategoryUseCase
import com.example.feature.categories.usecase.GetCategoryUseCase
import com.example.feature.categories.usecase.DeleteCategoryUseCase
import com.example.feature.manufacturer.usecase.DeleteManufacturerUseCase
import com.example.feature.model.usecase.DeleteModelUseCase
import com.example.feature.settings.usecase.DeleteSettingsUseCase

import com.example.feature.tasks.repository.TaskRepositoryImpl 
import com.example.feature.tasks.repository.TaskRepository 
import com.example.feature.tasks.usecase.CreateTaskUseCase
import com.example.feature.tasks.usecase.GetTaskUseCase
import com.example.feature.tools.usecase.GetToolUseCase
import com.example.feature.contracts.usecase.UpdateContractUseCase
import com.example.feature.contracts.usecase.DeleteContractUseCase
import com.example.feature.inventory.usecase.UpdateInventoryUseCase
import com.example.feature.inventory.usecase.DeleteInventoryUseCase
import com.example.feature.maintenance.usecase.UpdateMaintenanceUseCase
import com.example.feature.maintenance.usecase.DeleteMaintenanceUseCase
import com.example.feature.tasks.usecase.UpdateTaskUseCase
import com.example.feature.tasks.usecase.DeleteTaskUseCase
import com.example.feature.ticket.usecase.UpdateTicketUseCase
import com.example.feature.ticket.usecase.DeleteTicketUseCase
import com.example.feature.tools.usecase.UpdateToolUseCase
import com.example.feature.tools.usecase.DeleteToolUseCase
import com.example.feature.fieldreport.usecase.UpdateFieldReportUseCase
import com.example.feature.fieldreport.usecase.DeleteFieldReportUseCase


class AppContainer {
    private val dbProvider by lazy { DatabaseProvider() }

    // repositories
    private val customerRepository by lazy {
        CustomerRepositoryImpl(dbProvider)
    }
    private val equipmentRepository by lazy{
        EquipmentRepositoryImpl(dbProvider)
    }
    private val inventoryRepository by lazy{
        InventoryRepositoryImpl(dbProvider)
    }
    private val modelRepository by lazy{
        ModelRepositoryImpl(dbProvider)
    }
    private val manufacturerRepository by lazy{
        ManufacturerRepositoryImpl(dbProvider)
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
    private val categoryRepository by lazy{
        CategoryRepositoryImpl(dbProvider)
    }



    // use cases
    val getCustomersUseCase by lazy {
        GetCustomersUseCase(customerRepository)
    }

    val createCustomerUseCase by lazy {
        CreateCustomerUseCase(customerRepository)
    }
    val updateCustomerUseCase by lazy {
        UpdateCustomerUseCase(customerRepository)
    }
    val deleteCustomerUseCase by lazy {
        DeleteCustomerUseCase(customerRepository)
    }
    val createEquipmentUseCase by lazy{
        CreateEquipmentUseCase(equipmentRepository)
    }
    val getEquipmentUseCase by lazy{
        GetEquipmentUseCase(equipmentRepository)
    }
    val updateEquipmentUseCase by lazy {
        UpdateEquipmentUseCase(equipmentRepository)
    }
    val deleteEquipmentUseCase by lazy {
        DeleteEquipmentUseCase(equipmentRepository)
    }
    val createInventoryUseCase by lazy{
        CreateInventoryUseCase(inventoryRepository)
    }
    val getInventoryUseCase by lazy{
        GetInventoryUseCase(inventoryRepository)
    }
    val createModelUseCase by lazy{
        CreateModelUseCase(modelRepository)
    }
    val getModelUseCase by lazy{
        GetModelUseCase(modelRepository)
    }
    val createManufacturerUseCase by lazy{
        CreateManufacturerUseCase(manufacturerRepository)
    }
    val getManufacturerUseCase by lazy{
        GetManufacturerUseCase(manufacturerRepository)
    }
    val createContractUseCase by lazy{
        CreateContractUseCase(contractRepository)
    }
    val getContractUseCase by lazy{
        GetContractUseCase(contractRepository)
    }
    val getMaintenanceUseCase by lazy{
        GetMaintenanceUseCase(maintenanceRepository)
    }
    val createMaintenanceUseCase by lazy{
        CreateMaintenanceUseCase(maintenanceRepository)
    }
    val getToolUseCase by lazy{
        GetToolUseCase(toolRepository)
    }
    val createToolUseCase by lazy{
        CreateToolUseCase(toolRepository)
    }
    val getUserUseCase by lazy{
        GetUserUseCase(userRepository)
    }
    val createUserUseCase by lazy{
        CreateUserUseCase(userRepository)
    }
    val getFieldReportUseCase by lazy{
        GetFieldReportUseCase(fieldReportRepository)
    }
    val createFieldReportUseCase by lazy{
        CreateFieldReportUseCase(fieldReportRepository)
    }
    val getTicketUseCase by lazy{
        GetTicketUseCase(ticketRepository)
    }
    val createTicketUseCase by lazy{
        CreateTicketUseCase(ticketRepository)
    }
    val getSettingsUseCase by lazy{
        GetSettingsUseCase(settingRepository)
    }
    val createSettingsUseCase by lazy{
        CreateSettingsUseCase(settingRepository)
    }
    val getTaskUseCase by lazy{
        GetTaskUseCase(taskRepository)
    }
    val createTaskUseCase by lazy{
        CreateTaskUseCase(taskRepository)
    }
    val getTechnicalCasePriorityUseCase by lazy{
        GetTechnicalCasePriorityUseCase(settingRepository)
    }
    val createTechnicalCasePriorityUseCase by lazy{
        CreateTechnicalCasePriorityUseCase(settingRepository)
    }
    val getContractTypeUseCase by lazy{
        GetContractTypeUseCase(settingRepository)
    }
    val getWorkOrderTypeUseCase by lazy{
        GetWorkOrderTypeUseCase(settingRepository)
    }
    val createContractTypeUseCase by lazy{
        CreateContractTypeUseCase(settingRepository)
    }
    val createWorkOrderTypeUseCase by lazy{
        CreateWorkOrderTypeUseCase(settingRepository)
    }
    val getCategoryUseCase by lazy{
        GetCategoryUseCase(categoryRepository)
    }
    val createCategoryUseCase by lazy{
        CreateCategoryUseCase(categoryRepository)
    }
    val deleteCategoryUseCase by lazy{
        DeleteCategoryUseCase(categoryRepository)
    }
    val deleteSettingsUseCase by lazy{
        DeleteSettingsUseCase(settingRepository)
    }
    val deleteModelUseCase by lazy{
        DeleteModelUseCase(modelRepository)
    }
    val deleteManufacturerUseCase by lazy {
        DeleteManufacturerUseCase(manufacturerRepository)
    }
    val updateContractUseCase by lazy { UpdateContractUseCase(contractRepository) }
    val deleteContractUseCase by lazy { DeleteContractUseCase(contractRepository) }
    val updateInventoryUseCase by lazy { UpdateInventoryUseCase(inventoryRepository) }
    val deleteInventoryUseCase by lazy { DeleteInventoryUseCase(inventoryRepository) }
    val updateMaintenanceUseCase by lazy { UpdateMaintenanceUseCase(maintenanceRepository) }
    val deleteMaintenanceUseCase by lazy { DeleteMaintenanceUseCase(maintenanceRepository) }
    val updateTaskUseCase by lazy { UpdateTaskUseCase(taskRepository) }
    val deleteTaskUseCase by lazy { DeleteTaskUseCase(taskRepository) }
    val updateTicketUseCase by lazy { UpdateTicketUseCase(ticketRepository) }
    val deleteTicketUseCase by lazy { DeleteTicketUseCase(ticketRepository) }
    val updateToolUseCase by lazy { UpdateToolUseCase(toolRepository) }
    val deleteToolUseCase by lazy { DeleteToolUseCase(toolRepository) }
    val updateFieldReportUseCase by lazy { UpdateFieldReportUseCase(fieldReportRepository) }
    val deleteFieldReportUseCase by lazy { DeleteFieldReportUseCase(fieldReportRepository) }
}
