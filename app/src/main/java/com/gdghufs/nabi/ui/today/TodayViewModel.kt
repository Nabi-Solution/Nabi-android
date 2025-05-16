package com.gdghufs.nabi.ui.today

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gdghufs.nabi.data.model.Habit
import com.gdghufs.nabi.data.model.Medication
import com.gdghufs.nabi.data.model.TimeOfDay
import com.gdghufs.nabi.data.repository.HabitRepository
import com.gdghufs.nabi.data.repository.MedicationRepository
import com.gdghufs.nabi.data.repository.UserRepository
import com.gdghufs.nabi.utils.DateUtil
import com.gdghufs.nabi.utils.NabiResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import javax.inject.Inject

// Represents a unified item for the LazyColumn
sealed class TodoListItem {
    abstract val id: String
    abstract val name: String
    abstract val timeOfDay: TimeOfDay
    abstract val orderWeight: Int
    abstract val histories: Map<String, Boolean>
    abstract val itemType: ItemType // To help differentiate in UI if needed beyond icon

    data class HabitItem(val habit: Habit) : TodoListItem() {
        override val id: String get() = habit.id
        override val name: String get() = habit.name
        override val timeOfDay: TimeOfDay get() = habit.timeOfDay
        override val orderWeight: Int get() = habit.orderWeight
        override val histories: Map<String, Boolean> get() = habit.histories
        val source: String get() = habit.source
        override val itemType: ItemType = ItemType.HABIT
    }

    data class MedicationItem(val medication: Medication) : TodoListItem() {
        override val id: String get() = medication.id
        override val name: String get() = medication.name
        override val timeOfDay: TimeOfDay get() = medication.timeOfDay
        override val orderWeight: Int get() = medication.orderWeight
        override val histories: Map<String, Boolean> get() = medication.histories
        override val itemType: ItemType = ItemType.MEDICATION
    }
}

enum class ItemType { HABIT, MEDICATION }


data class TodayScreenUiState(
    val todoItems: List<TodoListItem> = emptyList(),
    val medicationItems: List<TodoListItem> = emptyList(),
    val isLoading: Boolean = true,
    val error: String? = null,
    val currentDateString: String = DateUtil.getCurrentDateString(), // "yyyy-MM-dd"
    val displayDateString: String = LocalDate.now().format(DateTimeFormatter.ofPattern("dd MMM")) // e.g., "17 May"
)

@HiltViewModel
class TodayViewModel @Inject constructor(
    private val habitRepository: HabitRepository,
    private val medicationRepository: MedicationRepository,
    private val userRepository: UserRepository // Example: To get current user ID
) : ViewModel() {

    private val _uiState = MutableStateFlow(TodayScreenUiState())
    val uiState: StateFlow<TodayScreenUiState> = _uiState.asStateFlow()

    // In a real app, you'd get the userId from UserRepository after login
    private var currentUserId: String? = null // "test_user_id" // Replace with actual user ID logic

    init {
        viewModelScope.launch {
            // Simulating fetching current user. Replace with actual logic.
            val user = userRepository.getCurrentUser() // Assuming this is a suspend function
            currentUserId = user?.uid

            if (currentUserId == null) {
                _uiState.update { it.copy(isLoading = false, error = "User not logged in") }
                return@launch
            }
            loadTasks(currentUserId!!)
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    private fun loadTasks(userId: String) {
        _uiState.update { it.copy(isLoading = true) }

        val habitsFlow = habitRepository.getHabits(userId)
        val medicationsFlow = medicationRepository.getMedications(userId)

        viewModelScope.launch {
            habitsFlow.combine(medicationsFlow) { habitsResult, medicationsResult ->
                val habits = (habitsResult as? NabiResult.Success)?.data ?: emptyList()
                val medications = (medicationsResult as? NabiResult.Success)?.data ?: emptyList()

                // Check for errors
                val errorMsg = listOf(habitsResult, medicationsResult)
                    .filterIsInstance<NabiResult.Error>()
                    .joinToString(", ") { it.exception.message ?: "Unknown error" }
                    .ifEmpty { null }

                Pair(habits, medications) to errorMsg
            }.collect { (dataPair, error) ->
                val (habits, medications) = dataPair

                val habitTodoItems = habits.map { TodoListItem.HabitItem(it) }
                val medicationTodoItems = medications.map { TodoListItem.MedicationItem(it) }

                _uiState.update {
                    it.copy(
                        todoItems = habitTodoItems.sortedByDescending { item -> item.orderWeight },
                        medicationItems = medicationTodoItems.sortedByDescending { item -> item.orderWeight },
                        isLoading = false,
                        error = error ?: it.error // Keep existing error if new one is null
                    )
                }
            }
        }
    }


    fun toggleCompletion(item: TodoListItem, isCompleted: Boolean) {
        val userId = currentUserId ?: return
        val date = uiState.value.currentDateString

        viewModelScope.launch {
            val result = when (item) {
                is TodoListItem.HabitItem -> habitRepository.updateHabitCompletion(item.id, date, isCompleted)
                is TodoListItem.MedicationItem -> medicationRepository.updateMedicationCompletion(item.id, date, isCompleted)
            }
            if (result is NabiResult.Error) {
                _uiState.update { it.copy(error = result.exception.message ?: "Failed to update status") }
            }
            // Firestore listener will update the UI automatically.
            // If not using listeners, you'd manually refresh or update the local state here.
        }
    }
}