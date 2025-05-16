package com.gdghufs.nabi.data.repository // Replace with your actual package name

import com.gdghufs.nabi.data.model.Medication
import com.gdghufs.nabi.utils.NabiResult
import kotlinx.coroutines.flow.Flow

interface MedicationRepository {
    fun getMedications(userId: String): Flow<NabiResult<List<Medication>>>
    suspend fun updateMedicationCompletion(medicationId: String, date: String, isCompleted: Boolean): NabiResult<Unit>
}