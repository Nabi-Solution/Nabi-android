package com.gdghufs.nabi.data.repository // Replace with your actual package name

import com.gdghufs.nabi.data.datasource.MedicationDataSource
import com.gdghufs.nabi.data.model.Medication
import com.gdghufs.nabi.utils.NabiResult
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class MedicationRepositoryImpl @Inject constructor(
    private val medicationDataSource: MedicationDataSource
) : MedicationRepository {

    override fun getMedications(userId: String): Flow<NabiResult<List<Medication>>> {
        return medicationDataSource.getMedications(userId)
            .map<List<Medication>, NabiResult<List<Medication>>> { medications ->
                // isActive filtering is now done in DataSource, sorting here
                NabiResult.Success(medications.sortedByDescending { it.orderWeight })
            }
            .catch { e -> emit(NabiResult.Error(Exception(e))) }
    }

    override suspend fun updateMedicationCompletion(medicationId: String, date: String, isCompleted: Boolean): NabiResult<Unit> {
        return try {
            medicationDataSource.updateMedicationHistory(medicationId, date, isCompleted)
            NabiResult.Success(Unit)
        } catch (e: Exception) {
            NabiResult.Error(e)
        }
    }
}