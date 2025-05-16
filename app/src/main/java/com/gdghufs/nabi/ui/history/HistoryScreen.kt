package com.gdghufs.nabi.ui.history

import android.annotation.SuppressLint
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.gdghufs.nabi.R // Ensure R file is correctly imported
import com.gdghufs.nabi.domain.model.DailyRecord
import com.gdghufs.nabi.navigation.ScreenRoutes
import com.gdghufs.nabi.ui.common.ExpandableCalendarViewAllMonths // Your custom calendar
import com.gdghufs.nabi.ui.theme.AmiriFamily
import com.gdghufs.nabi.ui.theme.NabiTheme
import com.gdghufs.nabi.ui.theme.RobotoPretendardFamily
import java.net.URLEncoder
import java.nio.charset.StandardCharsets
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle as JavaTextStyle // Alias to avoid conflict
import java.util.Locale

@Composable
fun HistoryScreen(
    navController: NavController,
    viewModel: HistoryViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val today = LocalDate.now()

    // Navigate to WebView when reportHtml is ready
    LaunchedEffect(uiState.reportHtml) {
        uiState.reportHtml?.let { htmlContent ->
            val encodedHtml = URLEncoder.encode(htmlContent, StandardCharsets.UTF_8.toString())
            navController.navigate("${ScreenRoutes.REPORT_WEBVIEW_SCREEN}/$encodedHtml")
            viewModel.clearPreparedReport() // Reset after navigation
        }
    }

    Column(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        // Header
        Row(
            Modifier
                .fillMaxWidth()
                .padding(start = 20.dp, end = 20.dp, top = 20.dp, bottom = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "History",
                fontFamily = AmiriFamily,
                color = Color(0xff740D0D), // Primary theme color
                fontWeight = FontWeight.Bold,
                fontSize = 28.sp, // Increased size
                modifier = Modifier.weight(1f)
            )
            // "Generate Report" button (functionality can be expanded later)
            Row(
                Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .background(color = Color(0xffBBE3FF)) // Accent color
                    .clickable { /* TODO: Implement overall report generation */ }
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Image(
                    painter = painterResource(R.drawable.diagnosis_24px), // Check your drawable name
                    contentDescription = "Generate Report Icon",
                    modifier = Modifier.size(20.dp),
                    colorFilter = ColorFilter.tint(Color.Black)
                )
                Spacer(Modifier.width(6.dp))
                Text(
                    text = "Generate Report",
                    fontFamily = RobotoPretendardFamily,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 13.sp, // Slightly larger
                    color = Color.Black
                )
            }
        }

        // Month/Year Display (Dynamic from ViewModel)
        Text(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 8.dp),
            text = "${
                uiState.currentCalendarDisplayDate.month.getDisplayName(
                    JavaTextStyle.FULL, // Use aliased TextStyle
                    Locale.ENGLISH
                )
            } ${uiState.currentCalendarDisplayDate.year}",
            fontSize = 22.sp, // Adjusted size
            fontWeight = FontWeight.Bold,
            fontFamily = AmiriFamily,
            color = MaterialTheme.colorScheme.onBackground
        )

        // Calendar View
        // Assuming ExpandableCalendarViewAllMonths can take these parameters
        // and has callbacks for date/range selection.
        ExpandableCalendarViewAllMonths(
            initialDisplayDate = uiState.currentCalendarDisplayDate,
            rangeStartDate = uiState.rangeStartDate,
            rangeEndDate = uiState.rangeEndDate,
            // onDateClicked = { date -> viewModel.updateCalendarDisplayDate(date) },
            // onMonthChanged = { newMonthDate -> viewModel.updateCalendarDisplayDate(newMonthDate)}
            // This is a placeholder. Your calendar needs to correctly interact with the ViewModel.
            // For now, it will just display based on ViewModel state.
        )

        Spacer(Modifier.height(16.dp))

        // Range Selection Buttons
        Row(
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            RangeSelectButton(text = "LAST 7 DAYS", modifier = Modifier.weight(1f)) {
                viewModel.setDateRange(today.minusDays(6), today)
            }
            RangeSelectButton(text = "LAST 14 DAYS", modifier = Modifier.weight(1f)) {
                viewModel.setDateRange(today.minusDays(13), today)
            }
            RangeSelectButton(text = "LAST 30 DAYS", modifier = Modifier.weight(1f)) {
                viewModel.setDateRange(today.minusDays(29), today)
            }
        }

        Spacer(Modifier.height(16.dp))

        // Records List
        if (uiState.isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else if (uiState.recordsToDisplay.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize().padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    "No records found for the selected period.",
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        color = Color(0x4DFBFFB7), // Lighter, more subtle background
                        shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp)
                    )
                    .padding(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 0.dp), // Adjust padding
                verticalArrangement = Arrangement.spacedBy(12.dp) // Space between items
            ) {
                items(uiState.recordsToDisplay, key = { record -> record.date.toString() }) { record ->
                    HistoryItem(record = record) {
                        viewModel.prepareReportForRecord(record)
                    }
                }
            }
        }
    }
}

@Composable
fun RangeSelectButton(
    modifier: Modifier = Modifier,
    text: String,
    onClick: () -> Unit
) {
    Button( // Using Material Button for better styling and accessibility
        onClick = onClick,
        modifier = modifier.height(40.dp), // Consistent height
        shape = RoundedCornerShape(8.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = Color(0xffE0E0E0), // Neutral background
            contentColor = Color.Black
        ),
        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp) // Adjust padding
    ) {
        Text(
            text = text,
            fontWeight = FontWeight.Medium, // Medium weight
            fontSize = 12.sp, // Ensure readability
            fontFamily = RobotoPretendardFamily,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
fun HistoryItem(record: DailyRecord, onClick: () -> Unit) {
    val dateFormatter = DateTimeFormatter.ofPattern("MMMM d, yyyy (EEE)", Locale.ENGLISH)
    Card(
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp, pressedElevation = 4.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant), // Use theme color
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    ) {
        Column(
            Modifier
                .padding(horizontal = 16.dp, vertical = 12.dp) // Standard padding
        ) {
            Text(
                text = record.date.format(dateFormatter),
                style = MaterialTheme.typography.titleSmall, // Theme typography
                fontWeight = FontWeight.Bold, // Make date bold
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = "Mood: ${record.q1_mood}  |  Energy: ${record.q4_energy}",
                style = MaterialTheme.typography.bodyMedium, // Theme typography
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 2
            )
            // Optionally, add a small visual cue for medication status or severe symptoms
            if (!record.q6_taken || record.q5_ideation.equals("Yes", ignoreCase = true)) {
                Spacer(Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (!record.q6_taken) {
                        Text("Medication Missed", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.error, modifier = Modifier.padding(end = 8.dp))
                    }
                    if (record.q5_ideation.equals("Yes", ignoreCase = true)) {
                        Text("Suicidal Ideation Reported", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.error)
                    }
                }
            }
        }
    }
}
/*
@SuppressLint("UnrememberedMutableState", "ViewModelConstructorInComposable")
@Preview(showBackground = true, device = "id:pixel_5")
@Composable
fun HistoryScreenPreview() {
    NabiTheme {
        // This preview won't have real ViewModel interaction or NavController.
        // For a more complete preview, consider a fake ViewModel.
        val dummyNavController = NavController(LocalContext.current)
        val dummyViewModel = HistoryViewModel() // This will load dummy data
        // Simulate some state for preview
        dummyViewModel.setDateRange(LocalDate.now().minusDays(2), LocalDate.now())

        HistoryScreen(navController = dummyNavController, viewModel = dummyViewModel)
    }
}*/

// You need to have ExpandableCalendarViewAllMonths composable defined.
// This is a placeholder:
@Composable
fun ExpandableCalendarViewAllMonths(
    initialDisplayDate: LocalDate,
    rangeStartDate: LocalDate?,
    rangeEndDate: LocalDate?,
    // Add callbacks as needed:
    // onDateClicked: (LocalDate) -> Unit = {},
    // onMonthChanged: (LocalDate) -> Unit = {}
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(280.dp) // Example fixed height
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(12.dp)),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                "Calendar Placeholder",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(8.dp))
            Text(
                "Displaying: ${initialDisplayDate.month.getDisplayName(JavaTextStyle.FULL, Locale.ENGLISH)} ${initialDisplayDate.year}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            if (rangeStartDate != null && rangeEndDate != null) {
                Spacer(Modifier.height(4.dp))
                Text(
                    "Range: ${rangeStartDate.dayOfMonth} - ${rangeEndDate.dayOfMonth} ${rangeEndDate.month.getDisplayName(JavaTextStyle.SHORT, Locale.ENGLISH)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}