package com.gdghufs.nabi.ui.history

import com.gdghufs.nabi.domain.model.DailyRecord

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gdghufs.nabi.domain.model.DummyDataGenerator
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle // Ensure this is java.time.format.TextStyle
import java.util.Locale
import javax.inject.Inject

data class HistoryScreenUiState(
    val recordsToDisplay: List<DailyRecord> = emptyList(),
    val reportHtml: String? = null, // HTML content for the selected record
    val isLoading: Boolean = true,
    val rangeStartDate: LocalDate? = null,
    val rangeEndDate: LocalDate? = null,
    val currentCalendarDisplayDate: LocalDate = LocalDate.now() // For ExpandableCalendar
)

@HiltViewModel
class HistoryViewModel @Inject constructor() : ViewModel() {

    private val _uiState = MutableStateFlow(HistoryScreenUiState())
    val uiState: StateFlow<HistoryScreenUiState> = _uiState.asStateFlow()

    private var allLoadedRecords: List<DailyRecord> = emptyList()

    init {
        loadInitialRecords()
    }

    private fun loadInitialRecords() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            allLoadedRecords = DummyDataGenerator.generateRandomRecords(30) // Load 30 days of data
            val today = LocalDate.now()
            // Default to showing the last 7 days initially
            val initialStartDate = today.minusDays(6)
            val initialEndDate = today
            filterRecordsForDateRange(initialStartDate, initialEndDate)
            _uiState.update {
                it.copy(
                    isLoading = false,
                    rangeStartDate = initialStartDate,
                    rangeEndDate = initialEndDate,
                    currentCalendarDisplayDate = initialEndDate
                )
            }
        }
    }

    fun setDateRange(startDate: LocalDate, endDate: LocalDate) {
        filterRecordsForDateRange(startDate, endDate)
        _uiState.update {
            it.copy(
                rangeStartDate = startDate,
                rangeEndDate = endDate,
                currentCalendarDisplayDate = endDate // Update calendar focus
            )
        }
    }

    private fun filterRecordsForDateRange(startDate: LocalDate, endDate: LocalDate) {
        val filtered = allLoadedRecords.filter { record ->
            !record.date.isBefore(startDate) && !record.date.isAfter(endDate)
        }.sortedByDescending { it.date } // Ensure newest is first
        _uiState.update { it.copy(recordsToDisplay = filtered) }
    }

    fun prepareReportForRecord(record: DailyRecord) {
        val html = generateReportHtmlContent(record)
        _uiState.update { it.copy(reportHtml = html) }
    }

    fun clearPreparedReport() {
        _uiState.update { it.copy(reportHtml = null) }
    }

    fun updateCalendarDisplayDate(date: LocalDate) {
        _uiState.update { it.copy(currentCalendarDisplayDate = date) }
    }

    // Generates HTML content based on OUTPUT FORMAT
    private fun generateReportHtmlContent(record: DailyRecord): String {
        val dateFormatter = DateTimeFormatter.ofPattern("MMMM d, yyyy", Locale.ENGLISH)
        val dateString = record.date.format(dateFormatter)

        val symptomsSummaryPoints = mutableListOf<String>()
        symptomsSummaryPoints.add("<b>Mood:</b> ${record.q1_mood}. Reason: <i>${record.q1_1_reason}</i>")
        symptomsSummaryPoints.add("<b>Enjoyment of activities:</b> ${record.q2_enjoyment}. Note: <i>${record.q2_1_note}</i>")
        symptomsSummaryPoints.add("<b>Sleep:</b> ${record.q3_1_hours} hours, Quality: ${record.q3_sleep}")
        symptomsSummaryPoints.add("<b>Energy level:</b> ${record.q4_energy}. Activity: <i>${record.q4_1_activity}</i>")

        if (record.q5_ideation.equals("Yes", ignoreCase = true)) {
            symptomsSummaryPoints.add("<b>Suicidal Ideation:</b> Reported. ${record.q5_1_reason_if_any?.let { "Details: <i>$it</i>" } ?: "<i>No specific details provided.</i>"}")
        }

        if (record.q7_discomfort.equals("Yes", ignoreCase = true)) {
            val symptoms = record.q7_1_symptoms_if_any?.joinToString(", ") ?: "<i>Not specified</i>"
            symptomsSummaryPoints.add("<b>Physical Discomfort:</b> Reported. Symptoms: <i>$symptoms</i>")
        }

        symptomsSummaryPoints.add("<b>Appetite:</b> ${record.q8_appetite}. ${record.q8_1_note_if_any?.let { "Note: <i>$it</i>" } ?: ""}")
        record.q8_2_1_weight_if_recorded?.let { symptomsSummaryPoints.add("<b>Weight Recorded:</b> $it kg") }
        symptomsSummaryPoints.add("<b>Concentration/Memory:</b> ${record.q9_concentration}")
        symptomsSummaryPoints.add("<b>Social Interaction:</b> ${record.q10_interaction}. Feeling: <i>${record.q10_1_feeling}</i>")

        val symptomsSummaryHtml = symptomsSummaryPoints.joinToString(separator = "<br>• ", prefix = "• ")

        val medicationLogHtml = if (record.q6_taken) {
            "☑️ Medication – Taken"
        } else {
            "❌ Missed medication.${record.q6_1_reason_if_any?.let { "<br>&nbsp;&nbsp;&nbsp;&nbsp;Reason: <i>$it</i>" } ?: ""}"
        }

        val todoListHtml = if (record.todo_list.isNotEmpty()) {
            record.todo_list.joinToString(separator = "") { (task, completed) ->
                "<li>[${if (completed) "✔" else "&nbsp;&nbsp;"}] $task</li>"
            }
        } else {
            "<li>No tasks logged for this day.</li>"
        }

        val videoCheckinHtml = record.video_summary?.let {
            "<p>Summary: <i>$it</i></p>"
        } ?: "<p>No video check-in summary available for this day.</p>"

        return """
        <!DOCTYPE html>
        <html lang="en">
        <head>
            <meta charset="UTF-8">
            <meta name="viewport" content="width=device-width, initial-scale=1.0">
            <title>Daily Report - $dateString</title>
            <style>
                body { font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, Helvetica, Arial, sans-serif; margin: 16px; line-height: 1.6; color: #333; background-color: #FFFFFF; }
                .report-container { border: 1px solid #E0E0E0; border-radius: 8px; padding: 20px; background-color: #FDFDFD; box-shadow: 0 2px 4px rgba(0,0,0,0.1); }
                h1 { font-size: 1.5em; color: #740D0D; text-align: center; margin-bottom: 0.2em;}
                .date-header { font-size: 1.2em; color: #555; text-align: center; margin-bottom: 1.5em; }
                h2 { font-size: 1.2em; color: #740D0D; border-bottom: 2px solid #BBE3FF; padding-bottom: 0.3em; margin-top: 1.5em; margin-bottom: 0.8em; }
                ul { list-style-type: none; padding-left: 0; }
                li { margin-bottom: 0.5em; }
                p { margin-top: 0.3em; margin-bottom: 0.8em;}
                b { color: #004D40; } /* Darker green for key terms */
                i { color: #424242; } /* Dark grey for notes/details */
                .section { margin-bottom: 1.5em; }
                .medication-taken { color: #2E7D32; } /* Green for taken */
                .medication-missed { color: #C62828; } /* Red for missed */
            </style>
        </head>
        <body>
            <div class="report-container">
                <h1>Depression Symptom Log</h1>
                <div class="date-header">$dateString</div>

                <div class="section">
                    <h2>1. Symptoms Summary:</h2>
                    <p>$symptomsSummaryHtml</p>
                </div>

                <div class="section">
                    <h2>2. Medication Log:</h2>
                    <p class="${if (record.q6_taken) "medication-taken" else "medication-missed"}">$medicationLogHtml</p>
                </div>

                <div class="section">
                    <h2>3. To-Do Log:</h2>
                    <ul>$todoListHtml</ul>
                </div>

                <div class="section">
                    <h2>4. Video Check-in Summary:</h2>
                    $videoCheckinHtml
                </div>
            </div>
        </body>
        </html>
        """.trimIndent()
    }
}