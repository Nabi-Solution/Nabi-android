package com.gdghufs.nabi.data.model // Replace with your actual package name

import com.google.firebase.firestore.DocumentId

data class Medication(
    @DocumentId val id: String = "", // Firestore document ID
    val userId: String = "",
    val name: String = "",
    val timeOfDay: TimeOfDay = TimeOfDay.ANYTIME,
    val isActive: Boolean = true,
    val histories: Map<String, Boolean> = emptyMap(), // Key: "yyyy-MM-dd"
    val orderWeight: Int = 0
)