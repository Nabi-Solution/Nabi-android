package com.gdghufs.nabi.ui.today

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gdghufs.nabi.data.model.DummySuggestions
import com.gdghufs.nabi.data.model.Habit
import com.gdghufs.nabi.data.model.Medication
import com.gdghufs.nabi.data.model.Suggestion
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
    val suggestions: List<Suggestion> = emptyList(), // Added
    val isLoading: Boolean = true,
    val error: String? = null,
    val currentDateString: String = DateUtil.getCurrentDateString(),
    val displayDateString: String = LocalDate.now().format(DateTimeFormatter.ofPattern("dd MMM"))
)

@HiltViewModel
class TodayViewModel @Inject constructor(
    private val habitRepository: HabitRepository,
    private val medicationRepository: MedicationRepository,
    private val userRepository: UserRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(TodayScreenUiState())
    val uiState: StateFlow<TodayScreenUiState> = _uiState.asStateFlow()

    private var currentUserId: String? = null

    init {
        viewModelScope.launch {
            val user = userRepository.getCurrentUser()
            currentUserId = user?.uid

            if (currentUserId == null) {
                _uiState.update { it.copy(isLoading = false, error = "User not logged in") }
                return@launch
            }
            loadTasksAndSuggestions(currentUserId!!)
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    private fun loadTasksAndSuggestions(userId: String) {
        _uiState.update { it.copy(isLoading = true) }

        val habitsFlow = habitRepository.getHabits(userId)
        val medicationsFlow = medicationRepository.getMedications(userId)

        viewModelScope.launch {
            habitsFlow.combine(medicationsFlow) { habitsResult, medicationsResult ->
                val habits = (habitsResult as? NabiResult.Success)?.data ?: emptyList()
                val medications = (medicationsResult as? NabiResult.Success)?.data ?: emptyList()

                val errorMsg = listOf(habitsResult, medicationsResult)
                    .filterIsInstance<NabiResult.Error>()
                    .joinToString(", ") { it.exception.message ?: "Unknown error" }
                    .ifEmpty { null }

                Pair(habits, medications) to errorMsg
            }.collect { (dataPair, error) ->
                val (habits, medications) = dataPair

                val habitTodoItems = habits.map { TodoListItem.HabitItem(it) }
                val medicationTodoItems = medications.map { TodoListItem.MedicationItem(it) }

                val displayedSuggestions = DummySuggestions.allSuggestions.shuffled().take(6)

                _uiState.update {
                    it.copy(
                        todoItems = habitTodoItems.sortedByDescending { item -> item.orderWeight },
                        medicationItems = medicationTodoItems.sortedByDescending { item -> item.orderWeight },
                        suggestions = displayedSuggestions,
                        isLoading = false,
                        error = error ?: it.error
                    )
                }
            }
        }
    }

    fun toggleCompletion(item: TodoListItem, isCompleted: Boolean) {
        val userId = currentUserId ?: return // Should not happen if UI is guarded
        val date = uiState.value.currentDateString

        viewModelScope.launch {
            val result = when (item) {
                is TodoListItem.HabitItem -> habitRepository.updateHabitCompletion(item.id, date, isCompleted)
                is TodoListItem.MedicationItem -> medicationRepository.updateMedicationCompletion(item.id, date, isCompleted)
            }
            if (result is NabiResult.Error) {
                _uiState.update { it.copy(error = result.exception.message ?: "Failed to update status") }
            }
            // Firestore listener updates UI.
        }
    }

    fun addSuggestionToTasks(suggestion: Suggestion) {
        val userId = currentUserId ?: run {
            _uiState.update { it.copy(error = "User not logged in. Cannot add suggestion.") }
            return
        }
        // Use a high orderWeight to make new items appear at the top.
        // Firestore sorts by orderWeight descending.
        val newOrderWeight = System.currentTimeMillis().toInt()


        viewModelScope.launch {
            val result: NabiResult<Unit> = when (suggestion) {
                is Suggestion.HabitSuggestion -> {
                    val newHabit = Habit(
                        userId = userId,
                        name = suggestion.name,
                        source = suggestion.source,
                        timeOfDay = suggestion.timeOfDay,
                        isActive = true,
                        histories = emptyMap(),
                        orderWeight = newOrderWeight
                    )
                    habitRepository.addHabit(newHabit)
                }
                is Suggestion.MedicationSuggestion -> {
                    val newMedication = Medication(
                        userId = userId,
                        name = suggestion.name,
                        timeOfDay = suggestion.timeOfDay,
                        isActive = true,
                        histories = emptyMap(),
                        orderWeight = newOrderWeight
                    )
                    medicationRepository.addMedication(newMedication)
                }
            }

            when (result) {
                is NabiResult.Success -> {
                    // Remove the added suggestion from the UI list
                    _uiState.update {
                        it.copy(suggestions = it.suggestions.filterNot { s ->
                            s.name == suggestion.name && s.description == suggestion.description && s.type == suggestion.type
                        })
                    }
                }
                is NabiResult.Error -> {
                    _uiState.update { it.copy(error = result.exception.message ?: "Failed to add suggestion") }
                }

                NabiResult.Loading -> TODO()
            }
        }
    }
}