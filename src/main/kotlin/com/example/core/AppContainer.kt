package com.example.core

import com.example.feature.customers.CreateCustomerUseCase
import com.example.feature.customers.CustomerRepositoryImpl
import com.example.feature.customers.GetCustomersUseCase
import com.example.feature.equipments.CreateEquipmentUseCase
import com.example.feature.equipments.EquipmentRepository
import com.example.feature.equipments.EquipmentRepositoryImpl
import com.example.feature.equipments.GetEquipmentUseCase

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