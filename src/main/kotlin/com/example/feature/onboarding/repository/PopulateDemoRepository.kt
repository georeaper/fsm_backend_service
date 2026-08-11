package com.example.feature.onboarding.repository

interface PopulateDemoRepository {

    /**
     * Populate the demo data for the specified database.
     */

    fun populateDemoData(databaseName: String)

}