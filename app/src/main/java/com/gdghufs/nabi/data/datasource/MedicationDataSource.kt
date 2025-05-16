package com.gdghufs.nabi.data.datasource // Replace with your actual package name

import com.gdghufs.nabi.data.model.Medication
import kotlinx.coroutines.flow.Flow

interface MedicationDataSource {
    fun getMedications(userId: String): Flow<List<Medication>>
    suspend fun updateMedicationHistory(medicationId: String, date: String, isDone: Boolean)
    suspend fun addMedication(medication: Medication) // Added
}