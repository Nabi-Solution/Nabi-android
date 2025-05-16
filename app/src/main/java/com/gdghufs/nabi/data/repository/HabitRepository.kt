package com.gdghufs.nabi.data.repository // Replace with your actual package name

import com.gdghufs.nabi.data.model.Habit
import com.gdghufs.nabi.utils.NabiResult
import kotlinx.coroutines.flow.Flow

interface HabitRepository {
    fun getHabits(userId: String): Flow<NabiResult<List<Habit>>>
    suspend fun updateHabitCompletion(habitId: String, date: String, isCompleted: Boolean): NabiResult<Unit>
}