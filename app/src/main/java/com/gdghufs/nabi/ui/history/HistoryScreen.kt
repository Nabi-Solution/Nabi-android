package com.gdghufs.nabi.ui.history

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CardElevation
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue // Added for state delegation
import androidx.compose.runtime.mutableStateOf // Added for state
import androidx.compose.runtime.remember // Added for state
import androidx.compose.runtime.setValue // Added for state delegation
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.gdghufs.nabi.R
import com.gdghufs.nabi.ui.common.ExpandableCalendarViewAllMonths
import com.gdghufs.nabi.ui.theme.AmiriFamily
import com.gdghufs.nabi.ui.theme.NabiTheme
import com.gdghufs.nabi.ui.theme.RobotoPretendardFamily
import java.time.LocalDate
import java.time.format.TextStyle // Added for month display
import java.util.Date
import java.util.Locale          // Added for month display

@Preview
@Composable
fun HistoryScreenPreview() {
    NabiTheme {
        HistoryScreen()
    }
}

@Composable
fun HistoryScreen() {
    // Hoisted state for date range and calendar display focus
    var rangeStartDate by remember { mutableStateOf<LocalDate?>(null) }
    var rangeEndDate by remember { mutableStateOf<LocalDate?>(null) }
    var currentInitialDisplayDate by remember { mutableStateOf(LocalDate.now()) } // Initialize with today

    val today = LocalDate.now() // Define today once for button actions

    Column {
        Row(
            Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                text = "History",
                fontFamily = AmiriFamily,
                color = Color(0xff740D0D),
                fontWeight = FontWeight.Bold,
                fontSize = 24.sp
            )

            Row(
                Modifier
                    .background(color = Color(0xffBBE3FF), shape = RoundedCornerShape(12.dp))
                    .padding(10.dp, 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Image(
                    modifier = Modifier.size(20.dp),
                    painter = painterResource(R.drawable.diagnosis_24px),
                    colorFilter = ColorFilter.tint(Color.Black),
                    contentDescription = "Generate Report Icon" // Added content description
                )

                Spacer(Modifier.width(4.dp))

                Text(
                    text = "Generate Report",
                    fontFamily = RobotoPretendardFamily,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 12.sp
                )
            }
        }

        Text(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 24.dp, end = 24.dp, top = 0.dp, bottom = 8.dp), // Adjusted padding
            text = "${
                currentInitialDisplayDate.month.getDisplayName(
                    TextStyle.FULL,
                    Locale.ENGLISH
                )
            } ${currentInitialDisplayDate.year}", // Dynamic month/year
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = AmiriFamily,
            color = Color(0xff000000)
        )

        ExpandableCalendarViewAllMonths(
            initialDisplayDate = currentInitialDisplayDate, // Use hoisted state for calendar focus
            rangeStartDate = rangeStartDate,                // Pass hoisted state
            rangeEndDate = rangeEndDate                     // Pass hoisted state
        )

        Spacer(Modifier.height(16.dp))

        Row(
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
        ) { // Use horizontal padding for the Row
            RangeSelectButton(
                modifier = Modifier
                    .weight(1f), // Removed fillMaxWidth here, weight handles distribution
                text = "LAST 7 DAYS"
            ) {
                rangeEndDate = today
                rangeStartDate = today.minusDays(6)
                currentInitialDisplayDate = today // Update calendar focus to today
            }

            Spacer(Modifier.width(12.dp)) // Adjusted spacer width

            RangeSelectButton(
                modifier = Modifier
                    .weight(1f),
                text = "LAST 14 DAYS"
            ) {
                rangeEndDate = today
                rangeStartDate = today.minusDays(13)
                currentInitialDisplayDate = today
            }

            Spacer(Modifier.width(12.dp)) // Adjusted spacer width


            RangeSelectButton(
                modifier = Modifier
                    .weight(1f),
                text = "LAST 30 DAYS"
            ) {
                rangeEndDate = today
                rangeStartDate = today.minusDays(29)
                currentInitialDisplayDate = today
            }
        }
        Spacer(Modifier.height(16.dp))

        LazyColumn(
            Modifier
                .fillMaxSize()
                .background(
                    color = Color(0x80FBFFB7),
                    shape = RoundedCornerShape(16.dp, 16.dp, 0.dp, 0.dp)
                )
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
           items(10) {
               HistoryItem("Today", "This is a sample history item content.")
           }
        }
    }
}

@Composable
fun HistoryItem(date: String, content: String) {
    Card(
        elevation = CardDefaults.cardElevation(
            defaultElevation = 2.dp
        ),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        )
    ) {
        Column(Modifier.fillMaxWidth().padding(16.dp, 24.dp)) {
            Text(text = date, fontSize = 16.sp, fontWeight = FontWeight.Medium, color = Color.Black)
            Spacer(Modifier.height(8.dp))
            Text(text = content, fontSize = 14.sp, fontWeight = FontWeight.Normal, color = Color.Black)
        }
    }
}

@Preview
@Composable
fun HistoryItemPreview() {
    NabiTheme {
        HistoryItem("Today", "This is a sample history item content.")
    }
}

@Composable
fun RangeSelectButton(
    modifier: Modifier = Modifier,
    text: String,
    onClick: () -> Unit
) { // Added default for modifier
    Box(
        modifier = modifier // Modifier passed from caller now defines weight etc.
            .background(color = Color(0xffF2F4FE), RoundedCornerShape(4.dp))
            .clip(RoundedCornerShape(4.dp))
            .clickable { onClick() } // Corrected: onClick.invoke() to onClick()
            .padding(vertical = 8.dp, horizontal = 6.dp) // Adjusted padding for better text fit
    ) {
        Text(
            modifier = Modifier.align(Alignment.Center),
            text = text,
            fontWeight = FontWeight.SemiBold,
            fontSize = 12.sp,
            fontFamily = RobotoPretendardFamily,
            color = MaterialTheme.colorScheme.primary,
            textAlign = TextAlign.Center // Ensure text is centered if it wraps
        )
    }
}