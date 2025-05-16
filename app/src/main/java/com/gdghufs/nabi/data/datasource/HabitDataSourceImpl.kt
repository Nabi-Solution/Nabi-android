package com.gdghufs.nabi.data.datasource // Replace with your actual package name

import com.gdghufs.nabi.data.model.FirebaseConstants
import com.gdghufs.nabi.data.model.Habit
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.snapshots
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
            .whereEqualTo("isActive", true)
            // Firestore doesn't support different orderBy on different properties if you have range/equality filters on others
            // Sorting by orderWeight will be done client-side after fetching active habits.
            // Or, if orderWeight is critical for querying, reconsider indexing strategy.
            // .orderBy("orderWeight", Query.Direction.DESCENDING) // This might require a composite index
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
}