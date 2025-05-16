package com.gdghufs.nabi.data.model

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.temporal.TemporalAdjusters


@Composable
fun WeeklyCalendarView(currentDate: LocalDate) {
    // 이번 주 월요일 계산
    val monday = currentDate.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
    val weekDays = mutableListOf<LocalDate>()
    for (i in 0..6) {
        weekDays.add(monday.plusDays(i.toLong()))
    }

    Column(modifier = Modifier.padding(16.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceAround
        ) {
            val daysHeader = listOf("M", "T", "W", "T", "F", "S", "S")
            daysHeader.forEach { day ->
                Text(
                    text = day,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceAround
        ) {
            weekDays.forEach { date ->
                Text(
                    text = date.dayOfMonth.toString(),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
fun CalendarScreen() {
    WeeklyCalendarView(currentDate = LocalDate.of(2025, 5, 16))
}