package com.gdghufs.nabi.data.model

import com.gdghufs.nabi.ui.common.TimeTagType

enum class TimeOfDay {
    ANYTIME,
    MORNING,
    AFTERNOON,
    EVENING;

    // Helper to map TimeOfDay to TimeTagType if needed directly, or rely on ViewModel/UI logic
    fun toTimeTagType(): TimeTagType { // Adjust package if needed
        return when (this) {
            ANYTIME -> TimeTagType.AnyTime
            MORNING -> TimeTagType.Morning
            AFTERNOON -> TimeTagType.Afternoon
            EVENING -> TimeTagType.Evening
        }
    }
}