package com.example.feature.onboarding.usecase

import com.example.feature.onboarding.repository.OnBoardingRepository
import com.example.feature.onboarding.repository.PopulateDemoRepository
import com.example.feature.onboarding.repository.PopulateDemoRepositoryImpl
import kotlinx.serialization.Serializable

@Serializable
data class PopulateDemoData(val databaseName: String)

class PopulateDemoUseCase (private val demoRepository: PopulateDemoRepository) {

    /**
     * Executes demo data population flow.
     * Throws exception on validation or DB errors.
     */
    fun execute(databaseName: String) {
        // delegate the heavy lifting to repository (inserts & tenant creation)
        demoRepository.populateDemoData(databaseName)
    }

}