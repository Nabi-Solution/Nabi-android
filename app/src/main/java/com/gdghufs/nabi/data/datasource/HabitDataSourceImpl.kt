package com.gdghufs.nabi.data.source // Or your package

import com.gdghufs.nabi.data.datasource.HabitDataSource
import com.gdghufs.nabi.data.model.FirebaseConstants
import com.gdghufs.nabi.data.model.Habit
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.snapshots
import com.google.firebase.firestore.ktx.toObjects
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class HabitDataSourceImpl @Inject constructor(
    private val firestore: FirebaseFirestore
) : HabitDataSource {

    override fun getHabits(userId: String): Flow<List<Habit>> {
        return firestore.collection(FirebaseConstants.COLLECTION_HABITS)
            .whereEqualTo("userId", userId)
            .whereEqualTo("active", true)
            .snapshots()
            .map { snapshot ->
                snapshot.toObjects(Habit::class.java)
            }
    }

    override suspend fun updateHabitHistory(habitId: String, date: String, isDone: Boolean) {
        firestore.collection(FirebaseConstants.COLLECTION_HABITS)
            .document(habitId)
            .update("histories.$date", isDone)
            .await()
    }

    override suspend fun addHabit(habit: Habit) { // Added
        val docRef = firestore.collection(FirebaseConstants.COLLECTION_HABITS).document()
        // Use the habit object passed, but ensure it has the new ID
        val habitWithId = habit.copy(id = docRef.id)
        docRef.set(habitWithId).await()
    }
}