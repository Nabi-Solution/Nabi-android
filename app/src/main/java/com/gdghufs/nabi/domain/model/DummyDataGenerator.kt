// file: com/gdghufs/nabi/data/DummyDataGenerator.kt
package com.gdghufs.nabi.domain.model

import java.time.LocalDate
import java.util.Locale
import kotlin.random.Random

object DummyDataGenerator {

    private val moods = listOf("Very Low", "Low", "Neutral", "Slightly Better", "Okay", "Good")
    private val moodReasons = mapOf(
        "Very Low" to listOf("Felt an overwhelming sense of dread.", "Couldn't stop crying.", "Everything felt like a huge effort.", "Isolated myself completely."),
        "Low" to listOf("Struggled with negative thoughts.", "Felt tired and unmotivated.", "Found it hard to enjoy things I usually like.", "Had a poor night's sleep."),
        "Neutral" to listOf("It was an average day.", "Nothing special happened.", "Managed to get through my tasks.", "Felt neither up nor down."),
        "Slightly Better" to listOf("Had a moment of calm.", "Achieved a small goal.", "Felt a bit more hopeful than yesterday.", "Connected briefly with nature."),
        "Okay" to listOf("Felt reasonably content.", "Was productive and focused for a while.", "Enjoyed a simple pleasure, like a good meal.", "Social interaction was manageable."),
        "Good" to listOf("Felt genuinely happy and energetic.", "Had a very productive day.", "Spent quality time with loved ones.", "Felt optimistic about the future.")
    )
    private val enjoyments = listOf("None at all", "Very little", "A little", "Some", "A moderate amount", "A lot")
    private val enjoymentNotes = listOf("Tried to watch a movie but couldn't concentrate.", "Listened to music, which helped a bit.", "Read a chapter of a book.", "Spent time on a hobby and it felt good.", "Laughed with a friend.")
    private val sleepQualities = listOf("Very Poor", "Poor", "Fair", "Good", "Very Good")
    private val energyLevels = listOf("Exhausted", "Very Low", "Low", "Moderate", "High")
    private val energyActivities = mapOf(
        "Exhausted" to listOf("Stayed in bed all day.", "Could barely keep my eyes open."),
        "Very Low" to listOf("Mostly rested on the couch.", "Managed only essential self-care."),
        "Low" to listOf("Did some light housework.", "Took a short, slow walk.", "Showered and got dressed."),
        "Moderate" to listOf("Went grocery shopping.", "Worked for a few hours.", "Engaged in a hobby for a bit."),
        "High" to listOf("Exercised.", "Completed all my planned tasks.", "Felt energetic throughout the day.")
    )
    private val ideationOptions = listOf("No", "Yes")
    private val ideationReasons = listOf("Felt like I was a burden to others.", "Wanted the emotional pain to end.", "Thought things would never improve for me.", "Felt completely alone.")
    private val physicalDiscomfortOptions = listOf("No", "Yes")
    private val symptoms = listOf("Persistent headache", "Upset stomach", "Muscle aches and tension", "Overwhelming fatigue", "Brain fog", "Chest tightness", "Nausea")
    private val appetites = listOf("No appetite at all", "Significantly decreased", "Slightly decreased", "Normal", "Slightly increased", "Significantly increased")
    private val concentrationLevels = listOf("Extremely poor, couldn't focus at all", "Poor, easily distracted", "Fair, struggled at times", "Good, mostly focused", "Very good, sharp concentration")
    private val interactions = listOf("Avoided all social contact", "Minimal, only necessary interactions", "Neutral, neither positive nor negative", "Somewhat positive, a pleasant chat", "Very positive, fulfilling connection")
    private val interactionFeelings = mapOf(
        "Avoided all social contact" to "Felt too anxious and overwhelmed to interact.",
        "Minimal, only necessary interactions" to "Interactions felt draining and difficult.",
        "Neutral, neither positive nor negative" to "Had a few conversations, they were okay.",
        "Somewhat positive, a pleasant chat" to "Had a nice conversation that lifted my spirits a bit.",
        "Very positive, fulfilling connection" to "Felt genuinely connected and supported during interactions."
    )
    private val todoTemplates = listOf("Take prescribed medication", "Drink at least 8 glasses of water", "Eat 3 nutritious meals", "Take a 15-minute walk outside", "Practice mindfulness for 10 minutes", "Journal about my feelings", "Reach out to a friend or family member", "Complete one small household chore", "Engage in a hobby for 30 minutes")
    private val videoSummaries = listOf(
        "Shared about feeling persistently tired and having trouble sleeping.",
        "Talked about increased anxiety and difficulty concentrating at work.",
        "Mentioned a slight improvement in mood but still feeling fragile.",
        "Expressed feelings of loneliness and a desire for more social connection.",
        "Discussed coping strategies that have been helpful, like journaling.",
        null // Represents no video check-in
    )

    fun generateRandomRecords(count: Int): List<DailyRecord> {
        val records = mutableListOf<DailyRecord>()
        val today = LocalDate.now()
        var lastMoodIndex = moods.size / 2 // Start with a neutral mood for the oldest record

        for (i in (count - 1) downTo 0) { // Generate from oldest to newest to allow some trend
            val date = today.minusDays(i.toLong())

            // Introduce a slight trend to mood over days, with some randomness
            val moodChange = Random.nextInt(-1, 2) // -1, 0, or 1
            lastMoodIndex = (lastMoodIndex + moodChange).coerceIn(0, moods.size - 1)
            val currentMood = moods[lastMoodIndex]

            val currentEnergyIndex = Random.nextInt(energyLevels.size)
            val currentEnergy = energyLevels[currentEnergyIndex]

            val currentInteractionIndex = Random.nextInt(interactions.size)
            val currentInteraction = interactions[currentInteractionIndex]

            val hasIdeation = Random.nextDouble() < 0.08 // 8% chance for "Yes"
            val missedMedication = Random.nextDouble() < 0.15 // 15% chance to miss
            val hasDiscomfort = Random.nextDouble() < 0.35 // 35% chance for discomfort
            val hasVideoSummary = Random.nextDouble() < 0.65 // 65% chance for video summary

            records.add(
                DailyRecord(
                    date = date,
                    q1_mood = currentMood,
                    q1_1_reason = moodReasons[currentMood]?.random() ?: "No specific reason noted.",
                    q2_enjoyment = enjoyments.random(),
                    q2_1_note = enjoymentNotes.random(),
                    q3_sleep = sleepQualities.random(),
                    q3_1_hours = String.format(Locale.US, "%.1f", Random.nextDouble(4.0, 9.5)).toFloat(),
                    q4_energy = currentEnergy,
                    q4_1_activity = energyActivities[currentEnergy]?.random() ?: "No specific activity.",
                    q5_ideation = if (hasIdeation) "Yes" else "No",
                    q5_1_reason_if_any = if (hasIdeation) ideationReasons.random() else null,
                    q6_taken = !missedMedication,
                    q6_1_reason_if_any = if (missedMedication) listOf("Forgot", "Felt too tired to get them", "Didn't want to take them", "Ran out of medication").random() else null,
                    q7_discomfort = if (hasDiscomfort) "Yes" else "No",
                    q7_1_symptoms_if_any = if (hasDiscomfort) symptoms.shuffled().take(Random.nextInt(1, 4)) else null,
                    q8_appetite = appetites.random(),
                    q8_1_note_if_any = if (Random.nextBoolean()) listOf("Ate more/less than usual due to stress.", "Had cravings for comfort food.", "Skipped a meal.").random() else null,
                    q8_2_1_weight_if_recorded = if (Random.nextDouble() < 0.05) String.format(Locale.US, "%.1f", Random.nextDouble(50.0, 80.0)).toFloat() else null, // 5% chance to record weight
                    q9_concentration = concentrationLevels.random(),
                    q10_interaction = currentInteraction,
                    q10_1_feeling = interactionFeelings[currentInteraction] ?: "No specific feeling recorded.",
                    todo_list = todoTemplates.shuffled().take(Random.nextInt(3, 6)).map { Pair(it, Random.nextDouble() < 0.6) }, // 60% chance of completing a task
                    video_summary = if (hasVideoSummary) videoSummaries.random() else null
                )
            )
        }
        return records.sortedByDescending { it.date } // Newest first
    }
}