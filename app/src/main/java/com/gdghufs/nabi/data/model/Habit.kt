package com.gdghufs.nabi.data.model

import com.google.firebase.firestore.DocumentId

// Replace with your actual package name


data class Habit(
    @DocumentId val id: String = "", // Firestore document ID
    val userId: String = "",
    val name: String = "",
    val source: String = "", // "doctor", "patient", "ai"
    val timeOfDay: TimeOfDay = TimeOfDay.ANYTIME,
    val isActive: Boolean = true,
    val histories: Map<String, Boolean> = emptyMap(), // Key: "yyyy-MM-dd"
    val orderWeight: Int = 0
)