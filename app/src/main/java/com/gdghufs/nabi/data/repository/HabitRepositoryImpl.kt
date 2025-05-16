package com.gdghufs.nabi.data.repository // Replace with your actual package name

import com.gdghufs.nabi.data.datasource.HabitDataSource
import com.gdghufs.nabi.data.model.Habit
import com.gdghufs.nabi.utils.NabiResult
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class HabitRepositoryImpl @Inject constructor(
    private val habitDataSource: HabitDataSource
) : HabitRepository {

    override fun getHabits(userId: String): Flow<NabiResult<List<Habit>>> {
        return habitDataSource.getHabits(userId)
            .map<List<Habit>, NabiResult<List<Habit>>> { habits ->
                // isActive filtering is now done in DataSource, sorting here
                NabiResult.Success(habits.sortedByDescending { it.orderWeight })
            }
            .catch { e -> emit(NabiResult.Error(Exception(e))) }
    }

    override suspend fun updateHabitCompletion(habitId: String, date: String, isCompleted: Boolean): NabiResult<Unit> {
        return try {
            habitDataSource.updateHabitHistory(habitId, date, isCompleted)
            NabiResult.Success(Unit)
        } catch (e: Exception) {
            NabiResult.Error(e)
        }
    }
}