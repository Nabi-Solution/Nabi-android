// file: com/gdghufs/nabi/data/DailyRecord.kt
package com.gdghufs.nabi.domain.model

import java.time.LocalDate

data class DailyRecord(
    val date: LocalDate,
    // Mood
    val q1_mood: String,
    val q1_1_reason: String,
    // Enjoyment
    val q2_enjoyment: String,
    val q2_1_note: String,
    // Sleep
    val q3_sleep: String, // Quality: "Good", "Fair", "Poor"
    val q3_1_hours: Float,
    // Energy
    val q4_energy: String, // Level: "High", "Medium", "Low"
    val q4_1_activity: String,
    // Suicidal Thoughts
    val q5_ideation: String, // "Yes", "No"
    val q5_1_reason_if_any: String?,
    // Medication
    val q6_taken: Boolean,
    val q6_1_reason_if_any: String?, // Reason if missed
    // Physical Symptoms
    val q7_discomfort: String, // "Yes", "No"
    val q7_1_symptoms_if_any: List<String>?,
    // Appetite
    val q8_appetite: String, // "Good", "Increased", "Decreased", "No appetite"
    val q8_1_note_if_any: String?,
    val q8_2_1_weight_if_recorded: Float?, // Optional
    // Concentration/Memory
    val q9_concentration: String, // "Good", "Fair", "Poor"
    // Social Interaction
    val q10_interaction: String, // "Positive", "Neutral", "Negative", "Avoided"
    val q10_1_feeling: String,
    // To-do Log
    val todo_list: List<Pair<String, Boolean>>, // (task, isCompleted)
    // Video Check-in Summary
    val video_summary: String?
)