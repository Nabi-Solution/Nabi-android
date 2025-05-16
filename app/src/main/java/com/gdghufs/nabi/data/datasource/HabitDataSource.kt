package com.gdghufs.nabi.data.datasource // Replace with your actual package name

import com.gdghufs.nabi.data.model.Habit
import kotlinx.coroutines.flow.Flow

interface HabitDataSource {
    fun getHabits(userId: String): Flow<List<Habit>>
    suspend fun updateHabitHistory(habitId: String, date: String, isDone: Boolean)
    suspend fun addHabit(habit: Habit) // Added
}