package com.example.core

import com.example.feature.customers.CustomerRepositoryImpl
import com.example.feature.customers.GetCustomersUseCase

class AppContainer {
    private val dbProvider by lazy { DatabaseProvider() }

    // repositories
    private val customerRepository by lazy {
        CustomerRepositoryImpl(dbProvider)
    }

    // use cases
    val getCustomersUseCase by lazy {
        GetCustomersUseCase(customerRepository)
    }

//    val createCustomerUseCase by lazy {
//        CreateCustomerUseCase(customerRepository)
//    }
}