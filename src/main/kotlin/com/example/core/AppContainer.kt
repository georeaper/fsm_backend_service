package com.example.core

import com.example.feature.customers.usecase.CreateCustomerUseCase
import com.example.feature.customers.repository.CustomerRepositoryImpl
import com.example.feature.customers.usecase.GetCustomersUseCase
import com.example.feature.equipments.usecase.CreateEquipmentUseCase
import com.example.feature.equipments.repository.EquipmentRepository
import com.example.feature.equipments.repository.EquipmentRepositoryImpl
import com.example.feature.equipments.usecase.GetEquipmentUseCase

class AppContainer {
    private val dbProvider by lazy { DatabaseProvider() }

    // repositories
    private val customerRepository by lazy {
        CustomerRepositoryImpl(dbProvider)
    }
    private val equipmentRepository by lazy{
        EquipmentRepositoryImpl(dbProvider)
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