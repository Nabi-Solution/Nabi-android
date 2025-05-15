package com.gdghufs.nabi.ui.common

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
// import androidx.compose.foundation.rememberScrollState // LazyColumn 사용으로 제거
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
// import androidx.compose.foundation.verticalScroll // LazyColumn 사용으로 제거
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.KeyboardArrowUp // 아이콘 변경됨
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.gdghufs.nabi.ui.theme.NabiTheme
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.TextStyle
import java.time.temporal.TemporalAdjusters
import java.util.Locale

// LazyColumn 관련 import 추가
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
// import kotlinx.coroutines.launch // LaunchedEffect 내부에서 바로 호출 가능

// --- Helper Functions (이전과 동일) ---
fun getWeekDays(dateInWeek: LocalDate): List<LocalDate> {
    val monday = dateInWeek.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
    return List(7) { i -> monday.plusDays(i.toLong()) }
}

fun getDaysInMonthGrid(yearMonth: YearMonth): List<LocalDate?> {
    val firstDayOfMonth = yearMonth.atDay(1)
    val lastDayOfMonth = yearMonth.atEndOfMonth()
    val daysInMonthList = mutableListOf<LocalDate?>()
    val firstDayOfWeekValue = firstDayOfMonth.dayOfWeek.value
    for (i in 1 until firstDayOfWeekValue) {
        daysInMonthList.add(null)
    }
    for (day in 1..lastDayOfMonth.dayOfMonth) {
        daysInMonthList.add(yearMonth.atDay(day))
    }
    return daysInMonthList
}

// --- Composable Components ---

// DayOfWeekHeader (이전과 동일)
@Composable
fun DayOfWeekHeader(modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceAround
    ) {
        val daysHeader = listOf("M", "T", "W", "T", "F", "S", "S")
        daysHeader.forEach { day ->
            Text(
                color = MaterialTheme.colorScheme.primary,
                text = day,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                modifier = Modifier.weight(1f),
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}

// CompactCalendarView (이전과 동일)
@Composable
fun CompactCalendarView(
    displayDate: LocalDate,
    rangeStartDate: LocalDate?,
    rangeEndDate: LocalDate?,
    modifier: Modifier = Modifier,
    currentDisplayMonth: YearMonth
) {
    val weekDays = getWeekDays(displayDate)
    Column(modifier = modifier.padding(vertical = 8.dp)) {
        DayOfWeekHeader()
        Spacer(modifier = Modifier.height(8.dp))
        Row(modifier = Modifier.fillMaxWidth()) {
            weekDays.forEach { date ->
                val isInRange = rangeStartDate != null && rangeEndDate != null &&
                        !date.isBefore(rangeStartDate) && !date.isAfter(rangeEndDate)
                val isRangeStart = date == rangeStartDate
                val isRangeEnd = date == rangeEndDate
                DateView(
                    date = date,
                    isCurrentDisplayMonthDate = date.month == currentDisplayMonth.month,
                    isToday = date == LocalDate.now(),
                    isInRange = isInRange,
                    isRangeStartDate = isRangeStart,
                    isRangeEndDate = isRangeEnd,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

// MonthView (이전과 동일)
@Composable
fun MonthView(
    yearMonth: YearMonth,
    rangeStartDate: LocalDate?,
    rangeEndDate: LocalDate?,
    modifier: Modifier = Modifier
) {
    val daysInGrid = getDaysInMonthGrid(yearMonth)
    val weekCount = (daysInGrid.size + 6) / 7
    Column(modifier = modifier) {
        DayOfWeekHeader()
        Spacer(modifier = Modifier.height(4.dp))
        for (week in 0 until weekCount) {
            Row(modifier = Modifier.fillMaxWidth()) {
                for (dayIndexInWeek in 0 until 7) {
                    val dayListIndex = week * 7 + dayIndexInWeek
                    if (dayListIndex < daysInGrid.size) {
                        val date = daysInGrid[dayListIndex]
                        val isInRange = rangeStartDate != null && rangeEndDate != null &&
                                date != null &&
                                !date.isBefore(rangeStartDate) && !date.isAfter(rangeEndDate)
                        val isRangeStart = date != null && date == rangeStartDate
                        val isRangeEnd = date != null && date == rangeEndDate
                        DateView(
                            date = date,
                            isCurrentDisplayMonthDate = date?.month == yearMonth.month,
                            isToday = date == LocalDate.now(),
                            isInRange = isInRange,
                            isRangeStartDate = isRangeStart,
                            isRangeEndDate = isRangeEnd,
                            modifier = Modifier.weight(1f)
                        )
                    } else {
                        Spacer(modifier = Modifier.weight(1f).aspectRatio(1f))
                    }
                }
            }
            if (week < weekCount - 1) Spacer(modifier = Modifier.height(4.dp))
        }
    }
}

// getDateRangeShape (이전과 동일)
fun getDateRangeShape(isRangeStart: Boolean, isRangeEnd: Boolean, isInRange: Boolean): Shape {
    return when {
        isRangeStart && isRangeEnd && isInRange -> CircleShape
        isRangeStart && isInRange -> RoundedCornerShape(topStartPercent = 50, bottomStartPercent = 50, topEndPercent = 0, bottomEndPercent = 0)
        isRangeEnd && isInRange -> RoundedCornerShape(topStartPercent = 0, bottomStartPercent = 0, topEndPercent = 50, bottomEndPercent = 50)
        isInRange -> RectangleShape
        else -> RectangleShape
    }
}

// DateView (이전과 동일)
@Composable
fun DateView(
    date: LocalDate?,
    isCurrentDisplayMonthDate: Boolean,
    isToday: Boolean,
    isInRange: Boolean,
    isRangeStartDate: Boolean,
    isRangeEndDate: Boolean,
    modifier: Modifier = Modifier
) {
    val cellShape = getDateRangeShape(isRangeStartDate, isRangeEndDate, isInRange)
    val rangeHighlightColor = Color(0xFFD9D9D9).copy(alpha = 0.7f)
    val backgroundColor = when {
        isInRange -> rangeHighlightColor
        else -> Color.Transparent
    }
    Box(
        modifier = modifier
            .aspectRatio(1f)
            .clip(cellShape)
            .background(backgroundColor),
        contentAlignment = Alignment.Center
    ) {
        if (isToday) {
            Box(
                modifier = Modifier
                    .fillMaxSize(0.7f)
                    .clip(CircleShape)
                    .background(
                        if (!isInRange) MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.6f)
                        else Color.Transparent
                    )
            )
        }
        if (date != null) {
            val textColor = if (isInRange) {
                MaterialTheme.colorScheme.onSurface
            } else if (!isCurrentDisplayMonthDate) {
                MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
            } else if (date.dayOfWeek == DayOfWeek.SUNDAY) {
                Color.Red
            } else if (date.dayOfWeek == DayOfWeek.SATURDAY) {
                Color.Blue
            } else if (isToday) {
                MaterialTheme.colorScheme.primary
            } else {
                MaterialTheme.colorScheme.onSurface
            }
            Text(
                text = date.dayOfMonth.toString(),
                textAlign = TextAlign.Center,
                color = textColor,
                fontWeight = if (isInRange || isToday) FontWeight.Bold else FontWeight.Normal,
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

@Composable
fun ExpandableCalendarViewAllMonths(
    initialDisplayDate: LocalDate = LocalDate.now(),
    rangeStartDate: LocalDate? = null,
    rangeEndDate: LocalDate? = null
) {
    var isExpanded by remember { mutableStateOf(false) }
    var compactViewDisplayMonth by remember { mutableStateOf(YearMonth.from(initialDisplayDate)) }

    LaunchedEffect(initialDisplayDate, isExpanded) {
        if (!isExpanded) {
            compactViewDisplayMonth = YearMonth.from(initialDisplayDate)
        }
    }

    val today = LocalDate.now() // 현재 날짜 (2025-05-16)
    // 확장 시 표시할 월 범위: 현재 달 기준 이전 2개월 ~ 다음 1개월 (총 4개월)
    // 예: 오늘이 5월이면, 3월, 4월, 5월, 6월
    val expandedStartMonth = YearMonth.from(today.minusMonths(2))
    val expandedEndMonth = YearMonth.from(today.plusMonths(1))

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .background(Color(0xFFFFFBE0), RoundedCornerShape(16.dp))
            .padding(vertical = 12.dp, horizontal = 8.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = if (!isExpanded) {
                    "${compactViewDisplayMonth.month.getDisplayName(TextStyle.FULL, Locale.ENGLISH)} ${compactViewDisplayMonth.year}"
                } else {
                    "${expandedStartMonth.month.getDisplayName(TextStyle.SHORT, Locale.ENGLISH)} ${expandedStartMonth.year} - ${expandedEndMonth.month.getDisplayName(TextStyle.SHORT, Locale.ENGLISH)} ${expandedEndMonth.year}"
                },
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.weight(1f)
            )
            IconButton(onClick = {
                isExpanded = !isExpanded
                if (!isExpanded) {
                    compactViewDisplayMonth = YearMonth.from(initialDisplayDate)
                }
            }) {
                Icon(
                    imageVector = if (isExpanded) Icons.Filled.KeyboardArrowUp else Icons.Filled.ArrowDropDown,
                    contentDescription = if (isExpanded) "Collapse Calendar" else "Expand Calendar"
                )
            }
        }

        AnimatedVisibility(visible = !isExpanded) {
            CompactCalendarView(
                displayDate = initialDisplayDate,
                rangeStartDate = rangeStartDate,
                rangeEndDate = rangeEndDate,
                currentDisplayMonth = compactViewDisplayMonth,
                modifier = Modifier.padding(horizontal = 8.dp)
            )
        }

        AnimatedVisibility(visible = isExpanded) {
            val lazyListState = rememberLazyListState()
            // 확장된 뷰에 표시할 월 목록 준비
            val monthsInExpandedView = remember(expandedStartMonth, expandedEndMonth) {
                buildList {
                    var current = expandedStartMonth
                    while (!current.isAfter(expandedEndMonth)) {
                        add(current)
                        current = current.plusMonths(1)
                    }
                }
            }

            // 달력이 확장되거나 rangeStartDate가 변경될 때 스크롤 실행
            LaunchedEffect(isExpanded, rangeStartDate, monthsInExpandedView) {
                if (isExpanded && rangeStartDate != null) {
                    val targetYearMonth = YearMonth.from(rangeStartDate)
                    val targetIndex = monthsInExpandedView.indexOfFirst { it == targetYearMonth }
                    if (targetIndex != -1) {
                        // 대상 월이 목록에 있으면 해당 월로 스크롤
                        lazyListState.animateScrollToItem(index = targetIndex)
                    }
                }
            }

            LazyColumn(
                state = lazyListState,
                // Modifier에 heightIn 등을 사용하여 최대 높이 제한 가능
                // modifier = Modifier.heightIn(max = 400.dp) // 예시
            ) {
                itemsIndexed(monthsInExpandedView) { index, monthToDisplay ->
                    Text(
                        text = "${monthToDisplay.month.getDisplayName(TextStyle.FULL, Locale.ENGLISH)} ${monthToDisplay.year}",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 10.dp)
                    )
                    MonthView(
                        yearMonth = monthToDisplay,
                        rangeStartDate = rangeStartDate,
                        rangeEndDate = rangeEndDate,
                        modifier = Modifier.padding(horizontal = 8.dp)
                    )

                    if (index < monthsInExpandedView.size - 1) { // 마지막 월이 아니면 간격 추가
                        Spacer(modifier = Modifier.height(12.dp))
                    }
                }
            }
        }
    }
}


// --- Previews (이전과 동일) ---
@Preview(showBackground = true, name = "Expandable Calendar (Range EN) - Auto Scroll")
@Composable
fun ExpandableCalendarAllMonthsRangePreviewScroll() {
    NabiTheme {
        // HistoryScreen에서 rangeStartDate가 오늘로부터 10일 전으로 설정되는 경우 시뮬레이션
        // initialDisplayDate는 오늘, rangeStartDate는 그 이전
        // 오늘: 2025-05-16
        // rangeStartDate: 2025-05-06 (5월)
        // expandedStartMonth: 2025-03 (오늘-2개월)
        // 5월은 expanded view에 포함되므로 스크롤되어야 함.
        ExpandableCalendarViewAllMonths(
            initialDisplayDate = LocalDate.of(2025, 5, 16),
            rangeStartDate = LocalDate.of(2025, 5, 6), // 5월로 스크롤될 것으로 예상
            rangeEndDate = LocalDate.of(2025, 5, 16)
        )
    }
}

@Preview(showBackground = true, name = "Expandable Calendar (Range EN) - Scroll to Prev Month")
@Composable
fun ExpandableCalendarAllMonthsRangePreviewScrollPrevMonth() {
    NabiTheme {
        // rangeStartDate가 이전 달인 경우 (예: "LAST 30 DAYS")
        // 오늘: 2025-05-16
        // rangeStartDate: 2025-04-17 (4월)
        // expandedStartMonth: 2025-03
        // 4월은 expanded view에 포함되므로 스크롤되어야 함.
        ExpandableCalendarViewAllMonths(
            initialDisplayDate = LocalDate.of(2025, 5, 16),
            rangeStartDate = LocalDate.of(2025, 4, 17), // 4월로 스크롤될 것으로 예상
            rangeEndDate = LocalDate.of(2025, 5, 16)
        )
    }
}

// CompactCalendarRangePreview, SingleDayRangePreview (이전과 동일)
@Preview(showBackground = true, name = "Compact Calendar (Range EN)")
@Composable
fun CompactCalendarRangePreview() {
    NabiTheme {
        Box(modifier = Modifier.background(Color(0xFFFFFBE0)).padding(16.dp)) {
            CompactCalendarView(
                displayDate = LocalDate.of(2025, 5, 16),
                rangeStartDate = LocalDate.of(2025, 5, 12),
                rangeEndDate = LocalDate.of(2025, 5, 16),
                currentDisplayMonth = YearMonth.of(2025,5),
                modifier = Modifier.padding(horizontal = 8.dp)
            )
        }
    }
}

@Preview(showBackground = true, name = "Single Day Range EN")
@Composable
fun SingleDayRangePreview() {
    NabiTheme {
        ExpandableCalendarViewAllMonths(
            initialDisplayDate = LocalDate.of(2025, 5, 16),
            rangeStartDate = LocalDate.of(2025, 5, 10),
            rangeEndDate = LocalDate.of(2025, 5, 10)
        )
    }
}