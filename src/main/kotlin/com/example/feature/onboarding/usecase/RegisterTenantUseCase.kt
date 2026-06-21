package com.example.feature.onboarding.usecase

import com.example.feature.onboarding.dto.RegisterTenantDto
import com.example.feature.onboarding.repository.OnBoardingRepository
import kotlinx.serialization.Serializable

@Serializable
data class RegisterTenantResult(val companyId: Int, val databaseName: String)

class RegisterTenantUseCase(private val repository: OnBoardingRepository) {

	/**
	 * Executes tenant registration flow.
	 * Throws exception on validation or DB errors.
	 */
	fun execute(data: RegisterTenantDto): RegisterTenantResult {
		// delegate the heavy lifting to repository (inserts & tenant creation)
		val companyId = repository.createTenantDB(data)

		val databaseName = data.name.trim().lowercase().replace("\\s+".toRegex(), "_") + "_db"
		return RegisterTenantResult(companyId, databaseName)
	}

}