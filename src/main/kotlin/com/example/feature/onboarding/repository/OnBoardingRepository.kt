package com.example.feature.onboarding.repository

import com.example.feature.onboarding.dto.RegisterTenantDto

interface OnBoardingRepository {

    /**
     * Creates company and tenant database/schema. Returns the inserted company id in master DB.
     */
    fun createTenantDB(data: RegisterTenantDto): Int


}