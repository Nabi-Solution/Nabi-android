package com.gdghufs.nabi.data.model

sealed class Suggestion {
    abstract val name: String
    abstract val description: String
    abstract val timeOfDay: TimeOfDay
    abstract val type: SuggestionType

    data class HabitSuggestion(
        override val name: String,
        override val description: String,
        override val timeOfDay: TimeOfDay,
        val source: String // "doctor", "patient", "ai"
    ) : Suggestion() {
        override val type: SuggestionType = SuggestionType.HABIT
    }

    data class MedicationSuggestion(
        override val name: String,
        override val description: String,
        override val timeOfDay: TimeOfDay
    ) : Suggestion() {
        override val type: SuggestionType = SuggestionType.MEDICATION
    }
}

enum class SuggestionType { HABIT, MEDICATION }

object DummySuggestions {
    val allSuggestions: List<Suggestion> = listOf(
        // Habit Suggestions
        Suggestion.HabitSuggestion("Morning Meditation", "Start your day calm and focused with a 10-minute meditation.", TimeOfDay.MORNING, "ai"),
        Suggestion.HabitSuggestion("Walk 10,000 Steps", "Stay active by aiming for 10,000 steps daily.", TimeOfDay.ANYTIME, "doctor"),
        Suggestion.HabitSuggestion("Read for 30 Minutes", "Expand your knowledge or relax with a book before bed.", TimeOfDay.EVENING, "patient"),
        Suggestion.HabitSuggestion("Drink 8 Glasses of Water", "Stay hydrated for overall well-being.", TimeOfDay.ANYTIME, "ai"),
        Suggestion.HabitSuggestion("Evening Stretch Routine", "Relax your muscles with a 15-minute stretch before sleep.", TimeOfDay.EVENING, "doctor"),
        Suggestion.HabitSuggestion("Journal Your Thoughts", "Spend 10 minutes writing down your thoughts and feelings.", TimeOfDay.EVENING, "patient"),
        Suggestion.HabitSuggestion("Eat a Healthy Breakfast", "Fuel your body with a nutritious breakfast to start your day.", TimeOfDay.MORNING, "ai"),
        Suggestion.HabitSuggestion("Practice Deep Breathing", "Take 5 minutes for deep breathing exercises when stressed.", TimeOfDay.ANYTIME, "doctor"),
        Suggestion.HabitSuggestion("Plan Your Day", "Take 10 minutes in the morning to plan your tasks.", TimeOfDay.MORNING, "patient"),
        Suggestion.HabitSuggestion("Limit Screen Time Before Bed", "Avoid screens for at least 1 hour before sleeping.", TimeOfDay.EVENING, "ai"),

        // Medication Suggestions
        Suggestion.MedicationSuggestion("Take Multivitamins", "Ensure you get essential daily nutrients.", TimeOfDay.MORNING),
        Suggestion.MedicationSuggestion("Prescription Omega-3", "Follow doctor's advice for heart health.", TimeOfDay.ANYTIME),
        Suggestion.MedicationSuggestion("Iron Supplement", "As prescribed, usually best taken in the morning.", TimeOfDay.MORNING),
        Suggestion.MedicationSuggestion("Calcium Tablet", "For bone health, consider taking with an evening meal.", TimeOfDay.EVENING),
        Suggestion.MedicationSuggestion("Probiotics", "To support gut health, take as directed.", TimeOfDay.MORNING)
    )
}