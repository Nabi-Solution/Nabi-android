package com.gdghufs.nabi.data.source // Or your package

import com.gdghufs.nabi.data.datasource.MedicationDataSource
import com.gdghufs.nabi.data.model.FirebaseConstants
import com.gdghufs.nabi.data.model.Medication
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.snapshots
import com.google.firebase.firestore.ktx.toObjects
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class MedicationDataSourceImpl @Inject constructor(
    private val firestore: FirebaseFirestore
) : MedicationDataSource {

    override fun getMedications(userId: String): Flow<List<Medication>> {
        return firestore.collection(FirebaseConstants.COLLECTION_MEDICATIONS)
            .whereEqualTo("userId", userId)
            .whereEqualTo("active", true)
            .snapshots()
            .map { snapshot ->
                snapshot.toObjects(Medication::class.java)
            }
    }

    override suspend fun updateMedicationHistory(medicationId: String, date: String, isDone: Boolean) {
        firestore.collection(FirebaseConstants.COLLECTION_MEDICATIONS)
            .document(medicationId)
            .update("histories.$date", isDone)
            .await()
    }

    override suspend fun addMedication(medication: Medication) { // Added
        val docRef = firestore.collection(FirebaseConstants.COLLECTION_MEDICATIONS).document()
        val medicationWithId = medication.copy(id = docRef.id)
        docRef.set(medicationWithId).await()
    }
}